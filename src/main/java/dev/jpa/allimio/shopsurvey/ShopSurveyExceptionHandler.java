package dev.jpa.allimio.shopsurvey;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 매장 설문 API 예외 처리기
 *
 * ShopSurveyCont에서만 동작합니다. 프론트는 { success: false, message } 형식으로 받습니다.
 *
 * - IllegalArgumentException      → 400 (입력값 오류, 필수 문항 누락 등)
 * - SecurityException             → 403 (본인 매장 아님, 점주 아님)
 * - IllegalStateException         → 409 (종료된 설문, 응답 있는 설문 수정, 도배 등 상태 충돌)
 */
@Slf4j
@RestControllerAdvice(assignableTypes = ShopSurveyCont.class)
public class ShopSurveyExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
    return build(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<Map<String, Object>> forbidden(SecurityException e) {
    return build(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> conflict(IllegalStateException e) {
    return build(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> serverError(Exception e) {
    log.error("매장 설문 처리 중 오류", e);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "설문 처리 중 서버 오류가 발생했습니다.");
  }

  private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
