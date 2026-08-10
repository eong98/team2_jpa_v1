package dev.jpa.alllimio.surveyanswer;

import java.math.BigDecimal;

/**
 * 문항별 실제 답변 등록·조회 DTO.
 */
public class SurveyAnswerDTO {

    private Long no;
    private Long responseNo;
    private Long questionNo;
    private String qtext;
    private String qtype;
    private String atext;
    private BigDecimal evalScore;
    private String cdate;

    public SurveyAnswerDTO() {
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public Long getResponseNo() {
        return responseNo;
    }

    public void setResponseNo(Long responseNo) {
        this.responseNo = responseNo;
    }

    public Long getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(Long questionNo) {
        this.questionNo = questionNo;
    }

    public String getQtext() {
        return qtext;
    }

    public void setQtext(String qtext) {
        this.qtext = qtext;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public String getAtext() {
        return atext;
    }

    public void setAtext(String atext) {
        this.atext = atext;
    }

    public BigDecimal getEvalScore() {
        return evalScore;
    }

    public void setEvalScore(BigDecimal evalScore) {
        this.evalScore = evalScore;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }
}
