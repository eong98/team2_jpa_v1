package dev.jpa.allimio.notice;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface NoticeRepository extends JpaRepository<Notice, Long> {

/**
 * 공지사항 전체 조회
 * @param word
 * @param type
 * @param fixyn
 * @param pageable
 * @return
 */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'N' AND n.vmode = 'Y' " +
      "AND (:word IS NULL OR :word = '' OR n.title LIKE %:word% OR n.content LIKE %:word%) " +
      "AND (:type IS NULL OR n.type = :type) " +
      "AND (:fixyn IS NULL OR n.fixyn = :fixyn) "
      + "ORDER BY n.fixyn DESC, n.type, n.cdate DESC")
  Page<Notice> searchAllNotice(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("fixyn") String fixyn,
      Pageable pageable);
  
  /**
   * 공지사항 관리자 조회
   * @param word
   * @param type
   * @param fixyn
   * @param pageable
   * @return
   */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'N' " +
      "AND (:word IS NULL OR :word = '' OR n.title LIKE %:word% OR n.content LIKE %:word%) " +
      "AND (:type IS NULL OR n.type = :type) " +
      "AND (:fixyn IS NULL OR n.fixyn = :fixyn) "
      + "ORDER BY n.fixyn DESC, n.type, n.cdate DESC")
  Page<Notice> searchAdminNotice(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("fixyn") String fixyn,
      Pageable pageable);

  /***
   * 게시글 삭제용
   * 글 번호(no), 비밀번호(pw), 삭제여부(isdel='N')가 모두 일치하는 게시글 조회
   * @param no
   * @param pw
   * @param isdel
   * @return
   */
  Optional<Notice> findByNoAndPwAndIsdel(Long no, String pw, String isdel);

}
