package dev.jpa.allimio.shop.qna.cate;

import dev.jpa.allimio.shop.qna.ShopQna;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity @Getter @Setter @ToString @Builder @AllArgsConstructor @NoArgsConstructor
@Table(name = "SHOP_QNA_CATE")
public class ShopQnaCate {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_qna_cate_seq_use")
  @SequenceGenerator(name = "shop_qna_cate_seq_use", sequenceName = "SHOP_QNA_CATE_SEQ", allocationSize = 1)
  /** 접수 유형 번호 */
  private Long no;
   
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "qno")
  /** 고객의 소리 번호 */
  private ShopQna qno;
  
  /** 문항 제목 */
  private String title;
  
  /** 카테고리 타입 */
  private String ctype;
  
  /** 문항 */
  private String blist;
  
  /** 등록일 */
  private String cdate;
}
