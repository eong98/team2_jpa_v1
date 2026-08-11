package dev.jpa.allimio.survey;

import java.util.ArrayList;
import java.util.List;

import dev.jpa.allimio.surveyquestion.SurveyQuestionDTO;

/**
 * 설문 등록·수정·조회에 사용하는 DTO.
 *
 * <p>관리자가 설문 기본정보와 문항 목록을 한 번에 전송할 수 있도록
 * questions 필드에 문항 DTO 목록을 포함한다.</p>
 */
public class SurveyDTO {

    private Long no;
    private Long memberNo;
    private String title;
    private String detail;
    private String startDate;
    private String endDate;
    private String cdate;
    private List<SurveyQuestionDTO> questions = new ArrayList<>();

    public SurveyDTO() {
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }

    public List<SurveyQuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SurveyQuestionDTO> questions) {
        this.questions = questions;
    }
}
