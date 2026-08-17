package dev.jpa.allimio.member.profileimage;

import dev.jpa.allimio.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROFILE_IMAGE")
public class ProfileImage {
  /** 이미지 번호 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_image_seq_use")
  @SequenceGenerator(name = "profile_image_seq_use", sequenceName = "PROFILE_IMAGE_SEQ", allocationSize = 1)
  private Long no;
  
  /** 회원 번호 */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mno", unique = true) 
  private Member mno;
  
  /** 저장된 파일명 */
  @Builder.Default
  private String storeFilename = "";
  
  /** 원본 파일명 */
  @Builder.Default
  private String uploadFilename = "";
  
  /** 변경일 */
  private String updateDate;
}
