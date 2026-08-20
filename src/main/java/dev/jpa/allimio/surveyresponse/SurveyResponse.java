package dev.jpa.allimio.surveyresponse;

import java.util.ArrayList;
import java.util.List;

import dev.jpa.allimio.survey.Survey;
import dev.jpa.allimio.surveyanswer.SurveyAnswer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 점주별 설문 응답 엔티티.
 *
 * <p>
 * 점주 한 명이 특정 설문에 제출한 전체 답변의 묶음을 나타낸다. 실제 문항별 답변은 SURVEYANSWER에 저장한다.</p>
 */
@Entity
@Table(name = "SURVEYRESPONSE")
public class SurveyResponse {

    /**
     * 설문응답번호(PK).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "surveyresponse_seq_generator")
    @SequenceGenerator(
            name = "surveyresponse_seq_generator",
            sequenceName = "SURVEYRESPONSE_SEQ",
            allocationSize = 1)
    @Column(name = "NO")
    private Long no;

    /**
     * 응답 대상 설문. SURVEYRESPONSE.SVNO와 연결된다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SVNO", nullable = false)
    private Survey survey;

    /**
     * 설문에 응답한 점주 회원번호. MEMBER.NO를 참조한다.
     */
    @Column(name = "MNO", nullable = false)
    private Long memberNo;

    /**
     * 관리자 확인 여부. Y 또는 N.
     */
    @Column(name = "CHECKYN", nullable = false, length = 1)
    private String checkYn = "N";

    /**
     * 관리자가 응답을 확인한 날짜.
     */
    @Column(name = "CHECKDATE", length = 30)
    private String checkDate;

    /**
     * 응답 등록일.
     */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;

    /**
     * 해당 점주가 작성한 문항별 실제 답변 목록.
     */
    @OneToMany(
            mappedBy = "response",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("no ASC")
    private List<SurveyAnswer> answers = new ArrayList<>();

    public SurveyResponse() {
    }

    /**
     * 실제 답변을 응답 묶음에 추가하고 양방향 관계를 맞춘다.
     */
    public void addAnswer(SurveyAnswer answer) {
        answers.add(answer);
        answer.setResponse(this);
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
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

    public List<SurveyAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SurveyAnswer> answers) {
        this.answers = answers;
    }
}
