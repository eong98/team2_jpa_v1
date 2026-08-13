package dev.jpa.allimio.history.login;

import dev.jpa.allimio.manager.Manager;
import dev.jpa.allimio.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString @Builder
public class LoginHistoryDTO {
  /** 로그인 기록 번호*/
  private Long no;
  
  /** 로그인 시도 아이디*/
  private String loginId;
  
  /** 로그인 시도 일시 */
  private String loginDate;
  
  /** 로그인 시도 결과 */
  private int loginResult;
  
  /** 로그인 시도 유형 */
  private int loginType;
  
  /** 회원 번호 */
  private Long mno;
  
  /** 관리자 번호 */
  private Long mnno;
  
  /** 실패 코드 */
  private String failCode;
  
  /** 실패 사유 */
  private String failReason;
  
  /** 로그인 시도 IP주소 */
  private String ipAddr;
  
  public LoginHistory toEntity() {
 

    return LoginHistory.builder()
            .no(this.no)
            .loginId(this.loginId)
            .loginDate(this.loginDate)
            .loginResult(this.loginResult)
            .loginType(this.loginType)
            .mno(this.mno != null ? Member.builder().no(this.mno).build() : null)
            .mnno(this.mnno != null ? Manager.builder().no(this.mnno).build() : null)
            .failCode(this.failCode)
            .failReason(this.failReason)
            .ipAddr(this.ipAddr)
            .build();
  }
}
