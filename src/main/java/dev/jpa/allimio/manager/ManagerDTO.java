package dev.jpa.allimio.manager;

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
public class ManagerDTO {
  /** 관리자 번호 */
  private Long no;
  
  @NotBlank(message = "아이디를 입력하지 않았습니다")
  /** 관리자 ID */
  private String id;
  
  @NotBlank(message = "비밀번호를 입력하지 않았습니다")
  @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
  /** 관리자 비밀번호 */
  private String password;
  
  @NotBlank(message = "이름을 입력하지 않았습니다")
  /** 관리자 이름*/
  private String mname;
  
  @NotBlank(message = "이메일을 입력하지 않았습니다")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  /** 관리자 이메일 */
  private String email;
  
  @NotBlank(message = "전화번호를 입력하지 않았습니다")
  /** 관리자 전화번호*/
  private String phone;
  
  /** 관리자 등급 */
  private Integer grade;
  
  /**관리자 상태 */
  private String status;
  
  /** 계정 생성일 */
  private String cdate;
  
  /** 정보 수정일 */
  private String udate;
  
  /** 새 비밀번호 */
  private String newPassword;
  
  public Manager toEntity() {
    return Manager.builder()
//        .no(this.no)
        .id(this.id)
        .password(this.password)
        .mname(this.mname)
        .email(this.email)
        .phone(this.phone)
        .grade(this.grade)
        .status(this.status)
        .cdate(this.cdate)
        .udate(this.udate)
        .build();
  }
  
  public static ManagerDTO from(Manager manager) {
    if (manager == null) {
      return null;
    }
    
    ManagerDTO dto = new ManagerDTO();
    dto.setNo(manager.getNo());
    dto.setId(manager.getId());
    dto.setMname(manager.getMname());
    dto.setEmail(manager.getEmail());
    dto.setPhone(manager.getPhone());
    dto.setGrade(manager.getGrade());
    dto.setStatus(manager.getStatus());
    dto.setCdate(manager.getCdate());
    dto.setUdate(manager.getUdate());
    
    return dto;
    
  }

  public ManagerDTO(Long no, String id, String mname, String email, String phone, Integer grade, String status, String udate, String cdate) {
    this.no = no;
    this.id = id;
    this.mname = mname;
    this.email = email;
    this.phone = phone;
    this.grade = grade;
    this.status = status;
    this.udate = udate;
    this.cdate = cdate;
  }
  
  
  
}
