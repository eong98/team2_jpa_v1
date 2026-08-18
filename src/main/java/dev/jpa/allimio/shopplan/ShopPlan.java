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

  /** CCTV 1대당 기본단가 */
  private Double bprice;

  /** 구독권 상세 설명 */
  private String description;

  /** 구독중인 상품 */
  @Builder.Default
  private String issell = "N";

  /** 구독시작일 */
  private String cdate;

}
