package dev.jpa.allimio.qa.reset;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.qa.Qa;
import dev.jpa.allimio.qa.QaAccessException;
import dev.jpa.allimio.qa.QaGuestTokenProvider;
import dev.jpa.allimio.qa.QaRepository;
import dev.jpa.allimio.qa.mail.QaMailService;
import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   비회원 문의글 비밀번호 재설정

   비회원은 게시글 비밀번호를 잊으면 자기 글을 열거나 고칠 방법이 없어서,
   글을 쓸 때 입력한 이메일로 본인 확인을 하고 비밀번호를 새로 정하게 합니다.

   1) 요청  POST /qa/guest/{no}/reset/request
      - 글에 저장된 이메일로 6자리 인증번호 발송 (이메일은 te**@example.com 처럼 가려서 알려 줌)
      - 인증번호는 BCrypt로 암호화해 Redis에 10분 보관
      - 1분에 한 번, 같은 글로 하루 5번까지만 요청 가능 (메일을 계속 보내게 만드는 것 방지)
   2) 확인  POST /qa/guest/{no}/reset/confirm  { code, pw }
      - 인증번호가 맞으면 게시글 비밀번호 변경, 인증번호 삭제(1회용), 비밀번호 5회 실패 잠금 해제
      - 5번 틀리면 인증번호가 무효가 되어 새로 받아야 함

   회원 글은 대상이 아닙니다(로그인으로 본인 확인, 게시글 비밀번호 없음).
   비회원 경로라 401/403은 쓰지 않습니다(QaAccessException 설명 참고).
--------------------------------------------------------------------- */
@Service
@RequiredArgsConstructor
public class QaResetService {

  /** 인증번호 유효 시간 */
  public static final int CODE_EXPIRE_MINUTES = 10;
  /** 인증번호를 틀릴 수 있는 횟수 */
  private static final int MAX_FAIL = 5;
  /** 같은 글로 하루에 요청할 수 있는 횟수 */
  private static final int MAX_REQUEST_PER_DAY = 5;
  /** 다시 요청하기까지 기다리는 시간(초) */
  private static final int REQUEST_WAIT_SECONDS = 60;
  /** 새 비밀번호 최소 길이 */
  private static final int PW_MIN_LENGTH = 4;

  private static final SecureRandom RANDOM = new SecureRandom();

  private final QaRepository qaRepository;
  private final QaResetStore store;
  private final QaMailService qaMailService;
  private final PasswordEncoder pwEncoder;
  private final QaGuestTokenProvider guestTokenProvider;

  /**
   * 인증번호 요청
   * @return 가려진 이메일 (예: te**@example.com)
   */
  public String request(Long no) {
    Qa qa = findGuestPost(no);

    String email = qa.getGuestEmail();
    if (email == null || email.isBlank()) {
      throw new QaAccessException(400, "NO_EMAIL", "이 글에는 등록된 이메일이 없어 비밀번호를 재설정할 수 없습니다.");
    }

    long wait = store.waitSecondsLeft(no);
    if (wait > 0) {
      throw new QaAccessException(429, "TOO_SOON", wait + "초 후에 다시 요청해 주세요.");
    }
    if (store.increaseTodayRequest(no) > MAX_REQUEST_PER_DAY) {
      throw new QaAccessException(429, "TOO_MANY",
          "오늘은 더 요청할 수 없습니다. (하루 " + MAX_REQUEST_PER_DAY + "회) 내일 다시 시도해 주세요.");
    }

    String code = String.format("%06d", RANDOM.nextInt(1_000_000));
    store.saveCode(no, pwEncoder.encode(code), Duration.ofMinutes(CODE_EXPIRE_MINUTES));
    store.startWait(no, Duration.ofSeconds(REQUEST_WAIT_SECONDS));

    qaMailService.sendResetCode(no, email.trim(), code, CODE_EXPIRE_MINUTES); // 백그라운드 발송 + 이력 기록
    return maskEmail(email.trim());
  }

  /** 인증번호 확인 + 새 비밀번호로 변경 */
  @Transactional
  public void confirm(Long no, String code, String newPw) {
    Qa qa = findGuestPost(no);

    if (newPw == null || newPw.isBlank() || newPw.length() < PW_MIN_LENGTH) {
      throw new QaAccessException(400, "PW_INVALID", "새 비밀번호는 " + PW_MIN_LENGTH + "자 이상 입력해 주세요.");
    }

    String saved = store.findCode(no);
    if (saved == null) {
      throw new QaAccessException(400, "CODE_EXPIRED", "인증번호가 만료되었습니다. 인증번호를 다시 받아 주세요.");
    }

    if (code == null || !pwEncoder.matches(code.trim(), saved)) {
      long failed = store.increaseFail(no, Duration.ofMinutes(CODE_EXPIRE_MINUTES));
      if (failed >= MAX_FAIL) {
        store.clearCode(no);
        throw new QaAccessException(400, "CODE_EXPIRED",
            "인증번호를 " + MAX_FAIL + "회 잘못 입력했습니다. 인증번호를 다시 받아 주세요.");
      }
      throw new QaAccessException(400, "CODE_MISMATCH",
          "인증번호가 일치하지 않습니다. (남은 횟수 " + (MAX_FAIL - failed) + "회)");
    }

    qa.changePw(pwEncoder.encode(newPw));
    store.clearCode(no);              // 1회용
    guestTokenProvider.clearFail(no); // 비밀번호 5회 실패 잠금도 해제
  }

  /** 삭제되지 않은 비회원 1:1 문의글만 */
  private Qa findGuestPost(Long no) {
    Qa qa = qaRepository.findByNoAndIsdel(no, "N").orElseThrow(QaAccessException::notFound);
    if ("Y".equals(qa.getIsfaq())) {
      throw QaAccessException.notFound();
    }
    if (qa.getMno() != null) {
      throw new QaAccessException(400, "FORBIDDEN", "회원 문의글은 로그인 후 이용해 주세요.");
    }
    return qa;
  }

  /** te**@example.com — 앞 두 글자만 보이게 */
  static String maskEmail(String email) {
    int at = email.indexOf('@');
    if (at <= 0) {
      return "**";
    }
    String id = email.substring(0, at);
    int shown = Math.min(2, Math.max(1, id.length() - 1));
    return id.substring(0, shown) + "*".repeat(Math.max(2, id.length() - shown)) + email.substring(at);
  }
}
