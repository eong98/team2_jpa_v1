package dev.jpa.allimio.shop;

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
public class Shop {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_seq_use")
  @SequenceGenerator(name = "shop_seq_use", sequenceName = "SEQ_SHOP_NO", allocationSize = 1)
  private long no;

  private long mno;
  private String title;
  private String zip;
  private String address;
  private String address2;
  private String tel;

  @Lob
  private String coment;

  private String phone;
  private String snum;
  private String udate;
  private String cdate;

  // 2026-08-11 PAYSTATE(결제상태)/QRIMG(QR이미지) 컬럼은 테이블에서 제거됨.
  // QR이미지는 "고객의소리" 쪽에서 받는 것으로 방향이 바뀌었고, 결제상태도 이 테이블 책임이 아닌 것으로 정리됨.

  public Shop() {

  }

  public Shop(long no, long mno, String title, String zip, String address, String address2,
      String tel, String coment, String phone, String snum, String udate, String cdate) {
    this.no = no;
    this.mno = mno;
    this.title = title;
    this.zip = zip;
    this.address = address;
    this.address2 = address2;
    this.tel = tel;
    this.coment = coment;
    this.phone = phone;
    this.snum = snum;
    this.udate = udate;
    this.cdate = cdate;
  }

}
