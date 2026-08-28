package dev.jpa.allimio.tool;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
  
  private final JavaMailSender mailSender;
  
  public void sendPasswordResetMail(String toEmail, String token) {
    String subject = "[all-im-io] 비밀번호 재설정 안내";
    String frontendUrl = "http://10.1.205.109:9102";
    
    // token에 특수문자가 섞였을때 깨지지않게 인코딩
    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
    String resetUrl = frontendUrl + "/reset/password?token=" + token;
    
    String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
        + "<h2>비밀번호 재설정 안내</h2>"
        + "<p>아래 버튼을 클릭하여 새 비밀번호를 설정해 주세요. (유효시간: 30분)</p>"
        + "<a href='" + resetUrl + "' style='display:inline-block; padding:10px 20px; background-color:#007bff; color:#fff; text-decoration:none; border-radius:5px;'>비밀번호 재설정하기</a>"
        + "</div>";
    
    try {
      MimeMessage message = mailSender.createMimeMessage();
      // true: multipart 메시지로 생성 (첨부파일 등 가능), "UTF-8": 한글 깨짐 방지
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // 두 번째 인자 true = HTML로 해석

      mailSender.send(message);
    } catch (MessagingException e) {
      log.error("비밀번호 재설정 메일 발송 실패: {}", toEmail, e);
      // 필요하면 여기서 커스텀 예외로 던져서 컨트롤러/서비스 상위에서 처리하세요
      throw new RuntimeException("메일 발송에 실패했습니다.", e);
    }
  }
  
  /**
   * CCTV 이슈 알림 이메일 발송
   */
  public void sendNotificationMail(
      String toEmail,
      String title,
      String content
  ) {
      String subject = "[all-im-io] " + title;

      String htmlContent =
          "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
          + "<h2>" + title + "</h2>"
          + "<p>" + content + "</p>"
          + "</div>";

      try {
          MimeMessage message = mailSender.createMimeMessage();
          MimeMessageHelper helper =
              new MimeMessageHelper(message, true, "UTF-8");

          helper.setTo(toEmail);
          helper.setSubject(subject);
          helper.setText(htmlContent, true);

          mailSender.send(message);

      } catch (MessagingException e) {
          log.error("이슈 알림 메일 발송 실패: {}", toEmail, e);
          throw new RuntimeException("알림 메일 발송에 실패했습니다.", e);
      }
  }
}
