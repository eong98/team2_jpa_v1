package dev.jpa.allimio.shoporder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopOrderDTO {

  /** 신규 구독 결제 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {
    /** 구독권(상품) 번호 */
    private Long pno;
    /** 회원번호 */
    private Long mno;
    /** 선택 기간 (6, 12) */
    private Integer pmonth;
    /** 구독시 지정한 CCTV 개수 */
    private Integer ccnt;
    /** CCTV 1대당 기본단가 — 결제 시점 SHOP_PLAN.bprice를 그대로 스냅샷으로 넘겨받음 */
    private Double bprice;
    /** 총 결제 금액 */
    private Long totalprice;
    /** 구독 시작일 */
    private String sdate;
    /** 구독 종료일 */
    private String edate;
  }

  /** 구독 내역 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private String orderno;
    private Long pno;
    private Long mno;
    private Long sno;
    private Integer pmonth;
    private Integer ccnt;
    private Double bprice;
    private Long totalprice;
    private Integer status;
    private String sdate;
    private String edate;
    private String cdate;
    private String udate;

    public static Response from(ShopOrder entity) {
      if (entity == null) return null;
      return Response.builder()
          .orderno(entity.getOrderno())
          .pno(entity.getPno())
          .mno(entity.getMno())
          .sno(entity.getSno())
          .pmonth(entity.getPmonth())
          .ccnt(entity.getCcnt())
          .bprice(entity.getBprice())
          .totalprice(entity.getTotalprice())
          .status(entity.getStatus())
          .sdate(entity.getSdate())
          .edate(entity.getEdate())
          .cdate(entity.getCdate())
          .udate(entity.getUdate())
          .build();
    }
  }

  /** 매장 선택 확정 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LinkShopRequest {
    /** 연결할 매장 번호 */
    private Long sno;
  }

  /**
   * 구독 갱신 요청.
   * newCcnt를 안 보내거나 기존 CCNT와 같으면 "동일 조건 갱신"(기간만 연장),
   * 다르게 보내면(더 큰 값만 허용) "CCTV 대수 변경 갱신"으로 처리됩니다.
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RenewRequest {
    private Integer newCcnt;
  }
  
  /** 구독 갱신 결과 — 대수 변경에 따른 추가결제/환불 금액을 함께 반환 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RenewResult {
    private String orderno;
    private Integer ccnt;
    private Long totalprice;
    private String edate;
    /** 대수 증가 시 추가 결제 금액 (증가 없으면 0) */
    private Long extraCharge;
    /** 대수 감소 시 환불 금액 (감소 없으면 0) */
    private Long refundAmount;
  }

  /** 구독 취소 응답 — 환불 계산 결과 포함 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CancelResult {
    /** 취소된 구독 내역 번호 */
    private String orderno;
    /** 사용한 개월수 (1개월 미만도 1개월로 올림) */
    private int usedMonths;
    /** 환불 대상 개월수 (총 결제개월수 - 사용개월수) */
    private int refundMonths;
    /** 환불 금액 (대당단가 × CCTV대수 × 환불개월수) */
    private long refundAmount;
  }
  
  
  
  
  /**
   * 내 구독 내역 검색 조건
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    private Long mno;
    private String word;
    private Integer status;
    private Long pno;
    private Long sno;

    /** 날짜 검색 기준 ('sdate' 구독시작일 / 'edate' 구독종료일 / 'cdate' 구매일), null이면 날짜 검색 안 함 */
    private String dateType;
    /** 날짜 검색 시작일 (YYYY-MM-DD) */
    private String dateFrom;
    /** 날짜 검색 종료일 (YYYY-MM-DD) */
    private String dateTo;
  }
  
  
  
  
  
  
  
  
  
  
  
  
}