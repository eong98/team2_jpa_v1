package dev.jpa.allimio.shop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShopDTO {

  private long no;
  private long mno;
  private String title;
  private String zip;
  private String address;
  private String address2;
  private String tel;
  private String coment;
  private String phone;
  private String paystate;
  private String qrimg;
  private String snum;
  private String udate;
  private String cdate;

  public ShopDTO() {

  }

  public ShopDTO(long no, long mno, String title, String zip, String address, String address2,
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

  public Shop toEntity() {
    return new Shop(no, mno, title, zip, address, address2, tel, coment, phone, paystate,
        qrimg, snum, udate, cdate);
  }

}