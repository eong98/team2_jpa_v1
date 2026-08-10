package dev.jpa.allimio.shopmenu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장관리(/user) 사이드바 메뉴 - InMenu(관리자 /dbms 메뉴)와 컬럼 구조 동일.
 * SHOP_MENU / SEQ_SHOP_MENU_NO (Oracle DDL은 sql/shop_menu.sql 참고)
 */
@Entity
@Getter
@Setter
@ToString
public class ShopMenu {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_menu_seq_use")
  @SequenceGenerator(name = "shop_menu_seq_use", sequenceName = "SEQ_SHOP_MENU_NO", allocationSize = 1)
  private long no;

  private Long fkno;
  private int dept;
  private int ord;
  private String title;
  private String purl;
  private String tname;
  private String mname;

  // IN_MENU와 동일하게 실제 컬럼명이 USEYN(언더스코어 없음)이라 명시적으로 매핑
  @Column(name = "USEYN")
  private String useYn;

  private String cdate;

  public ShopMenu() {

  }

  public ShopMenu(long no, Long fkno, int dept, int ord, String title, String purl, String tname,
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
