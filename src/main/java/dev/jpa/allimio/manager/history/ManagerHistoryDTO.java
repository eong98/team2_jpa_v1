package dev.jpa.allimio.manager.history;

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
public class ManagerHistoryDTO {
  /** 로그인 기록 번호*/
  private Long no;     
  
  /** 로그인 시도 아이디*/
  private String loginId;
  
  /** 로그인 시도 일시 */
  private String loginDate;
  
  /** 로그인 시도 결과 */
  private int loginResult;
  
  /** 관리자 번호 */
  private Long mnno;      
  
  /** 실패 사유 */
  private String failReason;
  
  /** 로그인 시도 IP주소 */
  private String ipAddr;
  
  public ManagerHistory toEntity() {
 

    return ManagerHistory.builder()
            .no(this.no)
            .loginId(this.loginId)
            .loginDate(this.loginDate)
            .loginResult(this.loginResult)
            .mnno(this.mnno)
            .failReason(this.failReason)
            .ipAddr(this.ipAddr)
            .build();
  }
}
