package dev.jpa.allimio.shoporder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 매장별 구독 내역 Entity
 *
 * SHOP_ORDER 테이블과 연결됩니다.
 * PK(ORDERNO)는 시퀀스가 아니라 랜덤 문자열로 직접 발급합니다(ShopOrderService.generateOrderNo()).
 * 매장(SNO) 연결은 결제 시점엔 비워두고, 매장 선택 확정 시점에 채워집니다.
 * 구독권 
 */
@Entity
@Table(name = "SHOP_ORDER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOrder {
  @Id
  /** 구독 내역 랜덤번호 (PK) */
  private String no;

  /** 회원 번호 (FK -> MEMBER.NO) */
  private Long mno;

  /** 구독권(상품) 번호 (FK -> SHOP_PLAN.NO) */
  private Long pno;

  /** 매장 번호 (FK -> SHOP.NO), 결제 후 매장 등록시 저장 */
  private Long sno;

  /** 선택 기간 (6: 6개월, 12: 12개월) */
  private Integer pmonth;

  /** 구독시 지정한 CCTV 개수 */
  private Integer ccnt;

  /** CCTV 1대당 기본단가 (결제 시점 스냅샷) */
  private Double bprice;

  /** 총 결제 금액 (원) */
  private Long totalprice;

  /** 구독 상태 (0: 대기, 1: 정상, 2: 만료, 3: 취소) */
  @Builder.Default
  private Integer status = 0;

  /** 구독 시작일 (YYYY-MM-DD) */
  private String sdate;

  /** 구독 종료일 (YYYY-MM-DD) */
  private String edate;

  /** 구매일시 (YYYY-MM-DD HH24:MI:SS) */
  private String cdate;

  /** 구매 변경일시 (환불, 취소, 갱신) */
  private String udate;
}