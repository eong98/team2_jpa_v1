package dev.jpa.allimio.shopsurveysummary;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 AI 요약 결과 Entity
 *
 * SHOP_SURVEY_SUMMARY 테이블과 연결됩니다.
 * 설문당 1개만 저장하며, 요약을 다시 실행하면 같은 SVNO 행을 덮어씁니다.
 * 점주 화면에는 다시 보여주지 않고, AI 자동작성 때 참고 자료로만 사용합니다.
 *
 * - SVNO: 설문번호 (PK, FK -> SHOP_SURVEY.NO)
 * - WEAKPOINTS: 약한 항목 JSON 배열 (예: ["매장 청결 - 점수 평균 4.2점", ...])
 */
@Entity
@Table(name = "SHOP_SURVEY_SUMMARY")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurveySummary {

  /** 설문번호 (PK, FK -> SHOP_SURVEY.NO) */
  @Id
  private Long svno;

  /** 요약 (100단어 미만) */
  private String summary;

  /** 긍정/부정 점수 0.0 ~ 10.0 */
  private BigDecimal score;

  /** 약한 항목 JSON 배열 */
  @Lob
  private String weakpoints;
}