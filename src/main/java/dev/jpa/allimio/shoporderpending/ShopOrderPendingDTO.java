package dev.jpa.allimio.shoporderpending;

import dev.jpa.allimio.shoporder.ShopOrderDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopOrderPendingDTO {
  /** 변경 신청 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {
    private String ono;
    private Integer pmonth;
    private Integer ccnt;
    
    private Integer pmethod;
    private String bankName;
    private String accountNo;
    private String accountHolder;
  }

  /** 변경 예상 결과 미리보기 */
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
    private String edate;
  }
  
  /** 구독권 변경 신청 처리 결과 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeResult {
    /** 생성된 SHOP_ORDER_PENDING.NO */
    private String no;
    /** true면 대수변경 포함되어 관리자 승인 대기중, false면 기간변경만이라 즉시 확정됨 */
    private boolean pending;
  }


  /** 관리자용 승인/반려 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ApprovalRequest {
    private boolean approve;
    private String memo; // 반려 사유
  }

  /** 신청 건 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long no;
    private String ono;
    private Long mno;
    private Long pno;
    private Integer ccnt;
    private Double bprice;
    private Integer pmonth;
    private String edate;
    private Long totalprice;
    private Integer status;
    private String memo;
    private String cdate;
    private String udate;

    // join
    private String sname; // 관리자용 조회
    private String pname; // 미리보기, 변경될 구독권 이름 조회
    /** 같은 이용기간(pmonth) 내 전체 등급을 통틀은 최소 CCTV 대수 (변경 시 하한선) */
    private Integer minCcnt;
    /** 같은 이용기간(pmonth) 내 전체 등급을 통틀은 최대 CCTV 대수 (변경 시 상한선) */
    private Integer maxCcnt;

    public static Response from(ShopOrderPending entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .ono(entity.getOno())
          .mno(entity.getMno())
          .pno(entity.getPno())
          .ccnt(entity.getCcnt())
          .bprice(entity.getBprice())
          .pmonth(entity.getPmonth())
          .edate(entity.getEdate())
          .totalprice(entity.getTotalprice())
          .status(entity.getStatus())
          .memo(entity.getMemo())
          .cdate(entity.getCdate())
          .udate(entity.getUdate())
          .build();
    }
    

    public static Response from(ShopOrderPending entity, String pname, String sname, Integer minCcnt, Integer maxCcnt) {
      Response res = from(entity);
      if (res != null) {
        res.setPname(pname);
        res.setSname(sname);
        res.setMinCcnt(minCcnt);
        res.setMaxCcnt(maxCcnt);
      }
      return res;
    }
    
  }
  
  /** 검색조건 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SearchRequest {
    private Integer status;
    private String word;
    private String dateFrom;
    private String dateTo;
  }
}