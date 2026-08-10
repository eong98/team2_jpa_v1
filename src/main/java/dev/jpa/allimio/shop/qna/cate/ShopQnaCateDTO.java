package dev.jpa.allimio.shop.qna.cate;

import dev.jpa.allimio.shop.qna.ShopQna;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString @Builder
public class ShopQnaCateDTO {
  /** 접수 유형 번호 */
  private Long no;
   
  /** 고객의 소리 번호 */
  private Long qno;
  
  /** 문항 제목 */
  private String title;
  
  /** 카테고리 타입 */
  private String ctype;
  
  /** 문항 */
  private String blist;
  
  /** 등록일 */
  private String cdate;
  
  public ShopQnaCate toEntity() {
    ShopQna tempShopQna = new ShopQna();
    tempShopQna.setNo(this.qno);
    
    return ShopQnaCate.builder()
        .qno(tempShopQna)
        .title(this.title)
        .ctype(this.ctype)
        .blist(this.blist)
        .cdate(this.cdate)
        .build();
  }
}
