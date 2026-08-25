package dev.jpa.allimio.member.token;

import dev.jpa.allimio.member.Member;
import jakarta.persistence.Column;
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

@Entity
@Table(name = "PASSWORD_RESET_TOKEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
  
  /** 회원 번호 */
  @Id
  @Column(name = "MNO")
  private Long mno;

  /** 회원 번호(FK이자 PK와 매핑) */
  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MNO")
  private Member member;

  /** 토큰 */
  private String token;
  
  /** 만료 일시 */
  private String expiryDate;
  
  /** 발급 일시 */
  private String cdate;
}
