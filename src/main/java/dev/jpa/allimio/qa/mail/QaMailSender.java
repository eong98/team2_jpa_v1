package dev.jpa.allimio.qa.mail;

import java.util.Properties;

import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

/* ---------------------------------------------------------------------
   문의 메일 전용 발송기

   회원 비밀번호 찾기 메일(tool/MailService)·CCTV 알림 메일과는 따로 둡니다.
   메일 계정 — 환경변수로 넣습니다 (application.properties는 건드리지 않음)
     QA_MAIL_USERNAME / QA_MAIL_PASSWORD   문의 메일 전용 계정 (Gmail이면 앱 비밀번호)
     QA_MAIL_HOST / QA_MAIL_PORT           생략하면 smtp.gmail.com / 587
     전용 계정이 없으면 공용 메일 설정(spring.mail.*, MAIL_USERNAME 등)을 그대로 씁니다.

   발송 객체는 여기서 따로 만들어 응답 대기 시간 제한을 겁니다.
   (공용 설정에는 제한이 없어, 메일 서버가 응답하지 않으면 끝없이 기다림)

   예외를 던지지 않고 결과(성공 여부 + 실패 사유)를 돌려줍니다 → 호출한 쪽에서 QA_SEND_LOG에 기록
--------------------------------------------------------------------- */
@Component
public class QaMailSender {

  /** 메일 서버 연결 / 응답 / 전송 대기 시간(ms) */
  private static final int CONNECT_TIMEOUT_MS = 10_000;
  private static final int READ_TIMEOUT_MS = 15_000;
  private static final int WRITE_TIMEOUT_MS = 15_000;

  /** 실패 사유로 남길 최대 길이 (QA_SEND_LOG.MSG VARCHAR2(500), 한글 3바이트) */
  private static final int MSG_MAX_LENGTH = 160;

  private final JavaMailSenderImpl mailSender;
  private final String from;

  /** 발송 결과 */
  public record Result(boolean success, String message) {
  }

  public QaMailSender(Environment env) {
    // 문의 메일 전용 계정(QA_MAIL_*)이 있으면 그것을 쓰고, 없으면 공용 메일 설정(spring.mail.*)을 씀
    String host = read(env, "qa.mail.host", read(env, "spring.mail.host", "smtp.gmail.com"));
    String username = read(env, "qa.mail.username", read(env, "spring.mail.username", ""));
    String password = read(env, "qa.mail.password", read(env, "spring.mail.password", ""));
    int port;
    try {
      port = Integer.parseInt(read(env, "qa.mail.port", read(env, "spring.mail.port", "587")));
    } catch (NumberFormatException e) {
      port = 587;
    }

    this.from = username.trim();

    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(port);
    sender.setUsername(username);
    sender.setPassword(password);
    sender.setDefaultEncoding("UTF-8");

    Properties props = sender.getJavaMailProperties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.starttls.required", "true");
    props.put("mail.smtp.connectiontimeout", String.valueOf(CONNECT_TIMEOUT_MS));
    props.put("mail.smtp.timeout", String.valueOf(READ_TIMEOUT_MS));
    props.put("mail.smtp.writetimeout", String.valueOf(WRITE_TIMEOUT_MS));
    this.mailSender = sender;
  }

  /**
   * 설정값 읽기 — 값이 환경변수(${MAIL_USERNAME} 등)인데 그 환경변수가 없으면 서버가 뜨지 못하므로
   * 그런 경우에는 기본값을 씁니다. (메일 계정이 없으면 발송할 때 "메일 계정 설정이 없습니다"로 실패 처리)
   */
  private static String read(Environment env, String key, String fallback) {
    try {
      String value = env.getProperty(key);
      return value == null || value.isBlank() ? fallback : value;
    } catch (RuntimeException e) {
      return fallback;
    }
  }

  /**
   * HTML 메일 발송
   *
   * @return 성공 여부와 실패 사유 (성공이면 message는 null)
   */
  public Result send(String toEmail, String subject, String html) {
    if (toEmail == null || toEmail.isBlank()) {
      return new Result(false, "받는 사람 이메일 주소가 없습니다.");
    }
    if (from.isBlank()) {
      return new Result(false, "메일 계정 설정이 없습니다. (QA_MAIL_USERNAME)");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from);
      helper.setTo(toEmail.trim());
      helper.setSubject(subject);
      helper.setText(html, true); // HTML
      mailSender.send(message);
      return new Result(true, null);

    } catch (Exception e) {
      String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
      reason = reason.replaceAll("\\s+", " ").trim();
      if (reason.length() > MSG_MAX_LENGTH) {
        reason = reason.substring(0, MSG_MAX_LENGTH);
      }
      return new Result(false, reason);
    }
  }
}
