package dev.jpa.allimio.cctvstream;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CctvStreamRepository extends JpaRepository<CctvStream, Long> {

  /** CCTV 1대당 스트림 1건(cno UNIQUE) - Jetson 워커가 자기 담당 CCTV의 접속정보를 찾을 때 사용 */
  Optional<CctvStream> findByCno(long cno);

  /**
   * 관리자 스트림 관리 목록 검색용. cno/connState/keyword(streamUrl 포함 검색)는 전부 선택 사항.
   */
  @Query(value = """
      SELECT s FROM CctvStream s
      WHERE (:cno IS NULL OR s.cno = :cno)
        AND (:connState IS NULL OR s.connState = :connState)
        AND (:keyword IS NULL OR s.streamUrl LIKE CONCAT('%', :keyword, '%'))
      """,
      countQuery = """
      SELECT COUNT(s) FROM CctvStream s
      WHERE (:cno IS NULL OR s.cno = :cno)
        AND (:connState IS NULL OR s.connState = :connState)
        AND (:keyword IS NULL OR s.streamUrl LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<CctvStream> search(
      @Param("cno") Long cno,
      @Param("connState") Integer connState,
      @Param("keyword") String keyword,
      Pageable pageable
  );

}
