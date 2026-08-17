package dev.jpa.allimio.member.profileimage;

import org.springframework.stereotype.Service;

import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileImageService {
  private final ProfileImageRepository profileImageRepository;
  /**
   * 1:1 대응되는 테이블 생성(가입시 자동생성), 초기 파일 명은 ""으로 들어감
   * @param memberno 회원 번호
   * @return true, false
   */
  public boolean create(Long memberno) {
    try {
      ProfileImage profileImage = ProfileImage.builder()
          .mno(memberno != null ? Member.builder().no(memberno).build() : null)
          .storeFilename("")
          .uploadFilename("")
          .build();
      
      profileImageRepository.save(profileImage);
      
      return true;
      
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  /** 파일 수정 */
  public int  update_file(String uploadFilename,String storeFilename, long memberno) {
    String now = Tool.getDate();
    
    return profileImageRepository.update_file(uploadFilename, storeFilename, memberno, now);
  }
  
  /** 조회 */
  public ProfileImageDTO findByMemberno(Long memberno) {
ProfileImage profileImage = profileImageRepository.findByMno_No(memberno);
    
    return ProfileImageDTO.builder()
        .no(profileImage.getNo())
        .mno(memberno) 
        .storeFilename(profileImage.getStoreFilename())
        .uploadFilename(profileImage.getUploadFilename())
        .updateDate(profileImage.getUpdateDate())
        .build();
}
}
