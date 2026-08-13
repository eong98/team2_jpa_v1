package dev.jpa.allimio.surveyanalysis;

import java.math.BigDecimal;

/**
 * 설문 전체 AI 분석 결과 DTO.
 *
 * H200 LLM이 분석한 설문 전체 결과를
 * 백엔드와 프론트엔드 사이에서 전달한다.
 */
public class SurveyAnalysisDTO {

    /**
     * AI 분석 결과 번호.
     */
    private Long no;

    /**
     * 분석 대상 설문번호.
     */
    private Long surveyNo;

    /**
     * 설문 전체 종합 평가점수.
     * 1.0 ~ 5.0
     */
    private BigDecimal aiScore;

    /**
     * 긍정 응답 비율(%).
     */
    private BigDecimal positiveRate;

    /**
     * 중립 응답 비율(%).
     */
    private BigDecimal neutralRate;

    /**
     * 부정 응답 비율(%).
     */
    private BigDecimal negativeRate;

    /**
     * 전체 설문 응답 AI 종합 요약.
     */
    private String summary;

    /**
     * 주요 긍정 의견 요약.
     */
    private String positiveSummary;

    /**
     * 주요 부정 의견 요약.
     */
    private String negativeSummary;

    /**
     * AI 분석일.
     */
    private String cdate;


    public SurveyAnalysisDTO() {
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