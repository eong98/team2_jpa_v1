package dev.jpa.allimio.shoppayment;

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
 * 구독 결제 내역 Entity
 *
 * SHOP_PAYMENT 테이블과 연결됩니다. SHOP_ORDER_LOG(구독 상태 변화 이벤트)와
 * 역할이 다릅니다 — 이 테이블은 "돈이 실제로 어떻게 오갔는지"(결제수단, 성공/실패,
 * PG 트랜잭션 단위)를 기록합니다. 한 SHOP_ORDER에 결제 시도가 여러 건(재시도,
 * 갱신결제 등) 쌓일 수 있습니다.
 */
@Entity
@Table(name = "SHOP_PAYMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopPayment {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_payment_seq_use")
  @SequenceGenerator(name = "shop_payment_seq_use", sequenceName = "SHOP_PAYMENT_SEQ", allocationSize = 1)
  /** 결제 내역 고유 번호 (PK) */
  private Long no;

  /** 구독 내역 번호 (FK -> SHOP_ORDER.ORDERNO) */
  private String ono;

  /** 회원 번호 (FK -> MEMBER.NO) */
  private Long mno;

  /** 실 결제 금액 (원) */
  private Long price;

  /** 결제 수단 (0: 카드, 1: 계좌이체, 2: 토스페이) */
  private Integer pmethod;

  /** 결제 상태 (0: 결제완료, 1: 결제실패, 2: 결제취소) */
  @Builder.Default
  private Integer pstatus = 0;

  /** 결제일시 */
  private String cdate;

  /** 결제 상태 변경일시 (환불일, 취소일) */
  private String udate;
}