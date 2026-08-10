package dev.jpa.allimio.cctvissue;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class CctvIssue {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cctv_issue_seq_use")
  @SequenceGenerator(name = "cctv_issue_seq_use", sequenceName = "SEQ_CCTV_ISSUE_NO", allocationSize = 1)
  private long no;

  private long cno;
  private long mno;
  private String code;
  private int state;

  @Lob
  private String comnet;

  private String reliability;
  private String pdate;
  private String noticeyn;
  private String cdate;

  public CctvIssue() {

  }

  public CctvIssue(long no, long cno, long mno, String code, int state, String comnet,
      String reliability, String pdate, String noticeyn, String cdate) {
    this.no = no;
    this.cno = cno;
    this.mno = mno;
    this.code = code;
    this.state = state;
    this.comnet = comnet;
    this.reliability = reliability;
    this.pdate = pdate;
    this.noticeyn = noticeyn;
    this.cdate = cdate;
  }

}