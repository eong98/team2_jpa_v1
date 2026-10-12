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
   POST /qa/mail/logs/{no}/resend                           → 실패한 답변 알림 다시 보내기
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

  /**
   * 실패한 답변 알림 다시 보내기 — 접수만 하고 바로 응답 (발송은 백그라운드, 결과는 목록에 새 줄로 추가됨)
   * POST /qa/mail/logs/15/resend
   */
  @PostMapping("/logs/{no}/resend")
  public ResponseEntity<Map<String, Object>> resend(@PathVariable("no") Long no) {
    qaMailService.resend(no);
    return ResponseEntity.ok(Map.of("success", true, "message", "다시 보내는 중입니다. 잠시 후 목록을 새로고침해 주세요."));
  }

  @ExceptionHandler(QaAccessException.class)
  public ResponseEntity<Map<String, Object>> handleAccess(QaAccessException e) {
    return ResponseEntity.status(e.getStatus())
        .body(Map.of("success", false, "code", e.getCode(), "message", e.getMessage()));
  }
}
