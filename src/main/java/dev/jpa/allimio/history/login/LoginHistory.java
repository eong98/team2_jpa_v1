package dev.jpa.allimio.history.login;


import dev.jpa.allimio.manager.Manager;
import dev.jpa.allimio.member.Member;
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
@Table(name = "LOGIN_HISTORY")
public class LoginHistory {
  /** 로그인 기록 번호*/
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "login_history_seq_use")
  @SequenceGenerator(name = "login_history_seq_use", sequenceName = "LOGIN_HISTORY_SEQ", allocationSize = 1)
  private Long no;
  
  /** 로그인 시도 아이디*/ 
  private String loginId;
  
  /** 로그인 시도 일시 */
  private String loginDate;
  
  /** 로그인 시도 결과 */
  private int loginResult;
  
  /** 로그인 시도 유형 */
  private int loginType;
  
  /** 회원번호 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mno")
  private Member mno;
  
  /** 관리자 번호 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mnno")
  private Manager mnno;
  
  /** 실패 코드 */
  private String failCode;
  
  /** 실패 사유 */
  private String failReason;
  
  /** 로그인 시도 IP주소 */
  private String ipAddr;
}
