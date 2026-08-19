package dev.jpa.allimio.surveyanalysis;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 설문 전체 AI 분석 결과 Entity.
 *
 * 특정 설문에 등록된 전체 응답을 H200 LLM으로 분석한 결과를 저장한다.
 */
@Entity
@Table(name = "SURVEYANALYSIS")
public class SurveyAnalysis {

    /**
     * AI 분석 결과 번호(PK).
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "surveyanalysis_seq"
    )
    @SequenceGenerator(
            name = "surveyanalysis_seq",
            sequenceName = "SEQ_SURVEYANALYSIS_NO",
            allocationSize = 1
    )
    @Column(name = "NO")
    private Long no;


    /**
     * 분석 대상 설문번호.
     *
     * SURVEY.NO를 참조한다.
     * 설문 하나당 분석 결과는 한 건만 저장한다.
     */
    @Column(name = "SVNO", nullable = false, unique = true)
    private Long surveyNo;


    /**
     * 설문 전체 종합 평가점수.
     * 1.0 ~ 5.0
     */
    @Column(name = "AISCORE", precision = 2, scale = 1)
    private BigDecimal aiScore;


    /**
     * 긍정 응답 비율(%).
     */
    @Column(name = "POSITIVE_RATE", precision = 5, scale = 2)
    private BigDecimal positiveRate;


    /**
     * 중립 응답 비율(%).
     */
    @Column(name = "NEUTRAL_RATE", precision = 5, scale = 2)
    private BigDecimal neutralRate;


    /**
     * 부정 응답 비율(%).
     */
    @Column(name = "NEGATIVE_RATE", precision = 5, scale = 2)
    private BigDecimal negativeRate;


    /**
     * 전체 설문 응답 AI 종합 요약.
     */
    @Column(name = "SUMMARY", columnDefinition = "CLOB")
    private String summary;


    /**
     * 주요 긍정 의견 요약.
     */
    @Column(name = "POSITIVE_SUMMARY", columnDefinition = "CLOB")
    private String positiveSummary;


    /**
     * 주요 부정 의견 요약.
     */
    @Column(name = "NEGATIVE_SUMMARY", columnDefinition = "CLOB")
    private String negativeSummary;


    /**
     * AI 분석일.
     */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;


    public SurveyAnalysis() {
    }


    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }


    public Long getSurveyNo() {
        return surveyNo;
    }

    public void setSurveyNo(Long surveyNo) {
        this.surveyNo = surveyNo;
    }


    public BigDecimal getAiScore() {
      return aiScore;
  }

  public void setAiScore(BigDecimal aiScore) {
      this.aiScore = aiScore;
  }


    public BigDecimal getPositiveRate() {
        return positiveRate;
    }

    public void setPositiveRate(BigDecimal positiveRate) {
        this.positiveRate = positiveRate;
    }


    public BigDecimal getNeutralRate() {
        return neutralRate;
    }

    public void setNeutralRate(BigDecimal neutralRate) {
        this.neutralRate = neutralRate;
    }


    public BigDecimal getNegativeRate() {
        return negativeRate;
    }

    public void setNegativeRate(BigDecimal negativeRate) {
        this.negativeRate = negativeRate;
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }


    public String getPositiveSummary() {
        return positiveSummary;
    }

    public void setPositiveSummary(String positiveSummary) {
        this.positiveSummary = positiveSummary;
    }


    public String getNegativeSummary() {
        return negativeSummary;
    }

    public void setNegativeSummary(String negativeSummary) {
        this.negativeSummary = negativeSummary;
    }


    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }
}