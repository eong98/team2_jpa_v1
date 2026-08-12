package dev.jpa.allimio.cctvvisitor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CctvVisitorRepository extends JpaRepository<CctvVisitor, Long> {

  /**
   * 관리자 손님(방문객) 내역 검색용 (/dbms/cctvvisitor). 매장(sno) 상관없이 전체 대상.
   * 파라미터는 전부 선택 사항(null이면 조건 무시).
   * CCTV_VISITOR엔 SNO 컬럼이 없어서, CNO가 속한 CCTV(테이블)의 SNO로 서브쿼리 필터링합니다
   * (CctvIssueRepository.searchByShop()과 동일 패턴).
   * @param sno 매장 번호 (선택 사항)
   * @param cno CCTV번호 (선택 사항, 정확히 일치)
   * @param state 상태값 (선택 사항, 정확히 일치)
   * @param keyword TRACK_ID(AI추적ID) 포함 검색
   * @param intimeFrom 입장일 시작 (yyyy-MM-dd), intime 앞 10글자와 비교
   * @param intimeTo   입장일 종료 (yyyy-MM-dd)
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT cv FROM CctvVisitor cv
      WHERE (:sno IS NULL OR cv.cno IN (SELECT c.no FROM Cctv c WHERE c.sno = :sno))
        AND (:cno IS NULL OR cv.cno = :cno)
        AND (:state IS NULL OR cv.state = :state)
        AND (:keyword IS NULL OR cv.trackId LIKE CONCAT('%', :keyword, '%'))
        AND (:intimeFrom IS NULL OR SUBSTRING(cv.intime, 1, 10) >= :intimeFrom)
        AND (:intimeTo IS NULL OR SUBSTRING(cv.intime, 1, 10) <= :intimeTo)
      """)
  Page<CctvVisitor> searchAdmin(
      @Param("sno") Long sno,
      @Param("cno") Long cno,
      @Param("state") Integer state,
      @Param("keyword") String keyword,
      @Param("intimeFrom") String intimeFrom,
      @Param("intimeTo") String intimeTo,
      Pageable pageable
  );

  /**
   * 매장(사용자) 손님 목록 검색용 (/user/cctvvisitor). sno(로그인 후 입장한 매장)는 필수,
   * 나머지 조건은 관리자 searchAdmin()과 동일하게 전부 선택 사항.
   * CCTV_VISITOR엔 SNO 컬럼이 없어서, CNO가 속한 CCTV(테이블)의 SNO로 서브쿼리 필터링합니다.
   * @param sno 매장 번호 (필수, GlobalCurrentShop().no 기준)
   * @param cno CCTV번호 (선택 사항, 정확히 일치)
   * @param state 상태값 (선택 사항, 정확히 일치)
   * @param keyword TRACK_ID(AI추적ID) 포함 검색
   * @param intimeFrom 입장일 시작 (yyyy-MM-dd)
   * @param intimeTo   입장일 종료 (yyyy-MM-dd)
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT cv FROM CctvVisitor cv
      WHERE cv.cno IN (SELECT c.no FROM Cctv c WHERE c.sno = :sno)
        AND (:cno IS NULL OR cv.cno = :cno)
        AND (:state IS NULL OR cv.state = :state)
        AND (:keyword IS NULL OR cv.trackId LIKE CONCAT('%', :keyword, '%'))
        AND (:intimeFrom IS NULL OR SUBSTRING(cv.intime, 1, 10) >= :intimeFrom)
        AND (:intimeTo IS NULL OR SUBSTRING(cv.intime, 1, 10) <= :intimeTo)
      """)
  Page<CctvVisitor> searchByShop(
      @Param("sno") long sno,
      @Param("cno") Long cno,
      @Param("state") Integer state,
      @Param("keyword") String keyword,
      @Param("intimeFrom") String intimeFrom,
      @Param("intimeTo") String intimeTo,
      Pageable pageable
  );

}