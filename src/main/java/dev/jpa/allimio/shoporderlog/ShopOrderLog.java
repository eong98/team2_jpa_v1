package dev.jpa.allimio.shoporderlog;

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

/**
 * 구독 내역 변경 이력 Entity
 *
 * SHOP_ORDER_LOG 테이블과 연결됩니다. SHOP_ORDER에서 결제(0)/매장연결(1)/
 * 갱신(2)/취소(3)가 발생할 때마다 ShopOrderService가 이 테이블에 이벤트를
 * 하나씩 남깁니다. 조회 전용이라 이 테이블을 직접 수정하는 API는 없습니다.
 */
@Entity
@Table(name = "SHOP_ORDER_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOrderLog {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_order_log_seq_use")
  @SequenceGenerator(name = "shop_order_log_seq_use", sequenceName = "SHOP_ORDER_LOG_SEQ", allocationSize = 1)
  /** 로그 고유번호 (PK) */
  private Long no;

  /** 구독 내역 번호 (FK -> SHOP_ORDER.ono) */
  private String ono;

  /** 회원번호 (조회 편의용 비정규화) */
  private Long mno;
  
  /** 변경신청 번호 (조회 편의용 비정규화) */
  private Long pno;

  /** 이벤트 종류 (0: 결제, 1: 매장연결, 2: 갱신, 3: 취소) */
  private Integer action;

  /** 관련 매장번호 (매장연결일 때만 값 있음) */
  private Long sno;

  /** 변경 전 종료일 (갱신일 때) */
  private String beforeEdate;

  /** 변경 후 종료일 (결제/매장연결/갱신일 때) */
  private String afterEdate;

  /** 관련 금액 (결제: 결제액, 취소: 환불액) */
  private Long amount;

  /** 부가 설명 */
  private String memo;

  /** 발생일시 */
  private String cdate;

  /** 구독시 지정한 CCTV 개수 */
  private Integer ccnt;
  /** CCTV 1대당 기본단가 (결제 시점 스냅샷) */
  private Double bprice;
}