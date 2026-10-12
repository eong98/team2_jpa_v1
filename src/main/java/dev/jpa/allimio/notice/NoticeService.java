package dev.jpa.allimio.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.tool.Tool;

@Service
@Transactional(readOnly = true)
public class NoticeService {
  /** 게시글 비밀번호 암호화 (SecurityConfig의 BCryptPasswordEncoder) */
  @Autowired
  private PasswordEncoder pwEncoder;

  @Autowired
  NoticeRepository noticeRepository;
  
  public NoticeService() {
    System.out.println("-> NoticeService created");
  }
  /**
   * [일반 사용자] 공지사항 전체 + 키워드 검색 (페이징)
   */
  public Page<NoticeDTO.NoticeResponse> getAllNotices(NoticeDTO.NoticeSearchRequest req, Pageable pageable) {
    Page<Notice> noticePage = noticeRepository.searchAllNotice(
        req.getWord(),
        req.getType(),
        pageable
    );
    return noticePage.map(NoticeDTO.NoticeResponse::fromEntity);
  }
  
  /**
   * [관리자] 공지사항 전체 + 키워드 검색 (페이징)
   */
  public Page<NoticeDTO.NoticeResponse> getAllNoticesAdmin(NoticeDTO.NoticeSearchRequest req, Pageable pageable) {
    Page<Notice> noticePage = noticeRepository.searchAdminNotice(
        req.getWord(),
        req.getType(),
        req.getVmode(),
        pageable
    );

    System.out.println(req.getVmode());
    return noticePage.map(NoticeDTO.NoticeResponse::fromEntity);
  }

  /**
   * 게시글 상세페이지 조회 (조회수 증가 + DTO 반환)
   */
  @Transactional
  public NoticeDTO.NoticeResponse getNotice(Long no, boolean isManager) {
    // 1. 게시글 존재 및 미삭제 여부 확인 — 비공개 공지는 관리자만 (주소를 직접 입력해도 열리지 않게)
    noticeRepository.findById(no)
        .filter(n -> "N".equals(n.getIsdel()))
        .filter(n -> isManager || "Y".equals(n.getVmode()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));

    // 2. 조회수 증가 — DB에서 한 번에 올린 뒤, 올라간 값이 반영된 글을 다시 읽음
    noticeRepository.increaseVcnt(no);
    Notice notice = noticeRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));

    // 3. 이전글 / 다음글 조회 (미삭제만 — 관리자는 비공개 공지 포함, 그 외는 공개글만)
    //    목록에 보이는 순서 기준 — 이전글: 목록에서 바로 위 글, 다음글: 바로 아래 글
    String fixyn = notice.getFixyn() == null ? "N" : notice.getFixyn();
    NoticeDTO.NoticeNav prev = noticeRepository
        .findAboveInList(fixyn, notice.getType(), notice.getCdate(), no, isManager, PageRequest.of(0, 1)).stream().findFirst()
        .map(n -> new NoticeDTO.NoticeNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getCdate()))
        .orElse(null);

    NoticeDTO.NoticeNav next = noticeRepository
        .findBelowInList(fixyn, notice.getType(), notice.getCdate(), no, isManager, PageRequest.of(0, 1)).stream().findFirst()
        .map(n -> new NoticeDTO.NoticeNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getCdate()))
        .orElse(null);

    // 4. DTO 변환 후 반환
    return NoticeDTO.NoticeResponse.fromEntity(notice, prev, next);
  }

  /**
   * 공지사항 등록
   */
  @Transactional
  public Long createNotice(NoticeDTO.NCRequest dto, Long ano) {
    Notice notice = dto.toEntity();
    notice.changeAno(ano); // 작성자는 로그인한 관리자 (body의 ano는 쓰지 않음)
    notice.changePw(null); // 공지는 게시글 비밀번호를 쓰지 않음 (관리자 로그인으로 권한 확인)
    Notice savedNotice = noticeRepository.save(notice);
    return savedNotice.getNo();
  }
  
  /**
   * 공지사항 수정
   */
  @Transactional
  public void updateNotice(Long no, NoticeDTO.NCRequest updateDto) {
    Notice notice = noticeRepository.findById(no)
        .filter(n -> "N".equals(n.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));

    // 권한은 SecurityConfig에서 관리자만 — 게시글 비밀번호 확인 없음
    
    // NoticeDTO 내부 applyUpdateTo 호출로 엔티티 수정
    updateDto.applyUpdateTo(notice);
  }

  /**
   * 공지사항 소프트 삭제
   */
  @Transactional
  public void deleteNotice(NoticeDTO.DeleteRequest deleteDto) {
    // 권한은 SecurityConfig에서 관리자만 — 게시글 비밀번호 확인 없음
    Notice notice = noticeRepository.findById(deleteDto.getNo())
        .filter(n -> "N".equals(n.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다."));

    notice.delete(Tool.getDate());
  }

  /**
   * [관리자] 삭제된(소프트 삭제) 공지사항 목록
   */
  public Page<NoticeDTO.NoticeResponse> getDeletedNotices(NoticeDTO.NoticeSearchRequest req, Pageable pageable) {
    return noticeRepository.searchDeletedNotice(req.getWord(), req.getType(), pageable)
        .map(NoticeDTO.NoticeResponse::fromEntity);
  }

  /**
   * [관리자] 삭제된 공지사항 복원 — 삭제(ISDEL='Y')된 글만 가능
   * @return 복원했으면 true, 삭제된 목록에 없는 글이면 false
   */
  @Transactional
  public boolean restoreNotice(Long no) {
    return noticeRepository.findById(no)
        .filter(n -> "Y".equals(n.getIsdel()))
        .map(n -> {
          n.restore();
          return true;
        })
        .orElse(false);
  }

  /**
   * [관리자] 삭제된 공지사항 영구 삭제 (DB에서 실제 삭제)
   * - 이미 삭제(ISDEL='Y')된 글만 가능
   * - 첨부파일은 화면에서 삭제 성공 후 따로 지움 (DELETE /attach/delete_by_bno/{no}?tname=NOTICE)
   * @return 삭제했으면 true, 삭제된 목록에 없는 글이면 false
   */
  @Transactional
  public boolean purgeNotice(Long no) {
    return noticeRepository.findById(no)
        .filter(n -> "Y".equals(n.getIsdel()))
        .map(n -> {
          noticeRepository.delete(n);
          return true;
        })
        .orElse(false);
  }

}