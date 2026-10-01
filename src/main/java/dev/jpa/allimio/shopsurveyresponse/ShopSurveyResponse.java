package dev.jpa.allimio.shopsurveyresponse;

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
 * 매장 설문 답변(고객 1회 제출) Entity
 *
 * SHOP_SURVEY_RESPONSE 테이블과 연결됩니다.
 * 고객이 설문을 한 번 제출할 때마다 1건 생성됩니다.
 */
@Entity
@Table(name = "SHOP_SURVEY_RESPONSE")
@Getter
@Setter
@ToString(exclude = "survey") // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurveyResponse {

  /** 응답번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_survey_response_seq_use")
  @SequenceGenerator(name = "shop_survey_response_seq_use", sequenceName = "SHOP_SURVEY_RESPONSE_SEQ", allocationSize = 1)
  private Long no;

  /** 설문 (FK -> SHOP_SURVEY.NO) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "svno")
  private ShopSurvey survey;

  /** 접속IP (도배 방지용) */
  private String ipAddr;

  /** 응답일시 */
  private String cdate;
}
