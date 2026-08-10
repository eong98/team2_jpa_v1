package dev.jpa.allimio.shop.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString
public class ShopQnaDTO {
  /** 로그인 기록 번호*/
  private Long no;
  
  /** 매장 번호 */
  private Long sno;  
  
  /** 제목 */
  private String title;
  
  public ShopQna toEntity() {
//    Shop tempShop = new Shop();      // 병합 후 사용
//    tempShop.setNo(sno);                          // 병합 후 사용
    
    return ShopQna.builder()
//        .sno(tempShop)       // 병합 후 사용
        .sno(this.sno)                  // 병합 후 삭제
        .title(this.title)
        .build();
  }
}
