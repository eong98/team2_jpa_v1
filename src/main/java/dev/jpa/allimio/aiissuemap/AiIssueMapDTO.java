package dev.jpa.allimio.aiissuemap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 이슈 도면 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AiIssueMapDTO {

  private long no;

  private long mno;

  private long smno;

  private Double xpos;

  private Double ypos;

  private String color;

  private String fsaved;

  private int status;

  private String err;

  private String cdate;

}