package dev.jpa.allimio.cctvissue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CctvIssueRepository extends JpaRepository<CctvIssue, Long> {

  /**
   * 관리자 목록 검색용. 파라미터는 전부 선택 사항(null이면 조건 무시, 전체 대상).
   * @param cno CCTV번호 (정확히 일치)
   * @param code 문제유형코드 (정확히 일치)
   * @param state 오탐여부 (정확히 일치)
   * @param noticeyn 발송여부 Y/N (정확히 일치)
   * @param keyword 상황설명(comnet) 포함 검색
   *   ※ comnet이 CLOB이라 LOWER() 등 문자열 함수를 걸지 않고 LIKE만 사용 (대소문자 구분됨)
   * @param cdateFrom 등록일 시작 (yyyy-MM-dd), cdate 앞 10글자와 비교
   * @param cdateTo   등록일 종료 (yyyy-MM-dd)
   * @param pageable  페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT ci FROM CctvIssue ci
      WHERE (:cno IS NULL OR ci.cno = :cno)
        AND (:code IS NULL OR ci.code = :code)
        AND (:state IS NULL OR ci.state = :state)
        AND (:noticeyn IS NULL OR ci.noticeyn = :noticeyn)
        AND (:keyword IS NULL OR ci.comnet LIKE CONCAT('%', :keyword, '%'))
        AND (:cdateFrom IS NULL OR SUBSTRING(ci.cdate, 1, 10) >= :cdateFrom)
        AND (:cdateTo IS NULL OR SUBSTRING(ci.cdate, 1, 10) <= :cdateTo)
      """)
  Page<CctvIssue> search(
      @Param("cno") Long cno,
      @Param("code") String code,
      @Param("state") Integer state,
      @Param("noticeyn") String noticeyn,
      @Param("keyword") String keyword,
      @Param("cdateFrom") String cdateFrom,
      @Param("cdateTo") String cdateTo,
      Pageable pageable
  );

}
