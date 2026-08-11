package dev.jpa.allimio.survey;

import java.util.ArrayList;
import java.util.List;

import dev.jpa.allimio.surveyquestion.SurveyQuestion;
import dev.jpa.allimio.surveyresponse.SurveyResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 설문 기본정보 엔티티.
 *
 * <p>관리자가 작성한 설문 제목, 설명, 기간을 저장한다.
 * MEMBER 테이블과의 외래키 컬럼 MNO는 회원 엔티티와 직접 연결하지 않고
 * 관리자 회원번호(Long)로 관리한다.</p>
 */
@Entity
@Table(name = "SURVEY")
public class Survey {

    /** 설문번호(PK). Oracle SURVEY_SEQ를 사용한다. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_seq_generator")
    @SequenceGenerator(
            name = "survey_seq_generator",
            sequenceName = "SURVEY_SEQ",
            allocationSize = 1)
    @Column(name = "NO")
    private Long no;

    /** 설문을 작성한 관리자 회원번호. MEMBER.NO를 참조한다. */
    @Column(name = "MNO", nullable = false)
    private Long memberNo;

    /** 설문제목. */
    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    /** 설문설명. */
    @Column(name = "DETAIL", length = 2000)
    private String detail;

    /** 설문시작일. 형식: yyyy-MM-dd HH:mm:ss */
    @Column(name = "STARTDATE", nullable = false, length = 30)
    private String startDate;

    /** 설문종료일. 형식: yyyy-MM-dd HH:mm:ss */
    @Column(name = "ENDDATE", nullable = false, length = 30)
    private String endDate;

    /** 등록일. 형식: yyyy-MM-dd HH:mm:ss */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;

    /**
     * 설문에 포함된 문항 목록.
     * 설문 저장·삭제 시 문항도 함께 처리한다.
     */
    @OneToMany(
            mappedBy = "survey",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("seqNo ASC, no ASC")
    private List<SurveyQuestion> questions = new ArrayList<>();

    /**
     * 점주별 응답 목록.
     * 설문 삭제 시 DB의 ON DELETE CASCADE와 함께 하위 응답도 정리된다.
     */
    @OneToMany(
            mappedBy = "survey",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<SurveyResponse> responses = new ArrayList<>();

    public Survey() {
    }

    /** 문항을 설문에 추가하고 양방향 관계를 맞춘다. */
    public void addQuestion(SurveyQuestion question) {
        questions.add(question);
        question.setSurvey(this);
    }

    /** 기존 문항을 제거하고 연관관계를 해제한다. */
    public void removeQuestion(SurveyQuestion question) {
        questions.remove(question);
        question.setSurvey(null);
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

    public List<SurveyQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SurveyQuestion> questions) {
        this.questions = questions;
    }

    public List<SurveyResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<SurveyResponse> responses) {
        this.responses = responses;
    }
}
