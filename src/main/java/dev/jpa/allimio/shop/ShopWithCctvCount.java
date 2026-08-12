package dev.jpa.allimio.shop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/* ---------------------------------------------------------------------
   /shop/search(사용자 매장 목록, /user/shop) 응답 전용 DTO.

   Shop 엔티티 필드를 그대로 담고, 여기에 CCTV 등록 대수(cctvCount)만 하나 붙여서
   내려줍니다. Shop 엔티티/테이블 자체엔 CCTV 개수 컬럼이 없고, CCTV 테이블에서
   COUNT(SNO)로 매번 계산해서 붙여주는 방식입니다(ShopService.attachCctvCount()).
--------------------------------------------------------------------- */
@Getter
@Setter
@ToString
public class ShopWithCctvCount {

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

  /** 이 매장(sno) 소유로 등록된 CCTV 대수 */
  private long cctvCount;

  public ShopWithCctvCount() {

  }

  public ShopWithCctvCount(Shop shop, long cctvCount) {
    this.no = shop.getNo();
    this.mno = shop.getMno();
    this.title = shop.getTitle();
    this.zip = shop.getZip();
    this.address = shop.getAddress();
    this.address2 = shop.getAddress2();
    this.tel = shop.getTel();
    this.coment = shop.getComent();
    this.phone = shop.getPhone();
    this.snum = shop.getSnum();
    this.udate = shop.getUdate();
    this.cdate = shop.getCdate();
    this.cctvCount = cctvCount;
  }

}
