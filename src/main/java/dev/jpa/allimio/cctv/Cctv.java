package dev.jpa.allimio.cctv;

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
public class Cctv {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cctv_seq_use")
  @SequenceGenerator(name = "cctv_seq_use", sequenceName = "SEQ_CCTV_NO", allocationSize = 1)
  private long no;

  private long sno;
  private String mac;
  private String represent;
  private String cname;
  private String ckdate;
  private int state;
  private String cdate;

  public Cctv() {

  }

  public Cctv(long no, long sno, String mac, String represent, String cname, String ckdate,
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

}