package dev.jpa.allimio.shoppayment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopPaymentDTO {

  /** 결제 내역 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long no;
    private String ono;
    private Long mno;
    private Long price;
    private Integer pmethod;
    private Integer pstatus;
    private String cdate;
    private String udate;

    public static Response from(ShopPayment entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .ono(entity.getOno())
          .mno(entity.getMno())
          .price(entity.getPrice())
          .pmethod(entity.getPmethod())
          .pstatus(entity.getPstatus())
          .cdate(entity.getCdate())
          .udate(entity.getUdate())
          .build();
    }
  }

  /** 회원용/관리자용 공통 검색 조건 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    /** 회원번호. 회원용 API는 컨트롤러가 URL의 mno로 강제 세팅, 관리자용은 선택 필터 */
    private Long mno;
    /** 결제 수단 (0 카드 / 1 계좌이체 / 2 토스페이) */
    private Integer pmethod;
    /** 결제 상태 (0 완료 / 1 실패 / 2 취소) */
    private Integer pstatus;
    /** 조회 시작일 (YYYY-MM-DD) */
    private String dateFrom;
    /** 조회 종료일 (YYYY-MM-DD) */
    private String dateTo;
  }
}