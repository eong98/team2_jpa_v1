package dev.jpa.allimio.shoporderpending;

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
@Table(name = "SHOP_ORDER_PENDING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOrderPending {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_order_pending_use")
  @SequenceGenerator(name = "shop_order_pending_use", sequenceName = "SHOP_ORDER_PENDING_SEQ", allocationSize = 1)
  private Long no;
  
  /** 구독 주문내역 랜덤번호 (PK) */
  private String ono;
  
  /** 회원 번호 (조회용) */
  private Long mno;

  /** 변경 신청된 구독권 상품 번호 (FK -> SHOP_PLAN.NO) */
  private Long pno;
  
  /** 변경 신청된 CCTV 대수 */
  private Integer ccnt;
  
  /** 변경 신청된 등급의 CCTV 대당단가 (스냅샷) */
  private Double bprice;
  
  /** 변경 신청된 이용기간 (6, 12) */
  private Integer pmonth;
  
  /** 변경 확정시 반영될 새 종료일 */
  private String edate;

  /** 변경 확정시 반영될 총 결제금액 */
  private Long totalprice;

  /** 변경신청 상태(0: 승인대기, 1:승인반려, 2:완료) */
  @Builder.Default
  private Integer status = 0;
  
  /** 승인 반려 사유 (관리자 입력) */
  private String memo;

  /** 변경 신청일시 */
  private String cdate;

  /** 변경 처리완료 일시 */
  private String udate;

}
