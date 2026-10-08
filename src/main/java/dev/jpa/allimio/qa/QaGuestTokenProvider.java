package dev.jpa.allimio.qa;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/* ---------------------------------------------------------------------
   비회원 문의글 임시 토큰 + 비밀번호 실패 잠금.

   비회원은 로그인 JWT가 없으므로, 게시글 비밀번호를 확인하면 "그 글 하나"에만
   쓸 수 있는 10분짜리 토큰을 발급합니다. 화면은 sessionStorage에 보관했다가
   X-Qa-Token 헤더로 보내 상세 조회·수정·삭제에 사용합니다.
   (예전처럼 비밀번호를 화면 이동(location.state)으로 넘기거나 매번 다시 보내지 않음)

   - 서명 키는 로그인 JWT와 별도 (qa.guest-token.secret, 없거나 32바이트 미만이면
     서버 시작 때 무작위 생성 → 서버 재시작 시 발급된 토큰은 모두 만료)
   - 비밀번호 5회 연속 실패 시 그 글은 10분 잠금 (서버 메모리 보관 → 재시작 시 초기화)
--------------------------------------------------------------------- */
@Component
public class QaGuestTokenProvider {

  /** 토큰 유효시간 10분 */
  public static final long TOKEN_EXPIRE_MILLIS = 10 * 60_000L;
  /** 연속 실패 허용 횟수 */
  public static final int MAX_FAIL = 5;
  /** 잠금 시간 10분 */
  public static final long LOCK_MILLIS = 10 * 60_000L;

  /** 로그인 토큰과 섞이지 않게 구분값 */
  private static final String TOKEN_TYPE = "qa-guest";

  private final SecretKey secretKey;

  /** 글번호 → 실패 횟수/잠금 해제 시각 */
  private final Map<Long, FailState> fails = new ConcurrentHashMap<>();

  private record FailState(int count, long lockedUntil) {}

  public QaGuestTokenProvider(@Value("${qa.guest-token.secret:}") String secret) {
    byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      keyBytes = new byte[32];
      new SecureRandom().nextBytes(keyBytes);
    }
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  /** 글번호 전용 토큰 발급 */
  public String issue(Long no) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(String.valueOf(no))
        .claim("typ", TOKEN_TYPE)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + TOKEN_EXPIRE_MILLIS))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  /** 토큰이 이 글번호용으로 발급됐고 아직 유효한지 */
  public boolean isValid(String token, Long no) {
    if (token == null || token.isBlank() || no == null) {
      return false;
    }
    try {
      Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
          .parseClaimsJws(token).getBody();
      return TOKEN_TYPE.equals(claims.get("typ", String.class))
          && String.valueOf(no).equals(claims.getSubject());
    } catch (JwtException | IllegalArgumentException e) {
      return false; // 위변조·만료·형식 오류
    }
  }

  /** 잠금 남은 시간(초), 잠겨 있지 않으면 0 */
  public long lockedSecondsLeft(Long no) {
    FailState state = fails.get(no);
    if (state == null || state.lockedUntil() == 0) {
      return 0;
    }
    long left = state.lockedUntil() - System.currentTimeMillis();
    if (left <= 0) {
      fails.remove(no); // 잠금 시간이 지나면 횟수도 초기화
      return 0;
    }
    return (left + 999) / 1000;
  }

  /**
   * 비밀번호 실패 기록
   * @return 남은 시도 횟수 (0이면 방금 잠김)
   */
  public int recordFail(Long no) {
    FailState state = fails.compute(no, (k, prev) -> {
      int count = (prev == null ? 0 : prev.count()) + 1;
      long lockedUntil = count >= MAX_FAIL ? System.currentTimeMillis() + LOCK_MILLIS : 0;
      return new FailState(count, lockedUntil);
    });
    return Math.max(0, MAX_FAIL - state.count());
  }

  /** 비밀번호 성공 시 실패 횟수 초기화 */
  public void clearFail(Long no) {
    fails.remove(no);
  }
}
