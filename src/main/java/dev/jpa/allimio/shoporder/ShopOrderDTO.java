package dev.jpa.allimio.shoporder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopOrderDTO {

  /** 신규 구독 결제 요청 — 구독권/대수/기간은 여기서만 정해지고 이후 변경 불가 */
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

  /** 매장 선택 확정 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LinkShopRequest {
    /** 연결할 매장 번호 */
    private Long sno;
  }

  /** 구독 갱신 결과 (기간 연장 전용 — 대수/플랜 변경 없음) */
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
    private int usedMonths;
    /** 환불 대상 개월수 (총 결제개월수 - 사용개월수) */
    private int refundMonths;
    /** 환불 금액 (대당단가 × CCTV대수 × 환불개월수) */
    private long refundAmount;
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

  /** 내 구독 내역 검색 조건 (회원용/관리자용 공통) */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    /** 회원번호. 회원용 API는 컨트롤러가 URL의 mno로 강제 세팅, 관리자용은 선택 필터 */
    private Long mno;
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
  
  
  /**
   * -------------------구독권 변경 로직 -------------------
   *  
   */
  /**
   * 구독권 변경 신청 요청 — 기간/대수를 자유롭게 조합해서 변경 신청합니다.
   * 대수가 바뀌면(현재 등급의 범위를 벗어나든 아니든) 관리자 승인이 필요한
   * "대기중" 상태로 전환되고, 기간만 바뀌는 경우는 즉시 반영됩니다.
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeRequest {
    /** 변경할 이용기간 (6, 12). 기존과 같으면 기간 변동 없음 */
    private Integer pmonth;
    /** 변경할 CCTV 대수. 기존과 같으면 대수 변동 없음 */
    private Integer ccnt;
  }

  /** 구독권 변경 예상 결과(미리보기) — 실제 반영 없이 계산만 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangePreview {
    /** 변경 후 적용될 구독권 이름 (등급이 바뀌는 경우 새 등급명) */
    private String pname;
    /** 변경 후 대당단가 */
    private Double bprice;
    /** 추가 결제 금액 (0이면 없음) */
    private Long extraCharge;
    /** 환불 금액 (0이면 없음) */
    private Long refundAmount;
    /** 변경 후 예상 총 결제금액 */
    private Long totalprice;
    /** 변경 후 예상 종료일 (기간 변경이 없으면 기존과 동일) */
    private String edate;
    /** 대수 변경 여부 — true면 관리자 승인 필요(즉시 반영 안 됨) */
    private boolean requiresApproval;
  }

  /** 구독권 변경 신청 결과 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeResult {
    private String no;
    /** true면 대수 변경이 포함되어 관리자 승인 대기 상태로 전환됨 */
    private boolean pending;
    /** pending=false(기간만 변경)일 때 즉시 반영된 결과 */
    private Response applied;
  }

  /** 관리자용 — 구독권 변경 승인/반려 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeApprovalRequest {
    /** true면 승인, false면 반려 */
    private boolean approve;
  }
  
}