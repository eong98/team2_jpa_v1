package dev.jpa.allimio.chatbot.log;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  public ChatLogCont() {
    System.out.println("-> ChatLogCont created");
  }

  /**
   * 로그 등록
   * POST /chat_log
   */
  @PostMapping
  public ResponseEntity<ChatLogDTO.Response> create(@RequestBody ChatLogDTO.Request request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(chatLogService.create(request));
  }

  /**
   * 특정 세션의 전체 로그 조회
   * GET /chat_log/session/{sno}
   */
  @GetMapping("/session/{sno}")
  public ResponseEntity<List<ChatLogDTO.Response>> findBySno(@PathVariable("sno") String sno) {
    return ResponseEntity.ok(chatLogService.findBySno(sno));
  }
}