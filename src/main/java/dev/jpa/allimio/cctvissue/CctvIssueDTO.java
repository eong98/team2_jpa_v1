package dev.jpa.allimio.cctvissue;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CctvIssueDTO {

  private long no;
  private long cno;
  private long mno;
  private String code;
  private int state;
  private String comnet;
  private String reliability;
  private String pdate;
  private String noticeyn;
  private String cdate;

  public CctvIssueDTO() {

  }

  public CctvIssueDTO(long no, long cno, long mno, String code, int state, String comnet,
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

  public CctvIssue toEntity() {
    return new CctvIssue(no, cno, mno, code, state, comnet, reliability, pdate, noticeyn, cdate);
  }

}