package dev.jpa.allimio.shopsurveyansweroption;

import dev.jpa.allimio.shopsurveyanswer.ShopSurveyAnswer;
import dev.jpa.allimio.shopsurveyoption.ShopSurveyOption;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 객관식 답(선택한 보기) Entity
 *
 * SHOP_SURVEY_ANSWER_OPTION 테이블과 연결됩니다.
 * PK가 (SANO, SONO) 복합키라서 @EmbeddedId + @MapsId로 매핑합니다.
 * 한 답변에서 같은 보기를 두 번 선택할 수 없습니다.
 */
@Entity
@Table(name = "SHOP_SURVEY_ANSWER_OPTION")
@Getter
@Setter
@ToString(exclude = {"answer", "option"}) // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurveyAnswerOption {

  /** 복합키 (SANO, SONO) */
  @EmbeddedId
  private Pk id;

  /** 답변 (PK, FK -> SHOP_SURVEY_ANSWER.NO) */
  @MapsId("sano")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sano")
  private ShopSurveyAnswer answer;

  /** 보기 (PK, FK -> SHOP_SURVEY_OPTION.NO) */
  @MapsId("sono")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sono")
  private ShopSurveyOption option;

  /** 복합키 클래스 */
  @Embeddable
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class Pk implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 답변번호 */
    private Long sano;

    /** 보기번호 */
    private Long sono;
  }
}
