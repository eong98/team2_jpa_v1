package dev.jpa.allimio.member;

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
public class Member {
  /** 회원번호 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq_use")
  @SequenceGenerator(name = "member_seq_use", sequenceName = "MEMBER_SEQ", allocationSize = 1)
  private Long no;

  /** 아이디 */
  private String id;

  /** 비밀번호 */
  private String password;
  
  /** 이름 */
  private String mname;

  /** 이메일 */
  private String email;

  /** 전화번호 */
  private String phone;

  /** 우편번호 */
  private String zipcode;

  /** 기본주소 */
  private String addr;

  /** 상세주소 */
  private String addrDetail;

  /** 가입일 */
  private String cdate;

  /** 등급 */
  private int grade;

  /** 상태 */
  private String status;

  /** 정보 수정일 */
  private String udate;

  /** 이용약관 동의 */
  private String termsAgreeYn;

  /** 개인정보이용 동의 */
  private String privacyAgreeYn;

  /** 국적 */
  private String nation;

  public void updateByManager(String mname, int grade, String phone, String email, String zipcode, String addr,
      String addrDetail, String status, String udate) {
    this.mname = mname;
    this.grade = grade;
    this.phone = phone;
    this.email = email;
    this.zipcode = zipcode;
    this.addr = addr;
    this.addrDetail = addrDetail;
    this.status = status;
    this.udate = udate;
  }
  
  public void updateBySelf(String mname, String phone, String email, String zipcode, String addr,
      String addrDetail, String udate) {
    this.mname = mname;
    this.phone = phone;
    this.email = email;
    this.zipcode = zipcode;
    this.addr = addr;
    this.addrDetail = addrDetail;
    this.udate = udate;
  }

  public Member(long no, String id, String email, String phone, String mname, String zipcode, String addr,
      String addrDetail, String cdate, int grade, String status, String udate, String termsAgreeYn,
      String privacyAgreeYn, String nation) {
    this.no = no;
    this.id = id;
    this.email = email;
    this.phone = phone;
    this.mname = mname;
    this.zipcode = zipcode;
    this.addr = addr;
    this.addrDetail = addrDetail;
    this.cdate = cdate;
    this.grade = grade;
    this.status = status;
    this.udate = udate;
    this.termsAgreeYn = termsAgreeYn;
    this.privacyAgreeYn = privacyAgreeYn;
    this.nation = nation;
  }
  
  

}
