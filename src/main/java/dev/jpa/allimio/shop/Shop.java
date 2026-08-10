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
  private String paystate;
  private String qrimg;
  private String snum;
  private String udate;
  private String cdate;

  public Shop() {

  }

  public Shop(long no, long mno, String title, String zip, String address, String address2,
      String tel, String coment, String phone, String paystate, String qrimg, String snum,
      String udate, String cdate) {
    this.no = no;
    this.mno = mno;
    this.title = title;
    this.zip = zip;
    this.address = address;
    this.address2 = address2;
    this.tel = tel;
    this.coment = coment;
    this.phone = phone;
    this.paystate = paystate;
    this.qrimg = qrimg;
    this.snum = snum;
    this.udate = udate;
    this.cdate = cdate;
  }

}