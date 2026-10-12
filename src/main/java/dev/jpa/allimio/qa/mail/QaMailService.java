package dev.jpa.allimio.qa.mail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.HtmlUtils;

import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.qa.Qa;
import dev.jpa.allimio.qa.QaAccessException;
import dev.jpa.allimio.qa.QaRepository;
import dev.jpa.allimio.tool.Tool;
import jakarta.annotation.PreDestroy;

/* ---------------------------------------------------------------------
   문의 메일 발송 + 발송 이력 기록

   보내는 메일은 두 가지입니다.
   1) 답변 등록 알림 — 관리자가 문의에 처음 답변을 등록했을 때 작성자에게 (AI 자동 답변에는 보내지 않음)
      본문에는 답변 내용을 넣지 않고 문의 제목과 확인 링크만 넣습니다 (비밀글 답변이 메일함에 남지 않게)
   2) 비밀번호 재설정 인증번호 — 비회원이 문의글 비밀번호 재설정을 요청했을 때 (qa.reset)

   메일 서버 응답이 몇 초씩 걸리므로 요청을 기다리게 하지 않고 별도 스레드에서 하나씩 보냅니다.
   메일이 실패해도 답변 등록·재설정 요청 자체는 정상 처리되고, 결과는 QA_SEND_LOG에 성공/실패로 남습니다.
   (관리자 화면 /dbms/qamail 에서 확인, 실패한 답변 알림은 [다시 보내기])
--------------------------------------------------------------------- */
@Service
public class QaMailService {

  private static final String REPLY_SUBJECT = "[Allimio] 문의하신 글에 답변이 등록되었습니다";
  private static final String RESET_SUBJECT = "[Allimio] 문의글 비밀번호 재설정 인증번호";

  private final QaRepository qaRepository;
  private final MemberRepository memberRepository;
  private final QaSendLogRepository sendLogRepository;
  private final QaMailSender mailSender;

  /** 메일 속 링크에 쓸 화면 주소 — 배포할 때 환경변수 QA_MAIL_FRONT_URL로 바꿈 */
  private final String frontUrl;

