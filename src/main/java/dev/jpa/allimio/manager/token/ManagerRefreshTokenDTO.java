package dev.jpa.allimio.manager.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class ManagerRefreshTokenDTO {
//1. [관리자 재발급 요청] Access Token 만료 시 Refresh Token을 담아 보낼 때
  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  public static class ReissueReq {
      private String refreshToken;
  }

  // 2. [응답] 관리자 새 토큰을 프론트로 전달할 때
  @Getter
  @Setter
  @NoArgsConstructor
  @ToString
  public static class Response {
      private String grantType;    // "Bearer"
      private String accessToken;
      private String refreshToken;
      private Long mnno;           // 관리자 번호
  }

}
