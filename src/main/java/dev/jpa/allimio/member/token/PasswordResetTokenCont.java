package dev.jpa.allimio.member.token;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/user/reset")
@RequiredArgsConstructor
public class PasswordResetTokenCont {
  private final PasswordResetTokenService tokenService;

  /**
   * 비밀번호 재설정 이메일 발송 
   * http://10.1.205.120:9102/v1/user/reset/email
   * @param req
   * @return
   */
  @PostMapping("/email")
  public ResponseEntity<?> sendMail(@RequestBody PasswordResetTokenDTO.EmailReq req) {
    try {
      tokenService.createResetToken(req.getId(), req.getEmail());
      return ResponseEntity.ok().body(Map.of("success", true, "message", "가입하신 이메일로 비밀번호 재설정 링크가 발송되었습니다"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "메일 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
  }

  /**
   * 토큰 유효성 검사
   * http://10.1.205.120:9102/v1/user/reset/validate
   * @param token
   * @return
   */
  @GetMapping("/validate")
  public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
    try {
      tokenService.validateToken(token);
      return ResponseEntity.ok(Map.of("success", true, "message", "유효한 재설정 링크입니다."));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  /**
   * 비밀번호 변경
   * http://10.1.205.120:9102/v1/user/reset/password
   * @param req
   * @return
   */
  @PostMapping("/password")
  public ResponseEntity<?> confirmReset(@RequestBody PasswordResetTokenDTO.ResetReq req) {
    try {
      tokenService.resetPassword(req.getToken(), req.getNewPassword());
      return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해 주세요."));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "비밀번호 변경 처리 중 오류가 발생했습니다."));
    }
  }

}
