package dev.jpa.allimio.shoprefund;

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
 * 환불계좌 Entity
 *
 * SHOP_REFUND 테이블과 연결됩니다. 구독 취소로 환불이 발생할 때(ShopOrderService.cancel())
 * 사용자가 입력한 계좌 정보와 환불 금액을 저장하고, 관리자가 실제 이체 처리를 완료하면
 * STATUS를 1(완료)로 바꿉니다. SHOP_PAYMENT의 환불 기록(pstatus=2)과 PAYMENTNO로 연결됩니다.
 */
@Entity
@Table(name = "SHOP_REFUND")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopRefund {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_refund_seq_use")
  @SequenceGenerator(name = "shop_refund_seq_use", sequenceName = "SHOP_REFUND_SEQ", allocationSize = 1)
  /** 환불 고유번호 (PK) */
  private Long no;

  /** 구독 내역 번호 (FK -> SHOP_ORDER.ORDERNO) */
  private String ono;

  /** 연결된 환불 결제기록 (FK -> SHOP_PAYMENT.NO) */
  private Long pno;

  /** 회원번호 (FK -> MEMBER.NO) */
  private Long mno;

  /** 은행명 */
  private String bankName;

  /** 계좌번호 */
  private String accountNo;

  /** 예금주명 */
  private String accountHolder;

  /** 환불 금액 */
  private Long amount;

  /** 처리상태 (0: 대기, 1: 완료, 2: 반려) */
  @Builder.Default
  private Integer status = 0;

  /** 등록일시 */
  private String cdate;

  /** 처리 완료/변경일시 */
  private String udate;
}