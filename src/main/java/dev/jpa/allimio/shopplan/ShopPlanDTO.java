package dev.jpa.allimio.shopplan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopPlanDTO {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {
    /** 구독권 상품이름 */
    private String pname;
    /** 구독 이용기간 (6, 12 / 개월) */
    private Integer pmonth;
    /** 이용기간별 CCTV 최소 갯수 지정 */
    private Integer mincctv;    
    /** 이용기간별 CCTV 최대 갯수 지정 */
    private Integer maxcctv;
    /** CCTV 1대당 기본단가 */
    private Double bprice;
    /** 구독권 상세 설명 */
    private String description;
    /** 판매중인 상품 */
    private String issell;
    /** 추천 요금제 여부 */
    private String isreco;
    
    public ShopPlan toEntity() {
      return ShopPlan.builder()
          .pname(this.pname)
          .pmonth(this.pmonth)
          .mincctv(this.mincctv)
          .maxcctv(this.maxcctv)
          .bprice(this.bprice)
          .description(this.description)
          .issell(this.issell != null ? this.issell : "Y")
          .isreco(this.isreco != null ? this.isreco : "N")
          .build();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    /** 구독권 고유번호 (PK) */
    private Long no;
    /** 구독권 상품이름 */
    private String pname;
    /** 구독 이용기간 (6, 12 / 개월) */
    private Integer pmonth;
    /** 이용기간별 CCTV 최소 갯수 지정 */
    private Integer mincctv;    
    /** 이용기간별 CCTV 최대 갯수 지정 */
    private Integer maxcctv;
    /** CCTV 1대당 기본단가 */
    private Double bprice;
    /** 구독권 상세 설명 */
    private String description;
    /** 판매중인 상품 */
    private String issell;
    /** 추천 요금제 여부 */
    private String isreco;
    /** 구독시작일 */
    private String cdate;
    /** 실제 결제(주문) 건수 — 인기도 계산용, DB 컬럼 아님 */
    private Long orderCount;
    /** 같은 이용기간(pmonth) 그룹 내에서 주문 건수 1위인지 — 인기 배지 표시용, DB 컬럼 아님 */
    private boolean popular;
    
    public static Response from(ShopPlan entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .pname(entity.getPname())
          .pmonth(entity.getPmonth())
          .maxcctv(entity.getMaxcctv())
          .mincctv(entity.getMincctv())
          .bprice(entity.getBprice())
          .description(entity.getDescription())
          .issell(entity.getIssell())
          .isreco(entity.getIsreco())
          .cdate(entity.getCdate())
          .build();
    }
  }
  
  

  /**
   * 검색 조회 조건
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PlanSearchRequest {
    /** 검색어 (제목, 내용 등) */
    private String word;
    /** 구독권 상품이름 */
    private String pname;
    /** 구독 이용기간 (6, 12 / 개월) */
    private Integer pmonth;
    /** CCTV 1대당 기본단가 */
    private Double bprice;
    /** 구독권 상세 설명 */
    private String description;
    /** 판매중인 상품 */
    private String issell;
    /** 구독시작일 */
    private String cdate;
    
  }
}
