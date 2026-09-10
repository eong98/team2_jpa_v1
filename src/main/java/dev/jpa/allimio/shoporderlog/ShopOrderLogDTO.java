package dev.jpa.allimio.shoporderlog;

import dev.jpa.allimio.shoporder.ShopOrderDTO.Response;
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
    private String ono;
    private Long mno;
    private Long pno;
    private Integer action;
    private Long sno;
    private String beforeEdate;
    private String afterEdate;
    private Long amount;
    private String memo;
    private String cdate;
    private Integer ccnt; // 현재 구독권에서 넘겨받음
    private Double bprice; // 현재 구독권에서 넘겨받음, 변경 완료 되면 새 구독권에 대한 bprice 적용, 별도 저장없음.
    

    private String id;        // join: 회원 아이디
    private String sname;     // join: 매장명
    private String sdate;     // join: 구독시작일
    
    private String newPname;  // join:  변경 신청인 경우에만 받음, pending 에서 ccnt 받아와서 plan에서 역산해서 받음
    private Integer newCcnt;  // join: 변경신청인 경우에만 받음, pending 에서  ccnt 넘겨받음
    private String pname;     // join: 저장된 ccnt 기준으로 plan 에서 역산해서 넘겨받음

    public static Response from(ShopOrderLog entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .ono(entity.getOno())
          .mno(entity.getMno())
          .pno(entity.getPno())
          .action(entity.getAction())
          .sno(entity.getSno())
          .beforeEdate(entity.getBeforeEdate())
          .afterEdate(entity.getAfterEdate())
          .amount(entity.getAmount())
          .memo(entity.getMemo())
          .cdate(entity.getCdate())
          .ccnt(entity.getCcnt())
          .bprice(entity.getBprice())
          .build();
    }
    
    public static Response from(ShopOrderLog entity, String id, 
        String sname, String pname, String sdate) {
      Response response = from(entity);
      if (response != null) {
        response.setId(id);
        response.setPname(pname);
        response.setSname(sname);
        response.setSdate(sdate);
      }
      return response;
    }
    
    public static Response from(ShopOrderLog entity, String id, 
        String sname, 
        String newPname, Integer newCcnt, String pname, String sdate) {
      Response response = from(entity);
      if (response != null) {
        response.setId(id);
        response.setSname(sname);
        response.setNewPname(newPname);
        response.setNewCcnt(newCcnt);
        response.setPname(pname);
        response.setSdate(sdate);
      }
      return response;
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
    private String ono;
    private String word;
    /** 이벤트 종류 (0 결제 / 1 매장연결 / 2 갱신 / 3 취소) */
    private Integer action;
    /** 조회 시작일 (YYYY-MM-DD) */
    private String dateFrom;
    /** 조회 종료일 (YYYY-MM-DD) */
    private String dateTo;
  }
}