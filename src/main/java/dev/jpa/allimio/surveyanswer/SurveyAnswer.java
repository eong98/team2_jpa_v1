package dev.jpa.allimio.surveyanswer;


import dev.jpa.allimio.surveyquestion.SurveyQuestion;
import dev.jpa.allimio.surveyresponse.SurveyResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 문항별 실제 답변 엔티티.
 *
 * <p>점주가 선택하거나 직접 입력한 값과
 * AI가 분석한 평가점수(1~5)를 저장한다.</p>
 */
@Entity
@Table(name = "SURVEYANSWER")
public class SurveyAnswer {

    /** 문항별 답변번호(PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "surveyanswer_seq_generator")
    @SequenceGenerator(
            name = "surveyanswer_seq_generator",
            sequenceName = "SURVEYANSWER_SEQ",
            allocationSize = 1)
    @Column(name = "NO")
    private Long no;

    /** 점주별 설문응답 묶음. SURVEYANSWER.RNO와 연결된다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RNO", nullable = false)
    private SurveyResponse response;

    /** 답변한 설문문항. SURVEYANSWER.QNO와 연결된다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "QNO", nullable = false)
    private SurveyQuestion question;

    /**
     * 객관식 선택값 또는 직접입력 답변.
     * Oracle CLOB 컬럼과 매핑한다.
     */
    @Lob
    @Column(name = "ATEXT")
    private String atext;

    /** 등록일. */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;

    public SurveyAnswer() {
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public SurveyResponse getResponse() {
        return response;
    }

    public void setResponse(SurveyResponse response) {
        this.response = response;
    }

    public SurveyQuestion getQuestion() {
        return question;
    }

    public void setQuestion(SurveyQuestion question) {
        this.question = question;
    }

    public String getAtext() {
        return atext;
    }

    public void setAtext(String atext) {
        this.atext = atext;
    }


    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }
}
