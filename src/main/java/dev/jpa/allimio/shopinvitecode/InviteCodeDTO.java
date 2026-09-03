package dev.jpa.allimio.shopinvitecode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class InviteCodeDTO {
  /** 코드 번호 */
  private Long no;
  
  /** 매장 번호 */
  private Long sno;
  
  /** 초대 코드 */
  private String code;
  
  /** 만료 일시 */
  private String expiryDate;
  
  /** 등록 일시 */
  private String cdate;
  
  public InviteCode toEntity() {
    return InviteCode.builder()
        .no(no)
        .sno(this.sno)
        .code(this.code)
        .expiryDate(this.expiryDate)
        .cdate(this.cdate)
        .build();
  }
  
  //======================================
  // 초대 요청 받을 DTO
  //======================================
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @ToString
  public static class Request {
      
      /** 회원번호*/
      private Long mno;
      
      /** 초대 코드 */
      private String code;
  }
}
