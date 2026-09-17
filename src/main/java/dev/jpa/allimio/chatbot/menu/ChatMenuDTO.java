package dev.jpa.allimio.chatbot.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatMenuDTO {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  /** 메뉴 등록/수정 요청 */
  public static class Request {
    private Long pno;
    private Integer step;
    private String label;
    private String answer;
    @Builder.Default
    private String userag = "N";
    @Builder.Default
    private Integer vseq = 0;
    @Builder.Default
    private String useyn = "Y";
  }
  

  /** 메뉴 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long no;
    private Long pno;
    private Integer step;
    private String label;
    private String answer;
    private String userag;
    private Integer vseq;
    private String useyn;
    private String cdate;

    /** 하위 선택지 존재 여부 — 조회 시점에 계산해서 채움(별도 컬럼 아님) */
    private Boolean hasChildren;

    public static Response from(ChatMenu entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .pno(entity.getPno())
          .step(entity.getStep())
          .label(entity.getLabel())
          .answer(entity.getAnswer())
          .userag(entity.getUserag())
          .vseq(entity.getVseq())
          .useyn(entity.getUseyn())
          .cdate(entity.getCdate())
          .build();
    }

    public static Response from(ChatMenu entity, boolean hasChildren) {
      Response response = from(entity);
      if (response != null) {
        response.setHasChildren(hasChildren);
      }
      return response;
    }
  }
  
}
