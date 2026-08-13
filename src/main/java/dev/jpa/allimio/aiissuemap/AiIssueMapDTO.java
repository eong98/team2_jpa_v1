package dev.jpa.allimio.aiissuemap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 이슈 도면 DTO
 */
@Getter
@Setter
@ToString
public class AiIssueMapDTO {

  private long aiissueMapno;

  private long mno;

  private long shopmapno;

  private Double xpos;

  private Double ypos;

  private String color;

  private String fsaved;

  private int status;

  private String err;

  private String cdate;

  public AiIssueMapDTO() {
  }

  public AiIssueMapDTO(
      long aiissueMapno,
      long mno,
      long shopmapno,
      Double xpos,
      Double ypos,
      String color,
      String fsaved,
      int status,
      String err,
      String cdate) {

    this.aiissueMapno = aiissueMapno;
    this.mno = mno;
    this.shopmapno = shopmapno;
    this.xpos = xpos;
    this.ypos = ypos;
    this.color = color;
    this.fsaved = fsaved;
    this.status = status;
    this.err = err;
    this.cdate = cdate;
  }

  public AiIssueMap toEntity() {

    return new AiIssueMap(
        aiissueMapno,
        mno,
        shopmapno,
        xpos,
        ypos,
        color,
        fsaved,
        status,
        err,
        cdate
    );
  }
}