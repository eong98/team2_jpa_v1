package dev.jpa.allimio.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.tool.Tool;

@Service
@Transactional(readOnly = true)
public class NoticeService {
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
    NoticeDTO.NoticeNav prev = noticeRepository
        .findFirstByNoLessThanAndIsdelAndVmodeOrderByNoDesc(no, "N", "Y")
        .map(n -> new NoticeDTO.NoticeNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getCdate()))
        .orElse(null);

    NoticeDTO.NoticeNav next = noticeRepository
        .findFirstByNoGreaterThanAndIsdelAndVmodeOrderByNoAsc(no, "N", "Y")
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
      if (!notice.matchPw(updateDto.getPw())) {
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
    Notice notice = noticeRepository.findByNoAndPwAndIsdel(deleteDto.getNo(), deleteDto.getPw(), "N")
        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않거나 비밀번호가 일치하지 않습니다."));

    notice.delete(Tool.getDate());
  }

}