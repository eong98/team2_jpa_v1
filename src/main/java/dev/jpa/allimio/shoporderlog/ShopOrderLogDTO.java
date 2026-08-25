package dev.jpa.allimio.shoporderlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ShopOrderLogDTO {

  /** 구독 변경 이력 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long no;
    private String orderno;
    private Long mno;
    private Integer action;
    private Long sno;
    private String beforeEdate;
    private String afterEdate;
    private Long amount;
    private String memo;
    private String cdate;

    public static Response from(ShopOrderLog entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .orderno(entity.getOrderno())
          .mno(entity.getMno())
          .action(entity.getAction())
          .sno(entity.getSno())
          .beforeEdate(entity.getBeforeEdate())
          .afterEdate(entity.getAfterEdate())
          .amount(entity.getAmount())
          .memo(entity.getMemo())
          .cdate(entity.getCdate())
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
    /** 매장 번호 */
    private Long sno;
    /** 이벤트 종류 (0 결제 / 1 매장연결 / 2 갱신 / 3 취소) */
    private Integer action;
    /** 조회 시작일 (YYYY-MM-DD) */
    private String dateFrom;
    /** 조회 종료일 (YYYY-MM-DD) */
    private String dateTo;
  }
}