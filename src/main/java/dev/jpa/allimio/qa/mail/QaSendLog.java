package dev.jpa.allimio.qa.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문의사항 메일 발송 이력 (QA_SEND_LOG)
 *
 * 메일을 한 번 보낼 때마다 한 줄 — 다시 보내면 새 줄이 생기고 이전 줄은 그대로 남습니다.
 * 문의글을 영구 삭제해도 기록은 남기기 위해 QA에 FK를 걸지 않았습니다.
 */
@Entity
@Table(name = "QA_SEND_LOG")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QaSendLog {

  /** 답변 등록 알림 */
  public static final int STYPE_REPLY = 1;
  /** 비밀번호 재설정 인증번호 */
  public static final int STYPE_RESET = 2;

  /** 발송 이력 번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qa_send_log_seq_use")
  @SequenceGenerator(name = "qa_send_log_seq_use", sequenceName = "QA_SEND_LOG_SEQ", allocationSize = 1)
  private Long no;

  /** 문의글 번호 (QA.NO, FK 없음) */
  private Long qno;

  /** 받는 회원 번호 (비회원 글이면 null) */
  private Long mno;

  /** 받는 사람 이메일 */
  private String email;

  /** 1: 답변 등록 알림, 2: 비밀번호 재설정 인증번호 */
  private int stype;

  /** 메일 제목 */
  private String title;

  /** 메일 본문 (인증번호 메일은 저장하지 않음) */
  private String content;

  /** Y: 발송 성공, N: 실패 */
  @Column(name = "RESULT", columnDefinition = "CHAR(1)")
  private String result;

  /** 실패 사유 */
  private String msg;

  /** 발송 일시 yyyy-MM-dd HH:mm:ss */
  private String cdate;
}