  /** 한 번에 하나씩 발송 */
  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
    Thread t = new Thread(r, "qa-mail");
    t.setDaemon(true);
    return t;
  });

  public QaMailService(QaRepository qaRepository, MemberRepository memberRepository,
      QaSendLogRepository sendLogRepository, QaMailSender mailSender,
      @Value("${qa.mail.front-url:http://localhost:5174}") String frontUrl) {
    this.qaRepository = qaRepository;
    this.memberRepository = memberRepository;
    this.sendLogRepository = sendLogRepository;
    this.mailSender = mailSender;
    this.frontUrl = frontUrl == null ? "" : frontUrl.replaceAll("/+$", "");
  }

  // ==========================================
  // 1) 답변 등록 알림
  // ==========================================

  /** 답변 저장 트랜잭션이 커밋된 뒤 알림 메일 발송 (커밋 전에 보내면 롤백돼도 메일이 나감) */
  public void sendReplyNoticeAfterCommit(Long qno) {
    if (qno == null) {
      return;
    }
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          executor.submit(() -> sendReplyNotice(qno));
        }
      });
    } else {
      executor.submit(() -> sendReplyNotice(qno));
    }
  }

  /**
   * [관리자] 실패한 답변 알림 다시 보내기 — 새 발송 이력이 한 줄 추가됨
   *
   * @param logNo 다시 보낼 발송 이력 번호
   */
  public void resend(Long logNo) {
    QaSendLog log = sendLogRepository.findById(logNo)
        .orElseThrow(() -> new QaAccessException(404, "NOT_FOUND", "발송 기록을 찾을 수 없습니다."));
    if (log.getStype() != QaSendLog.STYPE_REPLY) {
      throw new QaAccessException(400, "NOT_ALLOWED", "답변 알림 메일만 다시 보낼 수 있습니다.");
    }
    if ("Y".equals(log.getResult())) {
      throw new QaAccessException(400, "NOT_ALLOWED", "이미 발송에 성공한 메일입니다.");
    }
    if (sendLogRepository.existsByQnoAndStypeAndResultAndNoGreaterThan(
        log.getQno(), QaSendLog.STYPE_REPLY, "Y", log.getNo())) {
      throw new QaAccessException(400, "NOT_ALLOWED", "이미 다시 보내서 발송에 성공했습니다.");
    }
    Qa qa = qaRepository.findByNoAndIsdel(log.getQno(), "N")
        .orElseThrow(() -> new QaAccessException(400, "NOT_ALLOWED", "삭제된 게시글이라 다시 보낼 수 없습니다."));
    if (qa.getAnswer() == null || qa.getAnswer().isBlank()) {
      throw new QaAccessException(400, "NOT_ALLOWED", "답변이 등록되지 않은 문의입니다.");
    }
    executor.submit(() -> sendReplyNotice(log.getQno()));
  }

  private void sendReplyNotice(Long qno) {
    try {
      Qa qa = qaRepository.findByNoAndIsdel(qno, "N").orElse(null);
      if (qa == null) {
        return; // 그 사이 삭제됨
      }

      // 받는 사람 — 회원 글은 지금 회원정보의 이메일, 비회원 글은 작성할 때 입력한 이메일
      String email = qa.getGuestEmail();
      if (qa.getMno() != null) {
        Member member = memberRepository.findById(qa.getMno()).orElse(null);
        if (member != null && member.getEmail() != null && !member.getEmail().isBlank()) {
          email = member.getEmail();
        }
      }

      String link = frontUrl + (qa.getMno() != null ? "/user/qa/" : "/board/qa/") + qno;
      String html = "<div style='font-family: Arial, sans-serif; padding: 20px; line-height: 1.7;'>"
          + "<h2 style='margin: 0 0 12px;'>문의하신 글에 답변이 등록되었습니다</h2>"
          + "<p style='margin: 0 0 16px;'>문의 제목: <b>" + HtmlUtils.htmlEscape(nvl(qa.getTitle())) + "</b></p>"
          + "<a href='" + link + "' style='display:inline-block; padding:10px 20px; background-color:#16a34a;"
          + " color:#fff; text-decoration:none; border-radius:5px;'>답변 확인하기</a>"
          + "<p style='margin: 16px 0 0; color:#888; font-size:12px;'>"
          + (qa.getMno() != null ? "로그인 후 확인하실 수 있습니다." : "비밀글은 작성 시 입력한 비밀번호로 확인하실 수 있습니다.")
          + "</p></div>";
      String content = "문의하신 글에 답변이 등록되었습니다.\n문의 제목: " + nvl(qa.getTitle()) + "\n답변 확인: " + link;

      QaMailSender.Result result = mailSender.send(email, REPLY_SUBJECT, html);
      saveLog(qno, qa.getMno(), email, QaSendLog.STYPE_REPLY, REPLY_SUBJECT, content, result);

    } catch (Exception e) {
      System.out.println("⚠ 문의 답변 알림 메일 처리 실패(no=" + qno + "): " + e.getMessage());
    }
  }

  // ==========================================
  // 2) 비밀번호 재설정 인증번호
  // ==========================================

  /**
   * 인증번호 메일 발송 (백그라운드) — 이력에는 "인증 메일을 보냈다"는 사실만 남기고 인증번호·본문은 저장하지 않음
   */
  public void sendResetCode(Long qno, String email, String code, int expireMinutes) {
    executor.submit(() -> {
      try {
        String html = "<div style='font-family: Arial, sans-serif; padding: 20px; line-height: 1.7;'>"
            + "<h2 style='margin: 0 0 12px;'>문의글 비밀번호 재설정</h2>"
            + "<p style='margin: 0 0 12px;'>아래 인증번호를 화면에 입력하고 새 비밀번호를 설정해 주세요. (유효시간 " + expireMinutes + "분)</p>"
            + "<p style='margin: 0; font-size: 28px; font-weight: bold; letter-spacing: 6px;'>" + code + "</p>"
            + "<p style='margin: 16px 0 0; color:#888; font-size:12px;'>요청하신 적이 없다면 이 메일은 무시하셔도 됩니다.</p>"
            + "</div>";
        QaMailSender.Result result = mailSender.send(email, RESET_SUBJECT, html);
        saveLog(qno, null, email, QaSendLog.STYPE_RESET, RESET_SUBJECT, null, result);
      } catch (Exception e) {
        System.out.println("⚠ 문의글 비밀번호 재설정 메일 처리 실패(no=" + qno + "): " + e.getMessage());
      }
    });
  }

  // ==========================================
  // 공통
  // ==========================================

  private void saveLog(Long qno, Long mno, String email, int stype, String title, String content,
      QaMailSender.Result result) {
    sendLogRepository.save(QaSendLog.builder()
        .qno(qno)
        .mno(mno)
        .email(email == null || email.isBlank() ? "-" : email.trim()) // EMAIL NOT NULL
        .stype(stype)
        .title(title)
        .content(content)
        .result(result.success() ? "Y" : "N")
        .msg(result.message())
        .cdate(Tool.getDate())
        .build());
    System.out.println("-> 문의 메일 " + (result.success() ? "발송" : "발송 실패") + "(no=" + qno + ", 종류=" + stype + ")"
        + (result.success() ? "" : ": " + result.message()));
  }

  private String nvl(String s) {
    return s == null ? "" : s;
  }

  @PreDestroy
  public void shutdown() {
    executor.shutdown(); // 보내던 메일은 마저 보냄
  }
}
