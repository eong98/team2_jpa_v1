package dev.jpa.allimio.manager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Manager {
  /** 관리자 번호 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manager_seq_use")
  @SequenceGenerator(name = "manager_seq_use", sequenceName = "MANAGER_SEQ", allocationSize = 1)
  private Long no;
  
  /** 관리자 ID */
  private String id;
  
  /** 관리자 비밀번호 */
  private String password;
  
  /** 관리자 이름*/
  private String mname;
  
  /** 관리자 이메일 */
  private String email;
  
  /** 관리자 전화번호*/
  private String phone;
  
  /** 관리자 등급 */
  private Integer grade;
  
  /**관리자 상태 */
  private String status;
  
  /** 계정 생성일 */
  private String cdate;
  
  /** 계정 정보 수정일 */
  private String udate;

  public void updateByManager(String mname, String email, String phone, Integer grade, String status, String udate) {
    this.mname = mname;
    this.email = email;
    this.phone = phone;
    this.grade = grade;
    this.status = status;
    this.udate = udate;
  }

  public void updateBySelf(String mname, String email, String phone, String udate) {
    this.mname = mname;
    this.email = email;
    this.phone = phone;
    this.udate = udate;
  }
  
  
 
}
