package dev.jpa.alllimio.surveyquestion;

/**
 * 설문 문항 등록·수정·조회 DTO.
 */
public class SurveyQuestionDTO {

    private Long no;
    private Long surveyNo;
    private String qtext;
    private String qtype;
    private String qoptions;
    private String requiredYn;
    private Integer seqNo;
    private String cdate;

    public SurveyQuestionDTO() {
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

    public String getQoptions() {
        return qoptions;
    }

    public void setQoptions(String qoptions) {
        this.qoptions = qoptions;
    }

    public String getRequiredYn() {
        return requiredYn;
    }

    public void setRequiredYn(String requiredYn) {
        this.requiredYn = requiredYn;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }
}
