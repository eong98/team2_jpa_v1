package dev.jpa.allimio.qa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.allimio.qa.QaDTO.QaResponse;

public interface QaRepository extends JpaRepository<Qa, Long> {
  // ==========================================
  // ⭐ 이전글 / 다음글 조회
  // ==========================================
  /**
   * 이전글 — 목록(등록일 최신순, 같으면 글번호 큰 순)에서 바로 위에 보이는 글 = 바로 다음에 올라온 글
   * 1:1 문의(FAQ 제외)·미삭제만, PageRequest.of(0, 1)로 1건
   */
  @Query("""
      SELECT q FROM Qa q
      WHERE q.isdel = 'N' AND q.isfaq = 'N'
        AND (q.cdate > :cdate OR (q.cdate = :cdate AND q.no > :no))
      ORDER BY q.cdate ASC, q.no ASC
      """)
  List<Qa> findAboveInList(@Param("cdate") String cdate, @Param("no") Long no, Pageable pageable);

  /**
   * 다음글 — 목록에서 바로 아래에 보이는 글 = 바로 전에 올라온 글
   */
  @Query("""
      SELECT q FROM Qa q
      WHERE q.isdel = 'N' AND q.isfaq = 'N'
        AND (q.cdate < :cdate OR (q.cdate = :cdate AND q.no < :no))
      ORDER BY q.cdate DESC, q.no DESC
      """)
  List<Qa> findBelowInList(@Param("cdate") String cdate, @Param("no") Long no, Pageable pageable);

  /**
   * 다음글 (현재 글보다 날짜가 큰 것 중 가장 가까운 글 = 더 최근 글)
   */
  Optional<Qa> findFirstByNoGreaterThanAndIsdelAndIsfaqOrderByNoAsc(
      Long no, String isdel, String isfaq);

  // ==========================================
  // [작성자] 내 문의 내역 조회
  // ==========================================
  /**
   * 내 문의내역 전체조회 + 검색조회
   */
  @Query("""
      SELECT q, m.id 
      FROM Qa q
      LEFT JOIN Member m ON q.mno = m.no
      WHERE q.mno = :mno 
        AND q.isdel = 'N' 
        AND q.isfaq = 'N' 
        AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%) 
        AND (:type IS NULL OR q.type = :type) 
        AND (:status IS NULL OR q.status = :status) 
      ORDER BY q.cdate DESC 
      """)
  Page<Object[]> searchMyQuestions(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("status") Integer status,
      @Param("mno") Long mno,
      Pageable pageable);

  // ==========================================
  // 전체 내역 조회
  // ==========================================
  /**
   * 회원 문의내역 전체조회 + 검색조회 (관리자용)
   */
  @Query("""
      SELECT q, m.id 
      FROM Qa q 
      LEFT JOIN Member m ON q.mno = m.no 
      WHERE q.isdel = 'N' 
        AND q.isfaq = 'N' 
        AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%) 
        AND (:type IS NULL OR q.type = :type) 
        AND (:status IS NULL OR q.status = :status) 
        AND (:mno IS NULL OR q.mno = :mno) 
      ORDER BY q.cdate DESC
      """)
  Page<Object[]> searchAllQuestions(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("status") Integer status,
      @Param("mno") Long mno,
      Pageable pageable);

  /**
   * FAQ 게시글 전체조회 + 검색조회
   */
  @Query("""
      SELECT q FROM Qa q  
      WHERE q.isdel = 'N' 
        AND q.isfaq = 'Y' 
        AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%) 
        AND (:type IS NULL OR q.type = :type) 
        AND (:status IS NULL OR q.status = :status) 
      ORDER BY q.vseq ASC, q.cdate DESC
      """)
  Page<Qa> searchFaqsAll(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("status") Integer status,
      Pageable pageable);

  /**
   * 게시글 삭제용 (글 번호, 비밀번호, 삭제 여부 일치 조회)
   */
  Optional<Qa> findByNoAndIsdel(Long no, String isdel);

  /**
   * 비회원 검색조회
   * 검색어 : 작성자 이메일 , 제목, 내용
   */
  @Query("""
      SELECT q FROM Qa q  
      WHERE q.isdel = 'N' 
        AND q.isfaq = 'N'  
        AND (:word IS NULL OR :word = '' 
                OR q.title LIKE CONCAT('%', :word, '%') 
                OR q.content LIKE CONCAT('%', :word, '%') 
                OR LOWER(q.guestEmail) LIKE LOWER(CONCAT('%', :word, '%'))) 
      ORDER BY q.cdate DESC
      """)
  Page<Qa> searchGuestList(
      @Param("word") String word,
      Pageable pageable);

  /**
   * [관리자] 삭제된(소프트 삭제) 1:1 문의 목록 + 검색 — 삭제일 최신순
   */
  @Query("""
      SELECT q, m.id
      FROM Qa q
      LEFT JOIN Member m ON q.mno = m.no
      WHERE q.isdel = 'Y'
        AND q.isfaq = 'N'
        AND (:word IS NULL OR :word = '' OR q.title LIKE %:word% OR q.content LIKE %:word%)
        AND (:type IS NULL OR q.type = :type)
        AND (:mno IS NULL OR q.mno = :mno)
      ORDER BY q.ddate DESC, q.no DESC
      """)
  Page<Object[]> searchDeletedQuestions(
      @Param("word") String word,
      @Param("type") Integer type,
      @Param("mno") Long mno,
      Pageable pageable);

  /** 단건 상세 조회 — 작성자 아이디 포함 */
  @Query("""
      SELECT q, m.id 
      FROM Qa q 
      LEFT JOIN Member m ON m.no = q.mno
      WHERE q.no = :no
      """)
  Optional<Object[]> findByIdWithMemberId(@Param("no") Long no);
}