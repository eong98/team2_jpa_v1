package dev.jpa.allimio.member.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class PasswordResetTokenDTO {
  
//1. [화면 1] 비밀번호 재설정 이메일 요청
  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  public static class EmailReq {
      private String email;
      private String id;
  }

  // 2. [화면 2] 링크 클릭 후 새 비밀번호 변경 요청
  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  public static class ResetReq {
      private String token;
      private String newPassword;
  }

}
