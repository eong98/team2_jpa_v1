package dev.jpa.allimio.cctvvisitor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CctvVisitorDTO {

  private long no;
  private long cno;
  private String trackId;
  private String intime;
  private String outtime;
  private int staytime;
  private int state;
  private String cdate;

  public CctvVisitorDTO() {

  }

  public CctvVisitorDTO(long no, long cno, String trackId, String intime, String outtime,
      int staytime, int state, String cdate) {
    this.no = no;
    this.cno = cno;
    this.trackId = trackId;
    this.intime = intime;
    this.outtime = outtime;
    this.staytime = staytime;
    this.state = state;
    this.cdate = cdate;
  }

  public CctvVisitor toEntity() {
    return new CctvVisitor(no, cno, trackId, intime, outtime, staytime, state, cdate);
  }

}