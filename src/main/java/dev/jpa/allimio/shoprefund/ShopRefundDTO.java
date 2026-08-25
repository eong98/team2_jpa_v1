package dev.jpa.allimio.shoprefund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopRefundDTO {

  /** 환불계좌 등록 요청 (구독 취소 시 같이 넘어옴) */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {
    private String bankName;
    private String accountNo;
    private String accountHolder;
  }

  /** 환불계좌 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long no;
    private String ono;
    private Long paymentno;
    private Long mno;
    private String bankName;
    private String accountNo;
    private String accountHolder;
    private Long amount;
    private Integer status;
    private String cdate;
    private String udate;

    public static Response from(ShopRefund entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .ono(entity.getOno())
          .paymentno(entity.getPaymentno())
          .mno(entity.getMno())
          .bankName(entity.getBankName())
          .accountNo(entity.getAccountNo())
          .accountHolder(entity.getAccountHolder())
          .amount(entity.getAmount())
          .status(entity.getStatus())
          .cdate(entity.getCdate())
          .udate(entity.getUdate())
          .build();
    }
  }

  /** 관리자용 처리상태 변경 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UpdateStatusRequest {
    /** 변경할 처리상태 (0 대기 / 1 완료 / 2 반려) */
    private Integer status;
  }

  /** 회원용/관리자용 공통 검색 조건 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    private Long mno;
    private Integer status;
    private String dateFrom;
    private String dateTo;
  }
}