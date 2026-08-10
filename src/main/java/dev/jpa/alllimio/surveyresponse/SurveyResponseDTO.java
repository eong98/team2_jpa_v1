package dev.jpa.alllimio.surveyresponse;

import java.util.ArrayList;
import java.util.List;

import dev.jpa.alllimio.surveyanswer.SurveyAnswerDTO;

/**
 * 점주 설문 제출 및 관리자 응답 조회 DTO.
 */
public class SurveyResponseDTO {

    private Long no;
    private Long surveyNo;
    private Long memberNo;
    private String checkYn;
    private String checkDate;
    private String cdate;
    private List<SurveyAnswerDTO> answers = new ArrayList<>();

    public SurveyResponseDTO() {
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

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getCheckYn() {
        return checkYn;
    }

    public void setCheckYn(String checkYn) {
        this.checkYn = checkYn;
    }

    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }

    public List<SurveyAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SurveyAnswerDTO> answers) {
        this.answers = answers;
    }
}
