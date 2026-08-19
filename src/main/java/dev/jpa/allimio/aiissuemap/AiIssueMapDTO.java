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

  public AiIssueMapDTO() {
  }

  public AiIssueMapDTO(
      long no,
      long mno,
      long smno,
      Double xpos,
      Double ypos,
      String color,
      String fsaved,
      int status,
      String err,
      String cdate) {

    this.no = no;
    this.mno = mno;
    this.smno = smno;
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
        no,
        mno,
        smno,
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