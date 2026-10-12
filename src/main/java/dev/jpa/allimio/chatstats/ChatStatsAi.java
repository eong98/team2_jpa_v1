package dev.jpa.allimio.chatstats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챗봇 상담 만족도 AI 분석 결과 (CHAT_STATS_AI)
 * 분석 기간(7/30/90일)마다 한 줄 — [AI 분석]을 다시 누르면 그 기간의 결과를 덮어씁니다.
 */
@Entity
@Table(name = "CHAT_STATS_AI")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatStatsAi {
  /** 분석 결과 번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_stats_ai_seq_use")
  @SequenceGenerator(name = "chat_stats_ai_seq_use", sequenceName = "CHAT_STATS_AI_SEQ", allocationSize = 1)
  private Long no;

  /** 분석 기간(일) — 7 | 30 | 90 */
  private int days;

  /** 분석 시작일 yyyy-MM-dd */
  @Column(name = "FROM_DATE")
  private String fromDate;

  /** 분석 종료일 yyyy-MM-dd */
  @Column(name = "TO_DATE")
  private String toDate;

  /** 분석에 쓴 만족도 응답 수 */
  private long answered;

  /** 감정 점수 0(매우 부정) ~ 10(매우 긍정) */
  private double score;

  /** AI 요약 */
  @Lob
  private String summary;

  /** 점수 근거 */
  private String reason;

  /** 개선할 점 (JSON 배열 문자열) */
  @Lob
  @Column(name = "WEAK_POINTS")
  private String weakPoints;

  /** 분석을 실행한 관리자 번호 */
  private Long ano;

  /** 분석 일시 yyyy-MM-dd HH:mm:ss */
  private String rdate;

  /** 같은 기간을 다시 분석했을 때 결과 덮어쓰기 */
  public void overwrite(String fromDate, String toDate, long answered, double score, String summary,
      String reason, String weakPoints, Long ano, String rdate) {
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.answered = answered;
    this.score = score;
    this.summary = summary;
    this.reason = reason;
    this.weakPoints = weakPoints;
    this.ano = ano;
    this.rdate = rdate;
  }
}
