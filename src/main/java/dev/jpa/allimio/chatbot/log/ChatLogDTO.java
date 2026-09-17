package dev.jpa.allimio.chatbot.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatLogDTO {

  /** 로그 등록 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {
    private String sno;
    private Integer sender;
    private Integer mtype;
    private String content;
    private Long cno;
  }

  /** 로그 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long no;
    private String sno;
    private Integer sender;
    private Integer mtype;
    private String content;
    private Long cno;
    private String cdate;

    public static Response from(ChatLog entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .sno(entity.getSno())
          .sender(entity.getSender())
          .mtype(entity.getMtype())
          .content(entity.getContent())
          .cno(entity.getCno())
          .cdate(entity.getCdate())
          .build();
    }
  }
}