package dev.jpa.allimio.attach;

import java.util.List;

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
//==========================================
 // [등록한 게시판 내부에서 조회/삭제]
 // ==========================================

 /**
  * 테이블 번호(tno) 및 게시글 번호(bno)에 해당하는 첨부파일 목록 조회 (권장)
  * 
  * @param tno 테이블/메뉴 PK 번호
  * @param bno 게시글 PK 번호
  * @return 첨부파일 목록
  */
 List<Attach> findByTnoAndBno(Long tno, Long bno);

 /**
  * 테이블 이름(tname) 및 게시글 번호(bno)에 해당하는 첨부파일 목록 조회
  * 
  * @param tname 테이블 이름/폴더명
  * @param bno   게시글 PK 번호
  * @return 첨부파일 목록
  */
 List<Attach> findByTnameAndBno(String tname, Long bno);

 /**
  * 테이블 번호(tno) 및 게시글 번호(bno)에 해당하는 첨부파일 일괄 삭제 (권장)
  * 
  * @param tno 테이블/메뉴 PK 번호
  * @param bno 게시글 PK 번호
  */
 void deleteByTnoAndBno(Long tno, Long bno);

 /**
  * 테이블 이름(tname) 및 게시글 번호(bno)에 해당하는 첨부파일 일괄 삭제
  * 
  * @param tname 테이블 이름/폴더명
  * @param bno   게시글 PK 번호
  */
 void deleteByTnameAndBno(String tname, Long bno);


 // ==========================================
 // [첨부파일 관리자/통합 검색 및 페이징 조회]
 // ==========================================

 /**
  * 첨부파일 조건별 검색 및 페이징 목록 조회
  * 
  * @param word     파일명 키워드 검색어
  * @param tno      테이블/메뉴 PK 번호
  * @param tname    테이블 이름/폴더명
  * @param name     원본 파일명
  * @param type     파일 구분 (0: IMAGE, 1: FILE)
  * @param cdate    등록일자 (YYYY-MM-DD 등)
  * @param pageable 페이징 정보
  * @return 페이징 처리된 첨부파일 Entity 목록
  */
 @Query("SELECT a FROM Attach a WHERE " +
     "(:word IS NULL OR :word = '' OR a.name LIKE %:word%) " +
     "AND (:tno IS NULL OR a.tno = :tno) " +
     "AND (:tname IS NULL OR :tname = '' OR a.tname = :tname) " +
     "AND (:name IS NULL OR :name = '' OR a.name = :name) " +
     "AND (:type IS NULL OR a.type = :type) " +
     "AND (:cdate IS NULL OR :cdate = '' OR a.cdate LIKE %:cdate%) " +
     "ORDER BY a.cdate DESC, a.no DESC")
 Page<Attach> searchAllAttach(
     @Param("word") String word,
     @Param("tno") Long tno,
     @Param("tname") String tname,
     @Param("name") String name,
     @Param("type") Integer type,
     @Param("cdate") String cdate,
     Pageable pageable);

}