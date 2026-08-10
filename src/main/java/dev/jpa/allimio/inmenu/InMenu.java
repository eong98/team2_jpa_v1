package dev.jpa.allimio.inmenu;

import jakarta.persistence.Column;
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

  // 실제 컬럼명이 USEYN(언더스코어 없음)이라 Hibernate 기본 네이밍전략(use_yn)과
  // 어긋나서 명시적으로 매핑해줘야 함
  @Column(name = "USEYN")
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