package dev.jpa.allimio.cctv;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CctvDTO {

  private long no;
  private long sno;
  private String mac;
  private String represent;
  private String cname;
  private String ckdate;
  private int state;
  private String cdate;

  public CctvDTO() {

  }

  public CctvDTO(long no, long sno, String mac, String represent, String cname, String ckdate,
      int state, String cdate) {
    this.no = no;
    this.sno = sno;
    this.mac = mac;
    this.represent = represent;
    this.cname = cname;
    this.ckdate = ckdate;
    this.state = state;
    this.cdate = cdate;
  }

  public Cctv toEntity() {
    return new Cctv(no, sno, mac, represent, cname, ckdate, state, cdate);
  }

}