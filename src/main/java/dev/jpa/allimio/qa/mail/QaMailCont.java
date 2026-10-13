package dev.jpa.allimio.qa.mail;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.qa.QaAccessException;
import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   /dbms/qamail(문의 메일 발송 내역) API - 관리자 전용
   (SecurityConfig에서 /qa/mail/** 는 ROLE_MANAGER만)

   GET  /qa/mail/logs?word=&stype=&result=&page=0&size=10  → 발송 내역 목록 (최근 발송 순)
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/qa/mail")
@RequiredArgsConstructor
public class QaMailCont {

  private final QaSendLogQuery sendLogQuery;
  private final QaMailService qaMailService;

  /**
   * 발송 내역 목록
   * http://localhost:9102/qa/mail/logs?page=0&size=10
   * @param stype  1 답변 알림 / 2 비밀번호 재설정 인증 (없으면 전체)
   * @param result Y 성공 / N 실패 (없으면 전체)
   */
  @GetMapping("/logs")
  public ResponseEntity<QaMailDTO.LogPage> logs(
      @RequestParam(value = "word", required = false) String word,
      @RequestParam(value = "stype", required = false) Integer stype,
      @RequestParam(value = "result", required = false) String result,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    int safePage = Math.max(0, page);
    int safeSize = Math.min(100, Math.max(1, size));
    return ResponseEntity.ok(sendLogQuery.search(word, stype, result, safePage, safeSize));
  }

  @ExceptionHandler(QaAccessException.class)
  public ResponseEntity<Map<String, Object>> handleAccess(QaAccessException e) {
    return ResponseEntity.status(e.getStatus())
        .body(Map.of("success", false, "code", e.getCode(), "message", e.getMessage()));
  }
}
