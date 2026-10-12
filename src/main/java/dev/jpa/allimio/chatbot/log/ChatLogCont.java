package dev.jpa.allimio.chatbot.log;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat_log")
public class ChatLogCont {
  @Autowired
  ChatLogService chatLogService;

  /** 회원 소유 상담의 대화는 그 회원 토큰이 있어야 조회·등록 (ChatAccess 설명 참고) */
  @Autowired
  dev.jpa.allimio.chatbot.session.ChatAccess chatAccess;

  public ChatLogCont() {
    System.out.println("-> ChatLogCont created");
  }

  /**
   * 로그 등록
   * POST /chat_log
   */
  @PostMapping
  public ResponseEntity<ChatLogDTO.Response> create(Authentication auth, @RequestBody ChatLogDTO.Request request) {
    chatAccess.assertSessionAccess(request.getSno(), auth);
    return ResponseEntity.status(HttpStatus.CREATED).body(chatLogService.create(request));
  }

  /**
   * 특정 세션의 전체 로그 조회
   * GET /chat_log/session/{sno}
   */
  @GetMapping("/session/{sno}")
  public ResponseEntity<List<ChatLogDTO.Response>> findBySno(Authentication auth, @PathVariable("sno") String sno) {
    chatAccess.assertSessionAccess(sno, auth);
    return ResponseEntity.ok(chatLogService.findBySno(sno));
  }
}