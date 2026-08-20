package dev.jpa.allimio.surveyquestion;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 설문 문항 엔티티.
 *
 * <p>질문 내용, 답변 유형, 객관식 선택지 문자열, 필수 여부,
 * 화면 출력 순서를 저장한다.</p>
 */
@Entity
@Table(name = "SURVEYQUESTION")
public class SurveyQuestion {

    /** 설문문항번호(PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "surveyquestion_seq_generator")
    @SequenceGenerator(
            name = "surveyquestion_seq_generator",
            sequenceName = "SURVEYQUESTION_SEQ",
            allocationSize = 1)
    @Column(name = "NO")
    private Long no;

    /** 이 문항이 포함된 설문. SURVEYQUESTION.SVNO와 연결된다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SVNO", nullable = false)
    private Survey survey;

    /** 질문내용. */
    @Column(name = "QTEXT", nullable = false, length = 1000)
    private String qtext;

    /**
     * 문항유형.
     * SINGLE, MULTIPLE, TEXT, SCORE 중 하나를 저장한다.
     */
    @Column(name = "QTYPE", nullable = false, length = 20)
    private String qtype;

    /**
     * 객관식 선택지.
     * 예: 만족|조금만족|불만족
     * TEXT 또는 SCORE 문항에서는 null로 저장할 수 있다.
     */
    @Column(name = "QOPTIONS", length = 2000)
    private String qoptions;

    /** 필수응답 여부. Y 또는 N. */
    @Column(name = "REQUIREDYN", nullable = false, length = 1)
    private String requiredYn = "Y";

    /** 문항 출력 순서. */
    @Column(name = "SEQNO", nullable = false)
    private Integer seqNo = 1;

    /** 등록일. */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;

    /** 이 문항에 작성된 실제 답변 목록. */
    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = false,
            fetch = FetchType.LAZY)
    private List<SurveyAnswer> answers = new ArrayList<>();

    public SurveyQuestion() {
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

    public List<SurveyAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SurveyAnswer> answers) {
        this.answers = answers;
    }
}
