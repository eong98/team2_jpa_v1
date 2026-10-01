package dev.jpa.allimio.shopsurveyoption;

import dev.jpa.allimio.shopsurveyquestion.ShopSurveyQuestion;

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
 * 매장 설문 객관식 보기 Entity
 *
 * SHOP_SURVEY_OPTION 테이블과 연결됩니다.
 * ATYPE이 SINGLE / MULTI인 문항에만 존재합니다.
 */
@Entity
@Table(name = "SHOP_SURVEY_OPTION")
@Getter
@Setter
@ToString(exclude = "question") // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurveyOption {

  /** 보기번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_survey_option_seq_use")
  @SequenceGenerator(name = "shop_survey_option_seq_use", sequenceName = "SHOP_SURVEY_OPTION_SEQ", allocationSize = 1)
  private Long no;

  /** 문항 (FK -> SHOP_SURVEY_QUESTION.NO) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sqno")
  private ShopSurveyQuestion question;

  /** 보기내용 */
  private String label;

  /** 정렬순서 */
  private Integer sort;
}
