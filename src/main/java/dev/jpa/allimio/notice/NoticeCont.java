package dev.jpa.allimio.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.tool.PageResponse;


@RestController // RESTFull 방식 지원
@RequestMapping("/notice") // http://localhost:9102/notice
public class NoticeCont {
  @Autowired
  private NoticeService noticeService;

  public NoticeCont() {
    System.out.println("-> NoticeCont created");
  }
  
  /**
   * [일반] 전체/검색 조회 (페이징)
   * GET /notice/list?word=관제&page=0&size=10
   */
  @GetMapping(path="/list")
  public ResponseEntity<PageResponse<NoticeDTO.NoticeResponse>> getAllNotice(
      NoticeDTO.NoticeSearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<NoticeDTO.NoticeResponse> pageResult = noticeService.getAllNotices(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
  
  /**
   * [관리자] 전체/검색 조회 (페이징)
   * GET /notice/list/admin?word=관제&page=0&size=10
   */
  @GetMapping(path="/list/admin")
  public ResponseEntity<?> getAllNoticeAdmin(
      NoticeDTO.NoticeSearchRequest searchCondition,
      @RequestHeader(name = "accessNo", required = false) Long accessNo,
      @RequestHeader(name = "grade", required = false) Integer grade,
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    // 1. 관리자 권한 체크 (grade 1 = 관리자)
    boolean isAdmin = (grade != null && grade == 1);
    
    // 2. 관리자가 아니면 DB 조회 없이 즉시 403 Forbidden 반환
    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 없습니다.");
    }

    // 3. 관리자인 경우에만 DB 조회 실행
    Page<NoticeDTO.NoticeResponse> pageResult = noticeService.getAllNoticesAdmin(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));      
  }

  /**
   * 단건 상세 조회
   * GET /notice/1
   */
  @GetMapping("/{no}")
  public ResponseEntity<NoticeDTO.NoticeResponse> getNoticeDetail(@PathVariable("no") Long no) {
    // Service에서 DTO로 변환된 결과를 바로 받아 응답
    NoticeDTO.NoticeResponse response = noticeService.getNotice(no);
    return ResponseEntity.ok(response);
  }

  /**
   * 공지사항 작성
   * POST /notice
   */
  @PostMapping
  public ResponseEntity<Long> createNotice(@RequestBody NoticeDTO.NCRequest dto) {
    Long createdNo = noticeService.createNotice(dto);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdNo);
  }

  /**
   * 공지사항 수정
   * PUT /notice/10
   */
  @PutMapping(path="/{no}")
  public ResponseEntity<String> updateNotice(
      @PathVariable("no") Long no,
      @RequestBody NoticeDTO.NCRequest updateDto) {

    noticeService.updateNotice(no, updateDto);
    return ResponseEntity.ok("공지사항이 성공적으로 수정되었습니다.");
  }
  
  /**
   * 공지사항 삭제 (소프트 삭제)
   * DELETE /notice
   */
  @DeleteMapping
  public ResponseEntity<String> deleteNotice(@RequestBody NoticeDTO.DeleteRequest deleteDto) {
    noticeService.deleteNotice(deleteDto);
    return ResponseEntity.ok("성공적으로 삭제되었습니다.");
  }

}
