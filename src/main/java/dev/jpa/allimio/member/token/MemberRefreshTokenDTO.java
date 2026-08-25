package dev.jpa.allimio.member.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class MemberRefreshTokenDTO {
//1. [재발급 요청] Access Token 만료 시 Refresh Token을 담아 보낼 때
  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  public static class ReissueReq {
      private String refreshToken;
  }

  // 2. [응답] 새 토큰(Access Token 등)을 프론트로 전달할 때
  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  public static class Response {
      private String grantType;    // "Bearer"
      private String accessToken;
      private String refreshToken;
      private Long mno;
  }

}
