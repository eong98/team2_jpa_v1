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
  private String snum;
  private String udate;
  private String cdate;

  // 2026-08-11 PAYSTATE(결제상태)/QRIMG(QR이미지) 컬럼은 테이블에서 제거됨.

  public ShopDTO() {

  }

  public ShopDTO(long no, long mno, String title, String zip, String address, String address2,
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

  public Shop toEntity() {
    return new Shop(no, mno, title, zip, address, address2, tel, coment, phone, snum, udate, cdate);
  }

}
