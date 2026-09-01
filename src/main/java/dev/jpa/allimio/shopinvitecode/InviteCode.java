package dev.jpa.allimio.shopinvitecode;

import dev.jpa.allimio.shop.Shop;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
  private Long sno;
  
  /** 매장번호(PK와 매칭) */
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "sno", unique = true)
  private Shop shop;
  
  /** 초대 코드(6자리 난수) */
  private String code;
  
  /** 만료 일시 */
  private String expiryDate;
  
  /** 발급 일시 */
  private String cdate;
}
