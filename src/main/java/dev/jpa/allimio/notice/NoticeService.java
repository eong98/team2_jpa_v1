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
  public NoticeDTO.NoticeResponse getNotice(Long no) {
    // 1. 게시글 존재 및 미삭제 여부 확인
    Notice notice = noticeRepository.findById(no)
        .filter(n -> "N".equals(n.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));

    // 2. 조회수 증가
    notice.increaseVcnt();

    // 3. 이전글 / 다음글 조회 (공개글 & 미삭제만 대상)
    //    목록에 보이는 순서 기준 — 이전글: 목록에서 바로 위 글, 다음글: 바로 아래 글
    String fixyn = notice.getFixyn() == null ? "N" : notice.getFixyn();
    NoticeDTO.NoticeNav prev = noticeRepository
        .findAboveInList(fixyn, notice.getType(), notice.getCdate(), no, PageRequest.of(0, 1)).stream().findFirst()
        .map(n -> new NoticeDTO.NoticeNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getCdate()))
        .orElse(null);

    NoticeDTO.NoticeNav next = noticeRepository
        .findBelowInList(fixyn, notice.getType(), notice.getCdate(), no, PageRequest.of(0, 1)).stream().findFirst()
        .map(n -> new NoticeDTO.NoticeNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getCdate()))
        .orElse(null);

    // 4. DTO 변환 후 반환
    return NoticeDTO.NoticeResponse.fromEntity(notice, prev, next);
  }

  /**
   * 공지사항 등록
   */
  @Transactional
  public Long createNotice(NoticeDTO.NCRequest dto) {
    Notice notice = dto.toEntity();
    // 게시글 비밀번호는 문의사항(QaService)과 같이 BCrypt로 암호화해서 저장
    if (dto.getPw() != null && !dto.getPw().isBlank()) {
      notice.changePw(pwEncoder.encode(dto.getPw()));
    }
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

    // 비밀번호 입력값이 있는 경우 검증
    if (updateDto.getPw() != null && !updateDto.getPw().isBlank()) {
      if (notice.getPw() == null || !pwEncoder.matches(updateDto.getPw(), notice.getPw())) {
        throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
      }
    }
    
    // NoticeDTO 내부 applyUpdateTo 호출로 엔티티 수정
    updateDto.applyUpdateTo(notice);
  }

  /**
   * 공지사항 소프트 삭제
   */
  @Transactional
  public void deleteNotice(NoticeDTO.DeleteRequest deleteDto) {
    // 암호화된 비밀번호는 DB 조건(=)으로 비교할 수 없으므로 글을 찾은 뒤 pwEncoder.matches로 확인
    Notice notice = noticeRepository.findById(deleteDto.getNo())
        .filter(n -> "N".equals(n.getIsdel()))
        .filter(n -> deleteDto.getPw() != null && n.getPw() != null && pwEncoder.matches(deleteDto.getPw(), n.getPw()))
        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않거나 비밀번호가 일치하지 않습니다."));

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