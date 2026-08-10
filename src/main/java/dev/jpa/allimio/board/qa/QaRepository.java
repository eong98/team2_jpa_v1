package dev.jpa.allimio.board.qa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QaRepository extends JpaRepository<Qa, Long> {
  //==========================================
  // [작성자] 1:1 문의 내역 조회
  // ==========================================
  /***
   * 내 문의내역 전체조회 + 검색조회
   * 
   * @param mno
   * @param word
   * @param pageable
   * @return
   */
  @Query("SELECT q FROM Qa q WHERE q.mno = :mno AND q.isdel = 'N' AND q.isfaq = 'N' "
      + "AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%) "
      + "ORDER BY q.cdate DESC")
  Page<Qa> searchMyQuestions(@Param("mno") Long mno, @Param("word") String word, Pageable pageable);

  //==========================================
  // [관리자 조회]
  // ==========================================

  /***
   * 회원 문의내역 전체조회 + 검색조회
   * @param keyword
   * @param pageable
   * @return
   */
  @Query("SELECT q FROM Qa q WHERE q.isdel = 'N' AND q.isfaq = 'N' " +
      "AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%) " +
      "AND (:type IS NULL OR q.type = :type) " +
      "AND (:status IS NULL OR q.status = :status) " +
      "AND (:mno IS NULL OR q.mno = :mno)"
      + "ORDER BY q.cdate DESC")
  Page<Qa> searchAllQuestionsAdmin(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("status") Integer status,
      @Param("mno") Long mno,
      Pageable pageable);

  /***
   * FAQ 게시글 전체조회 + 검색조회
   * @param isdel
   * @param type
   * @param pageable
   * @return
   */
  @Query("SELECT q FROM Qa q WHERE q.isfaq = 'Y' AND q.isdel = 'N' " +
      "AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%) " +
      "AND (:type IS NULL OR q.type = :type) " +
      "AND (:status IS NULL OR q.status = :status) " +
      "AND (:mno IS NULL OR q.mno = :mno)" 
      + "ORDER BY q.vseq ASC, q.cdate DESC")
  Page<Qa> searchFaqsAdmin(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("status") Integer status,
      @Param("mno") Long mno,
      Pageable pageable);
  

  /***
   * 게시글 삭제용
   * 글 번호(no), 비밀번호(pw), 삭제여부(isdel='N')가 모두 일치하는 게시글 조회
   * @param no
   * @param pw
   * @param isdel
   * @return
   */
  Optional<Qa> findByNoAndPwAndIsdel(Long no, String pw, String isdel);
}
