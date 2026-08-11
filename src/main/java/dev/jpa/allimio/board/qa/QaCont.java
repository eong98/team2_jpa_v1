package dev.jpa.allimio.board.qa;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
   * 입력값 null, 공백 체크 유효성 검증
   * 권한 설정(비밀글, 관리자 게시 허용글 등
   * 관리자 답변 알림?
   * 첨부파일 연동
   * 작성자 정보 노출?
   * 잠긴글 상세 다른 회원 접근불가처리
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
   * 문의글 상세 조회 
   * GET /qa/22?mno=1 (회원 조회 시)
   * GET /qa/22?ano=1 (관리자 조회 시)
   * 
   * @param no 게시글 번호
   * @param mno 요청 회원 번호 (optional)
   * @param ano 요청 관리자 번호 (optional)
   * @return
   */
  @GetMapping("/{no}")
  public ResponseEntity<QaDTO.QaResponse> getQaDetail(@PathVariable("no") Long no,
      @RequestParam(name = "mno", required = false) Long mno,
      @RequestParam(name = "ano", required = false) Long ano) {
    // 1. Service에서 엔티티 조회
    Qa qa = qaService.getQa(no, mno, ano);
    
    // 2. DTO 변환 후 200 OK 응답 반환
    return ResponseEntity.ok(QaDTO.QaResponse.fromEntity(qa));
  }
  
  
  /**
   * 1:1 문의글 작성 (등록)
   * POST /qa
   * http://localhost:9102/qa
   */
  @PostMapping
  public ResponseEntity<Long> createQuestion(@RequestBody QaDTO.QCRequest dto) {
    Long createdNo = qaService.createQuestion(dto);
    return ResponseEntity
        .status(HttpStatus.CREATED) // 1. HTTP 응답 상태 코드를 '201 Created'로 설정 
        //-> 새로운 자원(데이터)이 성공적으로 생성 200대신 201 로 전송 권장
        .body(createdNo);           // 2. 응답 본문(Body)에 생성된 게시글의 번호(PK)를 담아 반환
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
