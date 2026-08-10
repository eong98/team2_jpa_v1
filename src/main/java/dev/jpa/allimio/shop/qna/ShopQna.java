package dev.jpa.allimio.shop.qna;

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
@Table(name = "SHOP_QNA")
public class ShopQna {
  /** 로그인 기록 번호*/
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_qna_seq_use")
  @SequenceGenerator(name = "shop_qna_seq_use", sequenceName = "Shop_Qna_SEQ", allocationSize = 1)
  private Long no;
  
  /** 매장 번호 */
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "sno") 
  private Long sno;             // 병합 후 제거
//  private Shop sno;         // 병합 후 사용
  
  /** 제목 */
  private String title;
}
