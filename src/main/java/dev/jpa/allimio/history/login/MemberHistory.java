package dev.jpa.allimio.history.login;


import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity @Getter @Setter @ToString @Builder @AllArgsConstructor @NoArgsConstructor
@Table(name = "MEMBER_HISTORY")
public class MemberHistory {
  /** 로그인 기록 번호*/
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_history_seq_use")
  @SequenceGenerator(name = "member_history_seq_use", sequenceName = "MEMBER_HISTORY_SEQ", allocationSize = 1)
  private Long no;
  
  /** 로그인 시도 아이디*/
  private String loginId;
  
  /** 로그인 시도 일시 */
  private String loginDate;
  
  /** 로그인 시도 결과 */
  private int loginResult;
  
  /** 회원번호 */
  private Long mno;
  
  /** 실패 사유 */
  private String failReason;
  
  /** 로그인 시도 IP주소 */
  private String ipAddr;
}
