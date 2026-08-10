package dev.jpa.allimio.shopmenu;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShopMenuDTO {

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

  public ShopMenuDTO() {

  }

  public ShopMenuDTO(long no, Long fkno, int dept, int ord, String title, String purl, String tname,
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

  public ShopMenu toEntity() {
    return new ShopMenu(no, fkno, dept, ord, title, purl, tname, mname, useYn, cdate);
  }

}
