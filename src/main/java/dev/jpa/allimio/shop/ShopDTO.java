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
 
  /** 도면 관리*/ 
  
  //매장 도면 번호
  private Long shopmapno;
  
  //원본 파일명
  private String fname;
  
  //저장 파일명
  private String fsaved;

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

  public ShopDTO(long no, long mno, String title, String zip, String address, String address2, String tel,
      String coment, String phone, String snum, String udate, String cdate, Long shopmapno, String fname,
      String fsaved) {
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
    this.shopmapno = shopmapno;
    this.fname = fname;
    this.fsaved = fsaved;
  
  }

}
