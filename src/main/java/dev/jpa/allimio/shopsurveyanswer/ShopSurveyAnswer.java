package dev.jpa.allimio.shopsurveyanswer;

import dev.jpa.allimio.shopsurveyquestion.ShopSurveyQuestion;
import dev.jpa.allimio.shopsurveyresponse.ShopSurveyResponse;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 문항별 답 Entity
 *
 * SHOP_SURVEY_ANSWER 테이블과 연결됩니다.
 * (SRNO, SQNO) 복합 UNIQUE: 한 응답에서 같은 문항의 답은 1개
 *
 * - SHORT / LONG  : CONTENT 사용
 * - SCALE         : SCALE 사용 (0.0 ~ 10.0)
 * - SINGLE / MULTI: SHOP_SURVEY_ANSWER_OPTION에 선택한 보기 저장
 * - 첨부 사진      : ATTACH (TNAME = 'SHOP_SURVEY_ANSWER', BNO = 이 테이블의 NO)
 */
@Entity
@Table(name = "SHOP_SURVEY_ANSWER")
@Getter
@Setter
@ToString(exclude = {"response", "question"}) // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurveyAnswer {

  /** 답변번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_survey_answer_seq_use")
  @SequenceGenerator(name = "shop_survey_answer_seq_use", sequenceName = "SHOP_SURVEY_ANSWER_SEQ", allocationSize = 1)
  private Long no;

  /** 응답 (FK -> SHOP_SURVEY_RESPONSE.NO) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "srno")
  private ShopSurveyResponse response;

  /** 문항 (FK -> SHOP_SURVEY_QUESTION.NO) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sqno")
  private ShopSurveyQuestion question;

  /** 답변 (단답/장문) */
  private String content;

  /** 점수 (척도형, NUMBER(3,1)) */
  private BigDecimal scale;
}
