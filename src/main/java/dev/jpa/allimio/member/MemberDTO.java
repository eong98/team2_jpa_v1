package dev.jpa.allimio.member;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@JsonInclude(JsonInclude.Include.NON_NULL) // 클래스 안의 모든 null 필드 제외
public class MemberDTO {
  /** 회원 번호 */
  private long no;
  
  /** 아이디 */
//  @NotBlank(message = "아이디를 입력하지 않았습니다")
  private String id;
  
  /** 비밀번호 */
//  @NotBlank(message = "비밀번호를 입력하지 않았습니다")
//  @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
  private String password;
  
  /** 이메일 */
//  @NotBlank(message = "이메일을 입력하지 않았습니다")
//  @Email(message = "이메일 형식이 올바르지 않습니다.")
  private String email;
  
  /** 전화번호 */
//  @NotBlank(message = "전화번호를 입력하지 않았습니다")
  private String phone;
    
  /** 이름 */
//  @NotBlank(message = "이름을 입력하지 않았습니다") 
  private String mname;
  
  /** 우편번호 */
//  @NotBlank(message = "우편번호를 입력하지 않았습니다")
  private String zipcode;
  
  /** 기본주소 */
//  @NotBlank(message = "주소를 입력하지 않았습니다")
  private String addr;
  
  /** 상세주소 */
//  @NotBlank(message = "상세주소를 입력하지 않았습니다")
  private String addrDetail;
  
  /** 가입일 */
  private String cdate;
  
  /** 등급 */
  private Integer grade;
  
  /** 상태 */
  private String status;
  
  /** 정보 수정일 */
  private String udate;
  
  /** 이용약관 동의 */
//  @NotBlank(message = "이용약관에 동의해야 합니다")
  private String termsAgreeYn;
  
  /** 개인정보이용 동의 */
//  @NotBlank(message = "이용약관에 동의해야 합니다")
  private String privacyAgreeYn;
  
  /** 국적 */
  private String nation;
  
  /** 새 비밀번호 */
  private String newPassword;
  
  public Member toEntity() {
    return Member.builder()
//        .no(this.no)
        .id(this.id)
        .password(this.password)
        .email(this.email)
        .phone(this.phone)
        .mname(this.mname)
        .zipcode(this.zipcode)
        .addr(this.addr)
        .addrDetail(this.addrDetail)
        .cdate(this.cdate)
        .grade(this.grade)
        .status(this.status)
        .udate(this.udate)
        .termsAgreeYn(this.termsAgreeYn)
        .privacyAgreeYn(this.privacyAgreeYn)
        .nation(this.nation)
        .build();
  }
  
  public static MemberDTO from(Member member) {
    if (member == null) {
        return null;
    }

    MemberDTO dto = new MemberDTO();
    dto.setNo(member.getNo());
    dto.setId(member.getId());
    dto.setEmail(member.getEmail());
    dto.setPhone(member.getPhone());
    dto.setMname(member.getMname());
    dto.setZipcode(member.getZipcode());
    dto.setAddr(member.getAddr());
    dto.setAddrDetail(member.getAddrDetail());
    dto.setCdate(member.getCdate());
    dto.setGrade(member.getGrade());
    dto.setStatus(member.getStatus());
    dto.setUdate(member.getUdate());
    dto.setTermsAgreeYn(member.getTermsAgreeYn());
    dto.setPrivacyAgreeYn(member.getPrivacyAgreeYn());
    dto.setNation(member.getNation());

    return dto;
}

  public MemberDTO(long no, String id, String email, String phone, String mname, String zipcode, String addr,
      String addrDetail, String cdate, Integer grade, String status, String udate, String termsAgreeYn,
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

  public MemberDTO(long no, String id, String email, String phone, String mname, String zipcode, String addr,
      String addrDetail, Integer grade, String status, String udate, String cdate, String nation) {
    this.no = no;
    this.id = id;
    this.email = email;
    this.phone = phone;
    this.mname = mname;
    this.zipcode = zipcode;
    this.addr = addr;
    this.addrDetail = addrDetail;
    this.grade = grade;
    this.status = status;
    this.udate = udate;
    this.cdate = cdate;
    this.nation = nation;
  }
  
  
  
}
