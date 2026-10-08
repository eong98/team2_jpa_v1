package dev.jpa.allimio.qa;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.tool.PageResponse;

/* ---------------------------------------------------------------------
   문의사항(QA) / FAQ API

   요청자 구분은 로그인 JWT(HttpOnly 쿠키)로만 합니다.
   - /qa/{no}        : 회원·관리자 (로그인 필요)
   - /qa/guest/{no}  : 비회원 (POST /qa/guest/{no}/verify 로 받은 임시 토큰을 X-Qa-Token 헤더로)
   - 답변·FAQ 등록/수정·관리자 삭제 : 관리자만 (SecurityConfig)
--------------------------------------------------------------------- */
@RestController // RESTFull 방식 지원
@RequestMapping("/qa") // http://localhost:9101/qa
public class QaCont {
  /** 비회원 임시 토큰 헤더 */
  public static final String GUEST_TOKEN_HEADER = "X-Qa-Token";

  @Autowired
  private QaService qaService;

  public QaCont() {
    System.out.println("-> QqCont created");
  }

  /**
   * 전체 회원 1:1 문의내역 전체/검색 조회 (페이징)
   * GET /qa/list?word=관제&page=0&size=10
   * 관리자가 아니면 남의 비밀글 내용·답변·이메일은 빈 값
   * http://localhost:9102/qa/list
   */
  @GetMapping(path="/list")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> getAllQuestions(
      Authentication auth,
      QaDTO.QaSearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<QaDTO.QaResponse> pageResult = qaService.getAllQuestions(
        searchCondition, pageable, isManager(auth), memberNoOrNull(auth));
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 비회원 문의 목록 검색 (이메일+키워드)
   * GET /qa/guest/list?word=환불
   */
  @GetMapping("/guest/list")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> searchGuestList(
      QaDTO.QaSearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

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
      QaDTO.QaSearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<QaDTO.QaResponse> pageResult = qaService.getFaqs(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 내 문의 내역 전체/검색 조회 (페이징) — 경로의 mno 대신 로그인 회원번호로 조회
   * GET /qa/my/1?word=장비&page=0&size=10
   * http://localhost:9102/qa/my/1
   */
  @GetMapping(path="/my/{mno}")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> getMyQuestions(
      Authentication auth,
      QaDTO.QaSearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    searchCondition.setMno(requireMember(auth));
    Page<QaDTO.QaResponse> pageResult = qaService.getMyQuestions(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  // ==========================================
  // [회원 / 관리자] 상세·등록·수정·삭제
  // ==========================================

  /**
   * 상세 조회 (관리자: 모든 글 / 회원: 비밀글은 본인 글만)
   * GET /qa/22
   */
  @GetMapping("/{no}")
  public ResponseEntity<QaDTO.QaResponse> getQaDetail(Authentication auth, @PathVariable("no") Long no) {
    if (isManager(auth)) {
      return ResponseEntity.ok(qaService.getAdminQaDetail(no));
    }
    return ResponseEntity.ok(qaService.getMemberQaDetail(no, requireMember(auth)));
  }

  /**
   * 1:1 문의글 작성 (회원·비회원 공용)
   * POST /qa — 로그인 회원이면 회원 글, 아니면 비회원 글 (body의 mno는 무시)
   * http://localhost:9102/qa
   */
  @PostMapping
  public ResponseEntity<QaDTO.QaResponse> createQuestion(Authentication auth, @RequestBody QaDTO.QCRequest dto) {
    QaDTO.QaResponse response = qaService.createQuestion(dto, memberNoOrNull(auth));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * [회원] 본인 문의글 수정
   * PUT /qa/10
   */
  @PutMapping(path="/{no}")
  public ResponseEntity<String> updateQuestion(
      Authentication auth,
      @PathVariable("no") Long no,
      @RequestBody QaDTO.QCRequest updateDto) {

    qaService.updateMemberQuestion(no, requireMember(auth), updateDto);
    return ResponseEntity.ok("문의글이 성공적으로 수정되었습니다.");
  }

  /**
   * [회원] 본인 문의글 삭제
   * DELETE /qa/10
   */
  @DeleteMapping(path="/{no}")
  public ResponseEntity<String> deleteMyQuestion(Authentication auth, @PathVariable("no") Long no) {
    qaService.deleteMemberQuestion(no, requireMember(auth));
    return ResponseEntity.ok("성공적으로 삭제되었습니다.");
  }

  // ==========================================
  // [비회원] 비밀번호 확인 → 임시 토큰으로 상세·수정·삭제
  // ==========================================

  /**
   * 비회원 게시글 비밀번호 확인 → 이 글 전용 임시 토큰(10분) 발급
   * POST /qa/guest/22/verify  {pw}
   * 응답 {token, expiresIn(초)} — 5회 연속 실패 시 10분 잠금(429)
   */
  @PostMapping("/guest/{no}/verify")
  public ResponseEntity<Map<String, Object>> verifyGuest(
      @PathVariable("no") Long no,
      @RequestBody QaDTO.GuestDetailRequest request) {

    String token = qaService.verifyGuestPw(no, request.getPw());
    return ResponseEntity.ok(Map.of(
        "token", token,
        "expiresIn", QaGuestTokenProvider.TOKEN_EXPIRE_MILLIS / 1000));
  }

  /**
   * 비회원 상세 조회 — 비밀글이 아니면 누구나, 비회원 비밀글은 임시 토큰 필요
   * GET /qa/guest/22  (헤더 X-Qa-Token)
   */
  @GetMapping("/guest/{no}")
  public ResponseEntity<QaDTO.QaResponse> getGuestDetail(
      @PathVariable("no") Long no,
      @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String token) {
    return ResponseEntity.ok(qaService.getGuestQaDetail(no, token));
  }

  /**
   * 비회원 문의글 수정
   * PUT /qa/guest/22  (헤더 X-Qa-Token)
   */
  @PutMapping("/guest/{no}")
  public ResponseEntity<String> updateGuestQuestion(
      @PathVariable("no") Long no,
      @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String token,
      @RequestBody QaDTO.QCRequest updateDto) {

    qaService.updateGuestQuestion(no, token, updateDto);
    return ResponseEntity.ok("문의글이 성공적으로 수정되었습니다.");
  }

  /**
   * 비회원 문의글 삭제
   * DELETE /qa/guest/22  (헤더 X-Qa-Token)
   */
  @DeleteMapping("/guest/{no}")
  public ResponseEntity<String> deleteGuestQuestion(
      @PathVariable("no") Long no,
      @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String token) {

    qaService.deleteGuestQuestion(no, token);
    return ResponseEntity.ok("성공적으로 삭제되었습니다.");
  }

  // ==========================================
  // [관리자 / FAQ] Endpoints — SecurityConfig에서 ROLE_MANAGER만 허용
  // ==========================================

  /**
   * [관리자] 1:1 문의글 답변 작성/수정 — 답변자는 로그인한 관리자
   * PUT /qa/reply/22
   */
  @PutMapping(path="/reply/{no}")
  public ResponseEntity<String> replyToQuestion(
      Authentication auth,
      @PathVariable("no") Long no,
      @RequestBody QaDTO.QARequest replyDto) {

    qaService.replyToQuestion(no, (Long) auth.getPrincipal(), replyDto);
    return ResponseEntity.ok("답변이 성공적으로 등록되었습니다.");
  }

  /**
   * [관리자] FAQ 작성 (등록)
   * POST /qa/faq
   */
  @PostMapping(path="/faq")
  public ResponseEntity<Long> createFAQ(@RequestBody QaDTO.FaqCRequest dto) {
    Long createdNo = qaService.createFAQ(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdNo);
  }

  /**
   * [관리자] FAQ 수정
   * PUT /qa/faq/25
   */
  @PutMapping(path="/faq/{no}")
  public ResponseEntity<String> updateFAQ(
      @PathVariable("no") Long no,
      @RequestBody QaDTO.FaqCRequest updateDto) {

    qaService.updateFAQ(no, updateDto);
    return ResponseEntity.ok("FAQ가 성공적으로 수정되었습니다.");
  }

  /**
   * [관리자] FAQ 삭제 — 실제 DB 삭제 (게시글 비밀번호 확인)
   * DELETE /qa  {no, pw}
   */
  @DeleteMapping
  public ResponseEntity<String> deleteQuestion(@RequestBody QaDTO.DeleteRequest deleteDto) {
    qaService.deleteQuestion(deleteDto);
    return ResponseEntity.ok("성공적으로 삭제되었습니다.");
  }

  /**
   * [관리자] 삭제된 1:1 문의 목록
   * GET /qa/deleted?word=&type=&mno=&page=0&size=10
   */
  @GetMapping(path="/deleted")
  public ResponseEntity<PageResponse<QaDTO.QaResponse>> getDeletedQuestions(
      QaDTO.QaSearchRequest searchCondition,
      @PageableDefault(size = 10) Pageable pageable) {

    return ResponseEntity.ok(PageResponse.of(qaService.getDeletedQuestions(searchCondition, pageable)));
  }

  /**
   * [관리자] 삭제된 1:1 문의 영구 삭제 (복구 불가)
   * DELETE /qa/deleted/22
   */
  @DeleteMapping(path="/deleted/{no}")
  public ResponseEntity<String> purgeQuestion(@PathVariable("no") Long no) {
    qaService.purgeQuestion(no);
    return ResponseEntity.ok("영구 삭제되었습니다.");
  }

  // ==========================================
  // 인증 정보 / 오류 응답
  // ==========================================

  /** 관리자(ROLE_MANAGER) 로그인 여부 */
  private boolean isManager(Authentication auth) {
    return hasRole(auth, "ROLE_MANAGER");
  }

  /** 회원 로그인이면 회원번호, 아니면 null (관리자 번호는 회원번호와 겹칠 수 있어 제외) */
  private Long memberNoOrNull(Authentication auth) {
    if (hasRole(auth, "ROLE_MEMBER") && auth.getPrincipal() instanceof Long no) {
      return no;
    }
    return null;
  }

  /** 회원 로그인 필수 */
  private Long requireMember(Authentication auth) {
    Long mno = memberNoOrNull(auth);
    if (mno == null) {
      throw new QaAccessException(403, "FORBIDDEN", "회원만 이용할 수 있습니다.");
    }
    return mno;
  }

  private boolean hasRole(Authentication auth, String role) {
    return auth != null && auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(role::equals);
  }

  /** 접근 오류 → 상태코드 + {success:false, code, message} (화면이 message를 그대로 표시) */
  @ExceptionHandler(QaAccessException.class)
  public ResponseEntity<Map<String, Object>> handleAccess(QaAccessException e) {
    return ResponseEntity.status(e.getStatus())
        .body(Map.of("success", false, "code", e.getCode(), "message", e.getMessage()));
  }

}
