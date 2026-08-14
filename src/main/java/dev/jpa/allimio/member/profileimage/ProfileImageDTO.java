package dev.jpa.allimio.member.profileimage;

import dev.jpa.allimio.member.Member;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
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
public class ProfileImageDTO {
  /** 이미지 번호 */
  private Long no;
  
  /** 회원 번호 */
  private Long mno;
  
  /** 저장된 파일명 */
  private String storeFilename;
  
  /** 원본 파일명 */
  private String uploadFilename;
  
  /** 변경일 */
  private String updateDate;

  public ProfileImage toEntity() {
    return ProfileImage.builder()
        .no(this.no)
        .mno(this.mno != null ? Member.builder().no(this.mno).build() : null)
        .storeFilename(this.storeFilename)
        .uploadFilename(this.uploadFilename)
        .updateDate(this.updateDate)
        .build();
  }
}
