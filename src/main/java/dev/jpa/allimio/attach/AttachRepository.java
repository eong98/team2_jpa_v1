package dev.jpa.allimio.attach;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 첨부파일 JPA 리포지토리 인터페이스
 */
@Repository
public interface AttachRepository extends JpaRepository<Attach, Long> {

    // ==========================================
    // [메뉴 번호(tno) 역조회 쿼리]
    // ==========================================

    /**
     * tname(테이블명/게시판명)으로 IN_MENU 및 SHOP_MENU에서 메뉴 번호(tno) 조회
     */
    @Query(value = "SELECT NO FROM (" +
                   "  SELECT NO FROM IN_MENU WHERE TNAME = :tname " +
                   "  UNION ALL " +
                   "  SELECT NO FROM SHOP_MENU WHERE TNAME = :tname" +
                   ") WHERE ROWNUM = 1", nativeQuery = true)
    Optional<Long> findTnoByTname(@Param("tname") String tname);

    /**
     * bno로 등록된 attach 레코드 중 하나에서 tno만 조회
     */
    @Query("SELECT DISTINCT a.tno FROM Attach a WHERE a.bno = :bno")
    Optional<Long> findTnoByBno(@Param("bno") Long bno);

    // ==========================================
    // [등록한 게시판 내부에서 조회/삭제]
    // ==========================================

    /**
     * 테이블 번호(tno) 및 게시글 번호(bno)에 해당하는 첨부파일 목록 조회 (권장)
     */
    List<Attach> findByTnoAndBno(Long tno, Long bno);

    /**
     * 테이블 번호(tno) 및 게시글 번호(bno)에 해당하는 첨부파일 일괄 삭제 (권장)
     */
    void deleteByTnoAndBno(Long tno, Long bno);

    // ==========================================
    // [첨부파일 관리자/통합 검색 및 페이징 조회]
    // ==========================================

    /**
     * 첨부파일 조건별 검색 및 페이징 목록 조회
     * 
     * @param word     파일명 키워드 검색어
     * @param tno      테이블/메뉴 PK 번호
     * @param type     파일 구분 (0: IMAGE, 1: FILE)
     * @param cdate    등록일자 (YYYY-MM-DD 등)
     * @param pageable 페이징 정보
     * @return 페이징 처리된 첨부파일 Entity 목록
     */
    @Query("SELECT a FROM Attach a WHERE " +
           "(:word IS NULL OR :word = '' OR a.name LIKE CONCAT('%', :word, '%')) " +
           "AND (:tno IS NULL OR a.tno = :tno) " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:cdate IS NULL OR :cdate = '' OR a.cdate LIKE CONCAT('%', :cdate, '%')) " +
           "ORDER BY a.cdate DESC, a.no DESC")
    Page<Attach> searchAllAttach(
            @Param("word") String word,
            @Param("tno") Long tno,
            @Param("type") Integer type,
            @Param("cdate") String cdate,
            Pageable pageable);
}