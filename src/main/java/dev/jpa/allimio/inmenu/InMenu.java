package dev.jpa.allimio.inmenu;

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
public class InMenu {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_menu_seq_use")
  @SequenceGenerator(name = "in_menu_seq_use", sequenceName = "SEQ_IN_MENU_NO", allocationSize = 1)
  private long no;

  private Long fkno;
  private int dept;
  private int ord;
  private String title;
  private String purl;
  private String tname;
  private String mname;
  private String useYn;
  private String cdate;

  public InMenu() {

  }

  public InMenu(long no, Long fkno, int dept, int ord, String title, String purl, String tname,
      String mname, String useYn, String cdate) {
    this.no = no;
    this.fkno = fkno;
    this.dept = dept;
    this.ord = ord;
    this.title = title;
    this.purl = purl;
    this.tname = tname;
    this.mname = mname;
    this.useYn = useYn;
    this.cdate = cdate;
  }

}
