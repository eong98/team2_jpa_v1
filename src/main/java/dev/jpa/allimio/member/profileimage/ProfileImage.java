package dev.jpa.allimio.member.profileimage;

import dev.jpa.allimio.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "member") // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROFILE_IMAGE")
public class ProfileImage {
  /** 회원 번호 (PK) */
  @Id
  private Long mno;
  
  /** 회원 번호(FK이자 PK와 매핑) */
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "mno", unique = true) 
  private Member member;
  
  /** 저장된 파일명 */
  @Builder.Default
  private String storeFilename = "";
  
  /** 원본 파일명 */
  @Builder.Default
  private String uploadFilename = "";
  
  /** 변경일 */
  private String updateDate;
}
