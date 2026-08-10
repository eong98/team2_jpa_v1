package dev.jpa.allimio.shop.qna.list;

import dev.jpa.allimio.shop.qna.ShopQna;
import dev.jpa.allimio.shop.qna.cate.ShopQnaCate;
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
@Table(name = "ShopQnaList")
public class ShopQnaList {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_qna_list_seq_use")
  @SequenceGenerator(name = "shio_qna_list_seq_use", sequenceName = "SHOP_QNA_LIST_SEQ", allocationSize = 1)
  /** 접수 번호 */
  private Long no;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "qno")
  /** 고객의 소리 번호 */
  private ShopQna qno;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "qcno")
  /** 접수 유형 번호 */
  private ShopQnaCate qcno;
  
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
}
