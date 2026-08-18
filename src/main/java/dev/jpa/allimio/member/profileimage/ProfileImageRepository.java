package dev.jpa.allimio.member.profileimage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    /** 
     * 파일 수정 (Native Query 유지 시)
     * @Modifying(clearAutomatically = true)를 추가하면 1차 캐시 동기화에 유리합니다.
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE PROFILE_IMAGE " +
                   "SET UPLOAD_FILENAME = :uploadFilename, " +
                   "    STORE_FILENAME = :storeFilename, " +
                   "    UPDATE_DATE = :updateDate " +
                   "WHERE MNO = :mno", 
           nativeQuery = true)
    int update_file(@Param("uploadFilename") String uploadFilename,
                    @Param("storeFilename") String storeFilename,
                    @Param("mno") Long mno,
                    @Param("updateDate") String updateDate);

}