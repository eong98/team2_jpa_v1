package dev.jpa.allimio.shopplan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SHOP_PLAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopPlan {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_plan_seq_use")
  @SequenceGenerator(name = "shop_plan_seq_use", sequenceName = "SHOP_PLAN_SEQ", allocationSize = 1)
  /** 구독권 고유번호 (PK) */
  private Long no;

  /** 구독권 상품이름 */
  private String pname;
  
  /** 구독 이용기간 (6, 12 / 개월) */
  private Integer pmonth;
  
  /** 이용기간별 CCTV 최소 갯수 지정 */
  private Integer mincctv;
  
  /** 이용기간별 CCTV 최대 갯수 지정 */
  private Integer maxcctv;

  /** CCTV 1대당 기본단가 */
  private Double bprice;

  /** 구독권 상세 설명 */
  private String description;

  /** 판매중인 상품 */
  @Builder.Default
  private String issell = "Y";
  
  /** 추천 요금제 여부 (Y/N) — 결제 화면 카드에 추천 배지 표시용 */
  @Builder.Default
  private String isreco = "N";

  /** 구독시작일 */
  private String cdate;

}
