package dev.jpa.allimio.notice;

import java.util.List;
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


  /**
   * [관리자] 삭제된(소프트 삭제) 공지사항 목록 + 검색 — 삭제일 최신순
   */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'Y' " +
      "AND (:word IS NULL OR :word = '' OR n.title LIKE %:word% OR n.content LIKE %:word%) " +
      "AND (:type IS NULL OR n.type = :type) " +
      "ORDER BY n.ddate DESC, n.no DESC")
  Page<Notice> searchDeletedNotice(
      @Param("word") String word,
      @Param("type") Integer type,
      Pageable pageable);

  // ==========================================
  // ⭐ 이전글 / 다음글 조회
  // ==========================================
  /**
   * 이전글 — 목록(고정글 먼저 → 유형 → 등록일 최신순 → 글번호 큰 순)에서 바로 위에 보이는 글
   * 공개(vmode=Y)·미삭제만 (사용자 목록과 같은 대상), PageRequest.of(0, 1)로 1건
   */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'N' AND n.vmode = 'Y' " +
      "AND (n.fixyn > :fixyn OR (n.fixyn = :fixyn AND (n.type < :type OR (n.type = :type " +
      "AND (n.cdate > :cdate OR (n.cdate = :cdate AND n.no > :no)))))) " +
      "ORDER BY n.fixyn ASC, n.type DESC, n.cdate ASC, n.no ASC")
  List<Notice> findAboveInList(@Param("fixyn") String fixyn, @Param("type") int type,
      @Param("cdate") String cdate, @Param("no") Long no, Pageable pageable);

  /**
   * 다음글 — 목록에서 바로 아래에 보이는 글
   */
  @Query("SELECT n FROM Notice n WHERE n.isdel = 'N' AND n.vmode = 'Y' " +
      "AND (n.fixyn < :fixyn OR (n.fixyn = :fixyn AND (n.type > :type OR (n.type = :type " +
      "AND (n.cdate < :cdate OR (n.cdate = :cdate AND n.no < :no)))))) " +
      "ORDER BY n.fixyn DESC, n.type ASC, n.cdate DESC, n.no DESC")
  List<Notice> findBelowInList(@Param("fixyn") String fixyn, @Param("type") int type,
      @Param("cdate") String cdate, @Param("no") Long no, Pageable pageable);

  /**
   * 다음글 (현재 글보다 번호가 큰 것 중 가장 가까운 글 = 더 최근 글)
   */
  Optional<Notice> findFirstByNoGreaterThanAndIsdelAndVmodeOrderByNoAsc(
      Long no, String isdel, String vmode);
  
  
  // 게시글 비밀번호는 BCrypt로 암호화 저장 → DB 조건(=)으로 비교할 수 없어서
  // 예전 findByNoAndPwAndIsdel(평문 비교)은 삭제함. 삭제 확인은 NoticeService.deleteNotice에서 pwEncoder.matches로 처리

}
