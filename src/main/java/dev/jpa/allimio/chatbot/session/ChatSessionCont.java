package dev.jpa.allimio.chatbot.session;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat_session")
public class ChatSessionCont {
  @Autowired
  ChatSessionService chatSessionService;

  /** 요청자 확인 — 회원은 로그인 토큰, 비회원은 gno (ChatAccess 설명 참고) */
  @Autowired
  ChatAccess chatAccess;

  @Autowired
  ChatTicketProvider chatTicketProvider;
 
  public ChatSessionCont() {
    System.out.println("-> ChatSessionCont created");
  }

  /**
   * 세션 생성. cmode=1(AI상담)로 만들면서 greeting을 같이 보내면, 이 안에서
   * 인사말까지 로그로 남겨서 반환합니다. AI상담을 세션 없는 상태에서
   * 바로 시작할 때도 이 엔드포인트를 그대로 씁니다(별도 엔드포인트 없음).
   */
  @PostMapping
  public ResponseEntity<ChatSessionDTO.ActionResult> create(Authentication auth,
      @RequestBody ChatSessionDTO.CreateRequest request,
      @RequestParam(value = "greeting", required = false) String greeting) {
    // 상담 주인: 회원이면 토큰의 회원번호(화면이 보낸 mno 값은 쓰지 않음), 아니면 gno
    Long mno = chatAccess.resolveMemberNo(auth, request.getMno());
    return ResponseEntity.status(HttpStatus.CREATED).body(chatSessionService.create(request, mno, greeting));
  }
 
  /**
   * 챗봇 연결용 티켓 — 화면이 FastAPI(AI 상담·웹소켓)를 부를 때 붙임 (ChatTicketProvider 설명 참고)
   * GET /chat_session/ticket?mno= 또는 ?gno=
   * 응답 {ticket, expiresIn(초)} — 비밀키가 없으면 ticket은 null (FastAPI도 검사하지 않음)
   */
  @GetMapping("/ticket")
  public ResponseEntity<java.util.Map<String, Object>> ticket(
      Authentication auth,
      @RequestParam(value = "mno", required = false) Long mno,
      @RequestParam(value = "gno", required = false) String gno) {
    java.util.Map<String, Object> body = new java.util.HashMap<>();
    body.put("expiresIn", ChatTicketProvider.EXPIRE_MILLIS / 1000);

    // 회원이면 토큰의 회원번호로, 아니면 브라우저 식별값(gno)으로 발급 — 화면이 보낸 mno 값은 쓰지 않음
    Long memberNo = chatAccess.resolveMemberNo(auth, mno);
    if (memberNo != null) {
      body.put("ticket", chatTicketProvider.issueForMember(memberNo));
    } else if (gno != null && !gno.isBlank()) {
      body.put("ticket", chatTicketProvider.issueForGuest(gno));
    } else {
      throw new IllegalArgumentException("식별값이 없습니다.");
    }
    return ResponseEntity.ok(body);
  }

