package dev.jpa.allimio.qa.reset;

import java.util.Map;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.qa.QaAccessException;
import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   비회원 문의글 비밀번호 재설정 API (로그인 없이 호출 — SecurityConfig에서 허용)

   POST /qa/guest/{no}/reset/request            → { success, email(가려진 주소), expiresIn(초) }
   POST /qa/guest/{no}/reset/confirm {code, pw} → { success, message }
   오류: 400/404/429 + { success:false, code, message }
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/qa/guest/{no}/reset")
@RequiredArgsConstructor
public class QaResetCont {

  private final QaResetService qaResetService;

  /**
   * 인증번호 요청 — 글에 저장된 이메일로 6자리 인증번호 발송
   * POST http://localhost:9102/qa/guest/70/reset/request
   */
  @PostMapping("/request")
  public ResponseEntity<Map<String, Object>> request(@PathVariable("no") Long no) {
    String maskedEmail = qaResetService.request(no);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "email", maskedEmail,
        "expiresIn", QaResetService.CODE_EXPIRE_MINUTES * 60));
  }

  /**
   * 인증번호 확인 + 새 비밀번호로 변경
   * POST http://localhost:9102/qa/guest/70/reset/confirm  { "code": "123456", "pw": "새비밀번호" }
   */
  @PostMapping("/confirm")
  public ResponseEntity<Map<String, Object>> confirm(
      @PathVariable("no") Long no,
      @RequestBody Map<String, String> body) {
    qaResetService.confirm(no, body.get("code"), body.get("pw"));
    return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 변경되었습니다. 새 비밀번호로 확인해 주세요."));
  }

  @ExceptionHandler(QaAccessException.class)
  public ResponseEntity<Map<String, Object>> handleAccess(QaAccessException e) {
    return ResponseEntity.status(e.getStatus())
        .body(Map.of("success", false, "code", e.getCode(), "message", e.getMessage()));
  }

  /** 인증번호 보관소(Redis)에 연결하지 못함 */
  @ExceptionHandler(RedisConnectionFailureException.class)
  public ResponseEntity<Map<String, Object>> handleRedisDown(RedisConnectionFailureException e) {
    System.out.println("⚠ 문의글 비밀번호 재설정: Redis 연결 실패 - " + e.getMessage());
    return ResponseEntity.status(400)
        .body(Map.of("success", false, "code", "UNAVAILABLE", "message", "지금은 비밀번호 재설정을 이용할 수 없습니다. 잠시 후 다시 시도해 주세요."));
  }
}
