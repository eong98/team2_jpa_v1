package dev.jpa.allimio.cctv;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CctvRepository extends JpaRepository<Cctv, Long> {

  /**
   * 매장 CCTV 목록 검색용 (/user/shop/cctv). 로그인 후 입장한 매장(sno) 소유 CCTV만 대상으로 함.
   * CCTV 등록/수정/삭제는 관리자(dbms) 전용이라 사용자 쪽은 조회만 지원합니다.
   * keyword는 선택 사항(null이면 조건 무시).
   * @param sno 매장 번호 (필수, GlobalCurrentShop().no 기준)
   * @param keyword CCTV명(cname)/MAC주소(mac) 포함 검색
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT c FROM Cctv c
      WHERE c.sno = :sno
        AND (:keyword IS NULL
             OR c.cname LIKE CONCAT('%', :keyword, '%')
             OR c.mac LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<Cctv> search(
      @Param("sno") long sno,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  /**
   * 관리자 CCTV 목록 검색용 (/dbms/cctv). sno 상관없이 전체 CCTV 대상.
   * sno를 넘기면 해당 매장 소유 CCTV만, 안 넘기면(null) 전체 조회.
   * @param sno 매장 번호 (선택 사항)
   * @param state 상태 코드 (선택 사항)
   * @param keyword CCTV명(cname)/MAC주소(mac) 포함 검색
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT c FROM Cctv c
      WHERE (:sno IS NULL OR c.sno = :sno)
        AND (:state IS NULL OR c.state = :state)
        AND (:keyword IS NULL
             OR c.cname LIKE CONCAT('%', :keyword, '%')
             OR c.mac LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<Cctv> searchAdmin(
      @Param("sno") Long sno,
      @Param("state") Integer state,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  /**
   * 매장(sno)에 등록된 CCTV 대수. /user/shop 매장 카드에 "등록 CCTV 몇 대"를 보여주기 위한 용도.
   * @param sno 매장 번호
   */
  long countBySno(long sno);

}
