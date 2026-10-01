package dev.jpa.allimio.shopsurveyquestion;

import dev.jpa.allimio.shopsurvey.ShopSurvey;

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
 * 매장 설문 문항 Entity
 *
 * SHOP_SURVEY_QUESTION 테이블과 연결됩니다.
 *
 * - ATYPE: SHORT(단답) / LONG(장문) / SINGLE(단일선택) / MULTI(복수선택) / SCALE(점수, 0~10)
 * - REQUIREDYN: 0:필수아님 / 1:필수
 * - FILEYN: 0:첨부불가 / 1:첨부가능 (최대 10장 고정)
 */
@Entity
@Table(name = "SHOP_SURVEY_QUESTION")
@Getter
@Setter
@ToString(exclude = "survey") // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurveyQuestion {

  /** 문항번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_survey_question_seq_use")
  @SequenceGenerator(name = "shop_survey_question_seq_use", sequenceName = "SHOP_SURVEY_QUESTION_SEQ", allocationSize = 1)
  private Long no;

  /** 설문 (FK -> SHOP_SURVEY.NO) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "svno")
  private ShopSurvey survey;

  /** 문항제목 */
  private String title;

  /** 답변타입 SHORT/LONG/SINGLE/MULTI/SCALE */
  private String atype;

  /** 필수여부 0:필수아님/1:필수 */
  private Integer requiredyn;

  /** 파일첨부여부 0:첨부불가/1:첨부가능 */
  private Integer fileyn;

  /** 정렬순서 */
  private Integer sort;
}
