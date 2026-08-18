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
 * 유형/제목 검색
 * @param word
 * @param type
 * @param pageable
 * @return
 */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'N' AND n.vmode = 'Y' " +
      "AND (:word IS NULL OR :word = '' OR n.title LIKE %:word% OR n.content LIKE %:word%) " +
      "AND (:type IS NULL OR n.type = :type) "
      + "ORDER BY n.fixyn DESC, n.type ASC, n.cdate DESC")
  Page<Notice> searchAllNotice(
      @Param("word") String word,
      @Param("type") Integer type,
      Pageable pageable);
  
  /**
   * 공지사항 관리자 조회
   * 유형/공개비공개/제목 검색
   * @param word
   * @param type
   * @param vmode
   * @param pageable
   * @return
   */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'N' " +
      "AND (:word IS NULL OR :word = '' OR n.title LIKE %:word% OR n.content LIKE %:word%) " +
      "AND (:type IS NULL OR n.type = :type) " +
      "AND (:vmode IS NULL OR n.vmode = :vmode) "
      + "ORDER BY n.fixyn DESC, n.type ASC, n.cdate DESC")
  Page<Notice> searchAdminNotice(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("vmode") String vmode,
      Pageable pageable);


  // ==========================================
  // ⭐ 이전글 / 다음글 조회
  // ==========================================
  /**
   * 이전글 (현재 글보다 번호가 작은 것 중 가장 가까운 글 = 더 오래된 글)
   * 공개(vmode=Y) & 미삭제(isdel=N) 조건만 대상
   */
  Optional<Notice> findFirstByNoLessThanAndIsdelAndVmodeOrderByNoDesc(
      Long no, String isdel, String vmode);

  /**
   * 다음글 (현재 글보다 번호가 큰 것 중 가장 가까운 글 = 더 최근 글)
   */
  Optional<Notice> findFirstByNoGreaterThanAndIsdelAndVmodeOrderByNoAsc(
      Long no, String isdel, String vmode);
  
  
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
