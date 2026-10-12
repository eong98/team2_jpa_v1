package dev.jpa.allimio.qa.reset;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   비회원 문의글 비밀번호 재설정 — 인증번호 보관소 (Redis)

   인증번호는 잠깐 있다 사라지는 값이라 DB 테이블 대신 Redis에 만료 시간을 걸어 둡니다.
   로그인 토큰(RT:…)과 같은 Redis를 쓰지만 키 접두어가 달라 섞이지 않습니다.

   QA:RESET:CODE:{글번호}          인증번호(BCrypt)        10분
   QA:RESET:FAIL:{글번호}          인증번호 틀린 횟수      인증번호와 같이 만료
   QA:RESET:WAIT:{글번호}          다시 요청 대기          1분
   QA:RESET:REQ:{글번호}:{날짜}    그날 요청 횟수          다음 날 자정 이후 만료
--------------------------------------------------------------------- */
@Component
@RequiredArgsConstructor
public class QaResetStore {

  private static final String PREFIX = "QA:RESET:";

  private final StringRedisTemplate redis;

  // ---- 인증번호 ----

  public void saveCode(Long qno, String encodedCode, Duration ttl) {
    redis.opsForValue().set(PREFIX + "CODE:" + qno, encodedCode, ttl);
    redis.delete(PREFIX + "FAIL:" + qno); // 새 인증번호를 받으면 틀린 횟수는 처음부터
  }

  /** 저장된 인증번호(BCrypt) — 없거나 만료됐으면 null */
  public String findCode(Long qno) {
    return redis.opsForValue().get(PREFIX + "CODE:" + qno);
  }

  /** 틀린 횟수 +1 → 지금까지 틀린 횟수 */
  public long increaseFail(Long qno, Duration ttl) {
    String key = PREFIX + "FAIL:" + qno;
    Long count = redis.opsForValue().increment(key);
    redis.expire(key, ttl);
    return count == null ? 1 : count;
  }

  /** 인증번호와 틀린 횟수 삭제 (인증 성공·횟수 초과) */
  public void clearCode(Long qno) {
    redis.delete(PREFIX + "CODE:" + qno);
    redis.delete(PREFIX + "FAIL:" + qno);
  }

  // ---- 요청 제한 ----

  /** 다시 요청할 수 있을 때까지 남은 초 (바로 가능하면 0) */
  public long waitSecondsLeft(Long qno) {
    Long seconds = redis.getExpire(PREFIX + "WAIT:" + qno);
    return seconds == null || seconds < 0 ? 0 : seconds;
  }

  public void startWait(Long qno, Duration ttl) {
    redis.opsForValue().set(PREFIX + "WAIT:" + qno, "1", ttl);
  }

  /** 오늘 요청 횟수 +1 → 오늘 요청한 횟수 */
  public long increaseTodayRequest(Long qno) {
    String key = PREFIX + "REQ:" + qno + ":" + LocalDate.now();
    Long count = redis.opsForValue().increment(key);
    redis.expire(key, Duration.ofHours(25)); // 날짜가 키에 들어 있어 다음 날에는 새 키로 셈
    return count == null ? 1 : count;
  }
}
