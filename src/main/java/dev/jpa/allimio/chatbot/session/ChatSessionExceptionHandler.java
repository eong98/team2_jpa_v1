package dev.jpa.allimio.chatbot.session;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 챗봇 상담 API 예외 처리 — 서비스의 검증 예외를 500 대신 의미 있는 상태코드로 반환한다.
 *   IllegalArgumentException → 400 (없는 세션, 필수값 누락 등)
 *   IllegalStateException    → 409 (이미 종료된 세션에 대한 요청, 중복 클릭 등)
 */
@RestControllerAdvice(assignableTypes = { ChatSessionCont.class })
public class ChatSessionExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
  }
}
