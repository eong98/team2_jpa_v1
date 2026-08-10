package dev.jpa.allimio.shop.qna.list;

import dev.jpa.allimio.shop.qna.ShopQna;
import dev.jpa.allimio.shop.qna.cate.ShopQnaCate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString
public class ShopQnaListDTO {
  /** 접수 번호 */
  private Long no;
  
  /** 고객의 소리 번호 */
  private Long qno;

  /** 접수 유형 번호 */
  private Long qcno;
  
  /** 내용 */
  private String content;
  
  /** 체크된 문항 내용 저장 */
  private String blistcont;
  
  /** 등록일 */
  private String cdate;
  
  /** 감정분석 결과 */
  private String aiResult;
  
  /** 감정분석 일시*/
  private String aiDate;
  
  public ShopQnaList toEntity() {
    ShopQna tempShopQna = new ShopQna();
    ShopQnaCate tempShopQnaCate = new ShopQnaCate();
    
    return ShopQnaList.builder()
        .qno(tempShopQna)
        .qcno(tempShopQnaCate)
        .content(this.content)
        .blistcont(this.blistcont)
        .cdate(this.cdate)
        .aiResult(this.aiResult)
        .aiDate(this.aiDate)
        .build();
  }

}
