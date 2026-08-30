package dev.jpa.allimio.qa;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.qa.QaDTO.QaResponse;
import dev.jpa.allimio.tool.PageResponse;


@RestController // RESTFull 방식 지원
@RequestMapping("/qa") // http://localhost:9101/qa
public class QaCont {
  @Autowired
  private QaService qaService;

  public QaCont() {
    System.out.println("-> QqCont created");
  }

  /***
   * 작업예정
   * 삭제된 게시글 확인용 데이터 호출 필요
   * (후순위) 중복된 키워드가 5개 이상일때 관리자 화면에서 자주묻는 질문 유형으로 집계(비공개) -> 직접 등록
   * 비밀번호 암호화
   * 권한 설정(비밀글, 관리자 게시 허용글 등
   * 관리자 답변 알림?
   * 작성자 정보 노출?
   */
  
  

  /**
   * 전체 회원 1:1 문의내역 전체/검색 조회 (페이징)
   * GET /qa/list?word=관제&page=0&size=10
   * http://localhost:9102/qa/list
   */
  @GetMapping(path="/list")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> getAllQuestions(
      QaDTO.QaSearchRequest searchCondition, // 👈 요거 하나만 적으면 끝!
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    // Service로 searchCondition 전달
    Page<QaDTO.QaResponse> pageResult = qaService.getAllQuestions(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
  
  /**
   * 비회원 문의 목록 검색 (이메일+키워드)
   * GET /qa/guest/list?guestEmail=xxx@xxx.com&word=환불
   */
  @GetMapping("/guest/list")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> searchGuestList(
      QaDTO.QaSearchRequest searchCondition, // 👈 요거 하나만 적으면 끝!
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    // Service로 searchCondition 전달
    Page<QaDTO.QaResponse> pageResult = qaService.getSearchGuestQa(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
  
  
  /**
   * FAQ 목록 전체/검색 조회 (페이징)
   * GET /qa/faq?word=비밀번호&page=0&size=10
   * http://localhost:9102/qa/faq
   */
  @GetMapping(path="/faq")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> getAllFaqs(
      QaDTO.QaSearchRequest searchCondition, // 👈 요거 하나만 적으면 끝!
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    // Service로 searchCondition 전달
    Page<QaDTO.QaResponse> pageResult = qaService.getFaqs(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 내 문의 내역 전체/검색 조회 (페이징)
   * GET /qa/my/1?word=장비&page=0&size=10
   * http://localhost:9102/qa/my/1
   */
  @GetMapping(path="/my/{mno}")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> getMyQuestions(
      QaDTO.QaSearchRequest searchCondition, // 👈 요거 하나만 적으면 끝!
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    // Service로 searchCondition 전달
    Page<QaDTO.QaResponse> pageResult = qaService.getMyQuestions(searchCondition, pageable);
    
    // PageResponse.of()를 통해 생성자 호출 코드 중복 없이 깔끔하게 반환
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  
  /**
   * 단건 상세 조회 (회원 본인 글 / 관리자 / 비밀글 아닌 글 전용)
   * GET /qa/22
   *
   * 비밀번호가 필요한 비회원 잠긴 글 조회는 이 API를 쓰지 않습니다.
   * (URL 쿼리파라미터로 비밀번호를 전달하면 브라우저 히스토리/서버 접근로그/
   * 리퍼러 등을 통해 노출될 수 있어 보안상 금지 — POST /qa/{no}/verify 사용)
   *
   * @param no 게시글 번호
   * @param accessNo 요청자 PK (회원번호 또는 관리자번호)
   * @param grade 회원 등급 (1 = 관리자)
   */
  @GetMapping("/{no}")
  public ResponseEntity<QaDTO.QaResponse> getQaDetail(
      @PathVariable("no") Long no,
      @RequestHeader(name = "accessNo", required = false) Long accessNo,
      @RequestHeader(name = "grade", required = false) Integer grade) {

    boolean isAdmin = grade != null && grade == 1;
    Long mno = isAdmin ? null : accessNo;
    Long ano = isAdmin ? accessNo : null;

    // pw는 항상 null로 호출 — 이 경로로는 비밀글(잠긴 글)을 열 수 없고,
    // 본인 글이 아닌 비밀글이면 서비스에서 예외가 발생합니다.
    QaDTO.QaResponse response = qaService.getQaDetail(no, mno, ano, null);

    return ResponseEntity.ok(response);
  }

  /**
   * 비밀글(비회원 잠긴 글 등) 비밀번호 검증 후 상세 조회.
   * POST /qa/22/verify
   * 비밀번호를 요청 바디로 전달하여 URL 노출을 방지합니다.
   *
   * @param no 게시글 번호
   * @param request 입력한 비밀번호 (GuestDetailRequest.pw)
   */
  @PostMapping("/{no}/verify")
  public ResponseEntity<QaDTO.QaResponse> verifyAndGetQa(
      @PathVariable("no") Long no,
      @RequestBody QaDTO.GuestDetailRequest request) {

    // 회원/관리자 여부와 무관하게 비밀번호로만 검증하는 경로이므로 mno/ano는 null 고정
    QaDTO.QaResponse response = qaService.getQaDetail(no, null, null, request.getPw());

    return ResponseEntity.ok(response);
  }
  
  /**
   * 1:1 문의글 작성 (등록)
   * POST /qa
   * http://localhost:9102/qa
   */
  @PostMapping
  public ResponseEntity<QaDTO.QaResponse> createQuestion(@RequestBody QaDTO.QCRequest dto) {
    QaDTO.QaResponse response = qaService.createQuestion(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 1:1 문의글 수정
   * PUT /api/qa/10
   */
  @PutMapping(path="/{no}")
  public ResponseEntity<String> updateQuestion(
      @PathVariable("no") Long no,
      @RequestBody QaDTO.QCRequest updateDto) {

    qaService.updateQuestion(no, updateDto);
    return ResponseEntity.ok("문의글이 성공적으로 수정되었습니다.");
  }


  // ==========================================
  // [관리자 / FAQ] Endpoints
  // ==========================================


  /**
   * 6. [관리자] 1:1 문의글 답변 작성/수정
   * PUT /api/qa/reply/10
   * http://localhost:9101/qa/reply/22
   */
  @PutMapping(path="/reply/{no}")
  public ResponseEntity<String> replyToQuestion(
      @PathVariable("no") Long no,
      @RequestBody QaDTO.QARequest replyDto) {

    qaService.replyToQuestion(no, replyDto);
    return ResponseEntity.ok("답변이 성공적으로 등록되었습니다.");
  }

  /**
   * 8. FAQ 작성 (등록)
   * POST /qa/faq
   * http://localhost:9102/qa/faq
   */
  @PostMapping(path="/faq")
  public ResponseEntity<Long> createFAQ(@RequestBody QaDTO.FaqCRequest dto) {
    Long createdNo = qaService.createFAQ(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdNo);
  }

  /**
   * 9. FAQ 수정
   * PUT /qa/faq/10
   * http://localhost:9102/qa/faq/25
   */
  @PutMapping(path="/faq/{no}")
  public ResponseEntity<String> updateFAQ(
      @PathVariable("no") Long no,
      @RequestBody QaDTO.FaqCRequest updateDto) {

    qaService.updateFAQ(no, updateDto);
    return ResponseEntity.ok("FAQ가 성공적으로 수정되었습니다.");
  }

  
  
  

  /**
   * 문의글/FAQ 삭제
   * DELETE /qa
   * http://localhost:9102/qa
   */
  @DeleteMapping
  public ResponseEntity<String> deleteQuestion(
      @RequestBody QaDTO.DeleteRequest deleteDto) {
    qaService.deleteQuestion(deleteDto);
    return ResponseEntity.ok("성공적으로 삭제되었습니다.");
  }

}
