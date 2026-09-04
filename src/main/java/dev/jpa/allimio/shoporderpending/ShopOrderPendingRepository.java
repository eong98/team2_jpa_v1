package dev.jpa.allimio.shoporderpending;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOrderPendingRepository extends JpaRepository<ShopOrderPending, Long> {
  /** 특정 주문에 특정 상태의 변경신청이 존재하는지 확인 (중복신청 방지용) */
  boolean existsByOnoAndStatus(String ono, Integer status);

  /** 
   * 관리자용 — 특정 상태의 변경신청 목록 조회
   * 필터 검색 : 검색어 - 매장명 / 필터 - 상태 , 신청일
  */
  @Query("""
      SELECT sop, sp.pname, s.title AS sname, 
         (SELECT MIN(p.mincctv) FROM ShopPlan p WHERE p.pmonth = sop.pmonth) AS minCcnt,
         (SELECT MAX(p.maxcctv) FROM ShopPlan p WHERE p.pmonth = sop.pmonth) AS maxCcnt 
      FROM ShopOrderPending sop 
      LEFT JOIN ShopOrder so On sop.ono = so.no 
      LEFT JOIN ShopPlan sp ON sop.pno = sp.no 
      LEFT JOIN Shop s ON so.sno = s.no 
      WHERE sop.status = :status 
          AND (:word IS NULL OR :word = ''
                  OR s.sname LIKE CONCAT('%', :word, '%')
          )
      AND (:status IS NULL OR sop.status = :staus) 
      AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(sop.cdate, 1, 10) >= :dateFrom)
      AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(sop.cdate, 1, 10) <= :dateTo)
      ORDER BY sop.status, sop.cdate DESC 
      """)
  Page<Object[]> findByStatusWithJoinSearch(
      @Param("status") Integer status,
      @Param("word") String word,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable
      );

}