  /**
   * 관리자 화면용 티켓 — 챗봇 매뉴얼·옵션메뉴 관리(FastAPI /api/chatbot/manual-doc/*) 호출에 붙임
   * GET /chat_session/ticket/admin — 관리자 로그인(ROLE_MANAGER)일 때만 발급
   */
  @GetMapping("/ticket/admin")
  public ResponseEntity<java.util.Map<String, Object>> adminTicket(Authentication auth) {
    boolean isManager = auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()));
    if (!isManager) {
      // 로그인 안 됨·토큰 만료 → 401(화면이 토큰 재발급 후 재시도), 회원 → 403
      throw new org.springframework.web.server.ResponseStatusException(
          auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String
              ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN, "관리자만 이용할 수 있습니다.");
    }
    java.util.Map<String, Object> body = new java.util.HashMap<>();
    body.put("expiresIn", ChatTicketProvider.EXPIRE_MILLIS / 1000);
    body.put("ticket", chatTicketProvider.issueForManager((Long) auth.getPrincipal()));
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{no}")
  public ResponseEntity<ChatSessionDTO.Response> findById(Authentication auth, @PathVariable("no") String no) {
    chatAccess.assertSessionAccess(no, auth);
    return ResponseEntity.ok(chatSessionService.findById(no));
  }
 
  @GetMapping("/active")
  public ResponseEntity<ChatSessionDTO.Response> findActive(
      Authentication auth,
      @RequestParam(value = "mno", required = false) Long mno,
      @RequestParam(value = "gno", required = false) String gno) {
    Long memberNo = chatAccess.resolveMemberNo(auth, mno);
    ChatSessionDTO.Response result = chatSessionService.findActive(memberNo, memberNo != null ? null : gno);
    if (result == null) return ResponseEntity.noContent().build();
    return ResponseEntity.ok(result);
  }
 
  @GetMapping("/list")
  public ResponseEntity<List<ChatSessionDTO.Summary>> getList(
      Authentication auth,
      @RequestParam(value = "mno", required = false) Long mno,
      @RequestParam(value = "gno", required = false) String gno) {
    Long memberNo = chatAccess.resolveMemberNo(auth, mno);
    return ResponseEntity.ok(chatSessionService.getList(memberNo, memberNo != null ? null : gno));
  }
  
  /** 옵션 선택 */
  @PutMapping("/{no}/select")
  public ResponseEntity<ChatSessionDTO.ActionResult> selectMenu(Authentication auth,
      @PathVariable("no") String no, @RequestBody ChatSessionDTO.SelectRequest request) {
    chatAccess.assertSessionAccess(no, auth);
    return ResponseEntity.ok(chatSessionService.selectMenu(no, request.getCno()));
  }
 

 
  /** 다른 질문하기(처음으로) */
  @PutMapping("/{no}/back-intro")
  public ResponseEntity<ChatSessionDTO.ActionResult> backToIntro(Authentication auth,
      @PathVariable("no") String no,
      @RequestParam(value = "greeting", required = false) String greeting) {
    chatAccess.assertSessionAccess(no, auth);
    return ResponseEntity.ok(chatSessionService.backToIntro(no, greeting));
  }
 
  /** 종료/만족도/불만족사유/기타메모/관리자연결요청/관리자연결확인 통합 처리 */
  @PutMapping("/{no}/step")
  public ResponseEntity<ChatSessionDTO.ActionResult> step(Authentication auth,
      @PathVariable("no") String no, @RequestBody ChatSessionDTO.StepRequest request) {
    chatAccess.assertSessionAccess(no, auth);
    return ResponseEntity.ok(chatSessionService.step(no, request));
  }

  /**
   * 자동 종료 전 경고 — 화면이 한동안 반응이 없을 때 호출 (systemMessage: 남길 경고 문구)
   * 때가 됐으면 경고 문구 + 만족도 질문 단계로 바꾸고, 아니면 retryAfterSeconds만 돌려줌
   */
  @PutMapping("/{no}/idle-warn")
  public ResponseEntity<ChatSessionDTO.ActionResult> idleWarn(Authentication auth,
      @PathVariable("no") String no, @RequestBody(required = false) ChatSessionDTO.StepRequest request) {
    chatAccess.assertSessionAccess(no, auth);
    return ResponseEntity.ok(chatSessionService.idleWarn(no, request == null ? null : request.getSystemMessage(), false));
  }

  /** 자동 종료 경고 뒤 [상담 계속하기] (label: 누른 버튼 글자, systemMessage: 이어서 보여 줄 안내 문구) */
  @PutMapping("/{no}/idle-continue")
  public ResponseEntity<ChatSessionDTO.ActionResult> idleContinue(Authentication auth,
      @PathVariable("no") String no, @RequestBody(required = false) ChatSessionDTO.StepRequest request) {
    chatAccess.assertSessionAccess(no, auth);
    return ResponseEntity.ok(chatSessionService.idleContinue(no, request == null ? null : request.getLabel(),
        request == null ? null : request.getSystemMessage()));
  }
 
}