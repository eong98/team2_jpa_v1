package dev.jpa.allimio.cctvvisitor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class CctvVisitor {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cctv_visitor_seq_use")
  @SequenceGenerator(name = "cctv_visitor_seq_use", sequenceName = "SEQ_CCTV_VISITOR_NO", allocationSize = 1)
  private long no;

  private long cno;
  private String trackId;
  private String intime;
  private String outtime;
  private int staytime;
  private int state;
  private String cdate;

  public CctvVisitor() {

  }

  public CctvVisitor(long no, long cno, String trackId, String intime, String outtime,
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

}