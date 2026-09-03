package dev.jpa.allimio.shopinvitecode;

import dev.jpa.allimio.shop.Shop;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "shop") // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SHOP_INVITE_CODE")
public class InviteCode {
  /** 매장 번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_invite_seq_use")
  @SequenceGenerator(name = "shop_invite_seq_use", sequenceName = "SHOP_INVITE_SEQ", allocationSize = 1)
  private Long no;
  
  /** 매장번호(PK와 매칭) */
  private Long sno;
  
  /** 초대 코드(6자리 난수) */
  private String code;
  
  /** 만료 일시 */
  private String expiryDate;
  
  /** 발급 일시 */
  private String cdate;
}
