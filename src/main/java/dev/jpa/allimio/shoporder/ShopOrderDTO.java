package dev.jpa.allimio.shoporder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopOrderDTO {

  /** 신규 구독권 결제 요청 */
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
    /** 결제 수단 (0 카드 / 1 계좌이체 / 2 토스페이) */
    private Integer pmethod;
  }

  /** 구독 내역 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private String no;
    private Long pno;
    private String pname; // join
    private Long mno;
    private Long sno;
    private String sname; // join
    private Integer pmonth;
    private Integer ccnt;
    private Double bprice;
    private Long totalprice;
    private Integer status;
    private String sdate;
    private String edate;
    private String cdate;
    private String udate;
    /** 변경 신청된 구독권 번호 (등급 변경 시) */
    private Long pendingPno;
    /** 변경 신청된 이용기간 */
    private Integer pendingPmonth;
    /** 변경 신청된 CCTV 대수 */
    private Integer pendingCcnt;
    /** 변경 신청된 등급의 대당단가 (스냅샷) */
    private Double pendingBprice;
    /** 변경 확정 시 반영될 총 결제금액 */
    private Long pendingTotalprice;
    /** 변경 확정 시 반영될 새 종료일 */
    private String pendingEdate;    
    
    /** 같은 이용기간(pmonth) 내 전체 등급을 통틀은 최소 CCTV 대수 (변경 시 하한선) */
    private Integer minCcnt;
    /** 같은 이용기간(pmonth) 내 전체 등급을 통틀은 최대 CCTV 대수 (변경 시 상한선) */
    private Integer maxCcnt;
    
    /** 1. 기본 엔티티 단건 변환용 메서드 */
    public static Response from(ShopOrder entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
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
          .pendingPno(entity.getPendingPno())
          .pendingPmonth(entity.getPendingPmonth())
          .pendingCcnt(entity.getPendingCcnt())
          .pendingBprice(entity.getPendingBprice())
          .pendingTotalprice(entity.getPendingTotalprice())
          .pendingEdate(entity.getPendingEdate())
          .build();
    }

    /** 2. 💡 실무형 조인 맵핑 메서드: 엔티티 + 조인 필드(pname, sname) 받아서 결합 */
    public static Response from(ShopOrder entity, String pname, String sname) {
      Response response = from(entity);
      if (response != null) {
        response.setPname(pname);
        response.setSname(sname);
      }
      return response;
    }
    
    // ShopOrderDTO.Response.from(entity, pname, sname) 패턴과 동일하게 오버로드 추가
    public static Response from(ShopOrder entity, String pname, String sname, Integer minCcnt, Integer maxCcnt) {
      Response response = from(entity, pname, sname);
      if (response != null) {
        response.setMinCcnt(minCcnt);
        response.setMaxCcnt(maxCcnt);
      }
      return response;
    }
  }
  

  /** 검색 조건 (회원용/관리자용 공통) */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    /** 회원번호. 회원용 API는 컨트롤러가 URL의 mno로 강제 세팅, 관리자용은 선택 필터 */
    private Long mno;
    private Long sno;
    /** 검색어 (매장이름 부분일치) */
    private String word;
    /** 구독 상태 (0 정상 / 1 만료됨 / 2 취소) */
    private Integer status;
    /** 구독권 이름 */
    private String pname;
    /** 선택 기간 (6, 12) */
    private Integer pmonth;
    /** 날짜 검색 시작일 (YYYY-MM-DD) */
    private String dateFrom;
    /** 날짜 검색 종료일 (YYYY-MM-DD) */
    private String dateTo;
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

  /** 구독 갱신 결과 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RenewResult {
    private String no;
    private Integer ccnt;
    private Long totalprice;
    private String edate;
  }

  /** 구독 취소 요청 — 환불 대상(환불금액>0)일 때 환불계좌 정보 필수 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CancelRequest {
    private String bankName;
    private String accountNo;
    private String accountHolder;
  }
  
  /** 구독 취소 결과 — 환불 계산 결과 포함 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CancelResult {
    /** 취소된 구독 내역 번호 */
    private String no;
    /** 사용한 개월수 (1개월 미만도 1개월로 올림) */
    private double usedMonths;
    /** 환불 대상 개월수 (총 결제개월수 - 사용개월수) */
    private double refundMonths;
    /** 환불 금액 (대당단가 × CCTV대수 × 환불개월수) */
    private long refundAmount;
  }
  

  
  /**
   * -------------------구독권 변경 로직 -------------------
   *  
   */
  
  /** 구독권 변경 신청 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeRequest {
    private Integer pmonth;
    private Integer ccnt;
    /** 추가금 발생 시 결제수단 (0 카드 / 1 계좌이체 / 2 토스페이) */
    private Integer pmethod;
    /** 환불 발생 가능성이 있는 변경(기간축소/대수증가/대수감소) 시 미리 받는 계좌 정보 */
    private String bankName;
    private String accountNo;
    private String accountHolder;
  }

  /** 구독권 변경 예상 결과 미리보기 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangePreview {
    private String pname;
    private Double bprice;
    private Long extraCharge;
    private Long refundAmount;
    private Long totalprice;
    private String edate;
    private Boolean requiresApproval;
  }

  /** 구독권 변경 신청 결과 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeResult {
    private String no;
    private boolean pending;
    private Response applied;
  }

  /** 관리자용 — 구독권 변경 승인/반려 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeApprovalRequest {
    private boolean approve;
  }
}