package dev.jpa.allimio.shopmember;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ShopMemberDTO {
  /** 소속 번호 */
  private Long no;
  
  /** 매장 번호 */
  private Long sno;
  
  /** 회원 번호 */
  private Long mno;
  
  /** 등록일 */
  private String cdate;
  
  public ShopMember toEntity() {
    return ShopMember.builder()
        .no(this.no)
        .sno(this.sno)
        .mno(this.mno)
        .cdate(this.cdate)
        .build();
  }

}
