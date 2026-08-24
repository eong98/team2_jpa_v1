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
   * 구독 갱신/변경 요청.
   * - newCcnt: 안 보내거나 기존과 같으면 대수 변경 없음. 다르면(늘리거나 줄이거나) 그 차이만큼
   *   대당단가 × 이용기간(PMONTH) 기준으로 추가결제/환불이 계산됩니다.
   * - extendPeriod: true면 "갱신"으로 처리되어 구독 종료일(EDATE)이 이용기간만큼 연장됩니다.
   *   false 또는 미전달이면 "변경"으로 처리되어 대수만 바뀌고 종료일은 그대로입니다.
   *   (예: 종료일이 아직 많이 남았는데 CCTV만 늘리고 싶은 경우 extendPeriod=false로 호출)
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RenewRequest {
    private Integer newCcnt;
    private Boolean extendPeriod;
    /** 대수 감소로 환불이 발생하는 경우에만 필요 */
    private RefundAccount refundAccount;
  }

  /** 구독 갱신/변경 결과 — 대수 변경에 따른 추가결제/환불 금액을 함께 반환 */
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
  
  /** 환불계좌 정보 (구독 취소, CCTV 대수 감소 환불 시 공통 사용) */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RefundAccount {
    /** 은행명 */
    private String bankName;
    /** 계좌번호 */
    private String accountNo;
    /** 예금주명 */
    private String accountHolder;
  }
  
  /** 구독 취소 요청 — 환불 대상(환불금액>0)일 때 계좌 정보 필수 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CancelRequest {
    private RefundAccount refundAccount;
  }
  

  /** 내 구독 내역 검색 조건 (회원용/관리자용 공통) */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    /** 회원번호. 회원용 API는 컨트롤러가 URL의 mno로 강제 세팅, 관리자용은 선택 필터 */
    private Long mno;
    /** 검색어 (주문번호 부분일치) */
    private String word;
    /** 구독 상태 (0 정상 / 1 만료됨 / 2 취소) */
    private Integer status;
    /** 구독권 번호 */
    private Long pno;
    /** 매장 번호 */
    private Long sno;
    /** 날짜 검색 기준 ('sdate' 구독시작일 / 'edate' 구독종료일 / 'cdate' 구매일), null이면 날짜 검색 안 함 */
    private String dateType;
    /** 날짜 검색 시작일 (YYYY-MM-DD) */
    private String dateFrom;
    /** 날짜 검색 종료일 (YYYY-MM-DD) */
    private String dateTo;
  }
}