package dev.jpa.allimio.member.profileimage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
  /** 파일 수정 */
  @Modifying
  @Query(value="UPDATE PROFILE_IMAGE SET UPLOAD_FILENAME=:uploadFilename, STORE_FILENAME=:storeFilename, UPDATE_DATE=:updateDate WHERE mno =:mno", nativeQuery = true)
  public int update_file(@Param("uploadFilename") String uploadFilename,
                                 @Param("storeFilename") String storeFilename,
                                 @Param("mno") Long mno,
                                 @Param("updateDate") String updateDate);
  
  /** 조회 */
  public ProfileImage findByMno_No(Long mno);
}
