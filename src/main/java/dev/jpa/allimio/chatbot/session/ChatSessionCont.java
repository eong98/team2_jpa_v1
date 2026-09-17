package dev.jpa.allimio.chatbot.session;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 
  public ChatSessionCont() {
    System.out.println("-> ChatSessionCont created");
  }

  /**
   * 세션 생성. cmode=1(AI상담)로 만들면서 greeting을 같이 보내면, 이 안에서
   * 인사말까지 로그로 남겨서 반환합니다. AI상담을 세션 없는 상태에서
   * 바로 시작할 때도 이 엔드포인트를 그대로 씁니다(별도 엔드포인트 없음).
   */
  @PostMapping
  public ResponseEntity<ChatSessionDTO.ActionResult> create(@RequestBody ChatSessionDTO.CreateRequest request,
      @RequestParam(value = "greeting", required = false) String greeting) {
    return ResponseEntity.status(HttpStatus.CREATED).body(chatSessionService.create(request, greeting));
  }
 
  @GetMapping("/{no}")
  public ResponseEntity<ChatSessionDTO.Response> findById(@PathVariable("no") String no) {
    return ResponseEntity.ok(chatSessionService.findById(no));
  }
 
  @GetMapping("/active")
  public ResponseEntity<ChatSessionDTO.Response> findActive(
      @RequestParam(value = "mno", required = false) Long mno,
      @RequestParam(value = "gno", required = false) String gno) {
    ChatSessionDTO.Response result = chatSessionService.findActive(mno, gno);
    if (result == null) return ResponseEntity.noContent().build();
    return ResponseEntity.ok(result);
  }
 
  @GetMapping("/list")
  public ResponseEntity<List<ChatSessionDTO.Summary>> getList(
      @RequestParam(value = "mno", required = false) Long mno,
      @RequestParam(value = "gno", required = false) String gno) {
    return ResponseEntity.ok(chatSessionService.getList(mno, gno));
  }
 
  /** 옵션 선택 */
  @PutMapping("/{no}/select")
  public ResponseEntity<ChatSessionDTO.ActionResult> selectMenu(
      @PathVariable("no") String no, @RequestBody ChatSessionDTO.SelectRequest request) {
    return ResponseEntity.ok(chatSessionService.selectMenu(no, request.getCno()));
  }
 
  /** 이미 있는 세션을 AI 상담 모드로 전환 (세션 없는 신규 시작은 POST /chat_session을 씀) */
  @PutMapping("/{no}/ai-start")
  public ResponseEntity<ChatSessionDTO.ActionResult> startAiConsult(
      @PathVariable("no") String no,
      @RequestParam(value = "startAi", required = false) String startAi, 
      @RequestParam(value = "greeting", required = false) String greeting) {
    return ResponseEntity.ok(chatSessionService.startAiConsult(no, startAi, greeting));
  }
 
  /** 다른 질문하기(처음으로) */
  @PutMapping("/{no}/back-intro")
  public ResponseEntity<ChatSessionDTO.ActionResult> backToIntro(
      @PathVariable("no") String no,
      @RequestParam(value = "endAi", required = false) String endAi, 
      @RequestParam(value = "greeting", required = false) String greeting) {
    return ResponseEntity.ok(chatSessionService.backToIntro(no, endAi, greeting));
  }
 
  /** 종료/만족도/불만족사유/기타메모/관리자연결요청/관리자연결확인 통합 처리 */
  @PutMapping("/{no}/step")
  public ResponseEntity<ChatSessionDTO.ActionResult> step(
      @PathVariable("no") String no, @RequestBody ChatSessionDTO.StepRequest request) {
    return ResponseEntity.ok(chatSessionService.step(no, request));
  }
 
  /** AI 자유질문 */
  @PostMapping("/{no}/ai-chat")
  public ResponseEntity<ChatSessionDTO.ActionResult> aiChat(
      @PathVariable("no") String no, @RequestBody ChatSessionDTO.AiChatRequest request) {
    return ResponseEntity.ok(chatSessionService.aiChat(no, request.getMessage()));
  }
}