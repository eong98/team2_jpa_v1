package dev.jpa.allimio.cctvissuecode;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CctvIssueCodeRepository extends JpaRepository<CctvIssueCode, String> {

  /**
   * 프론트(관리자·사용자 화면 공통)에서 코드→라벨 매핑을 만들 때 쓰는 전체 목록.
   * 정렬 순서(ord)대로 내려주므로, CctvIssueList.tsx의 필터 드롭다운 등에 그대로 쓰면 됩니다.
   * 화면에서 실제 사용 중인(useYn='Y') 코드만 필요할 때가 대부분이라 findAllUse()를 기본으로 쓰고,
   * 관리자 코드관리 화면(CctvIssueCodeList.tsx)에서는 미사용 코드도 봐야 하므로 findAll(Sort)을 씁니다.
   */
  List<CctvIssueCode> findAllByUseYnOrderByOrdAsc(String useYn);

  /**
   * 관리자 코드관리 목록 검색용. keyword(code/codeName 포함 검색), useYn 조건은 전부 선택 사항.
   */
  @Query(value = """
      SELECT c FROM CctvIssueCode c
      WHERE (:useYn IS NULL OR c.useYn = :useYn)
        AND (:keyword IS NULL
             OR c.code LIKE CONCAT('%', :keyword, '%')
             OR c.codeName LIKE CONCAT('%', :keyword, '%'))
      """,
      countQuery = """
      SELECT COUNT(c) FROM CctvIssueCode c
      WHERE (:useYn IS NULL OR c.useYn = :useYn)
        AND (:keyword IS NULL
             OR c.code LIKE CONCAT('%', :keyword, '%')
             OR c.codeName LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<CctvIssueCode> search(
      @Param("useYn") String useYn,
      @Param("keyword") String keyword,
      Pageable pageable
  );

}
