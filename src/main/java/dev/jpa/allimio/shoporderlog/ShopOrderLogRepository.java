package dev.jpa.allimio.shoporderlog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 구독 변경 이력 Repository
 *
 * SHOP_ORDER_LOG 테이블 조회 전용.
 */
@Repository
public interface ShopOrderLogRepository extends JpaRepository<ShopOrderLog, Long> {

  /** 특정 주문의 전체 이력을 최신순으로 조회 (구독 상세의 이력 탭 등에서 사용) */
  List<ShopOrderLog> findByOnoOrderByCdateDesc(String ono);

  /**
   * 회원 기준 변경 이력 검색 + 페이징 조회.
   * 매장별(sno), 이벤트 종류별(action), 기간별(cdate) 검색을 지원합니다.
   */
  @Query("""
      SELECT log, 
        (SELECT sp1.pname FROM ShopPlan sp1
          WHERE sop.ccnt BETWEEN sp1.mincctv AND sp1.maxcctv AND ROWNUM = 1) AS newPname,
        sop.ccnt AS newCcnt,
        (SELECT sp2.pname FROM ShopPlan sp2
          WHERE log.ccnt BETWEEN sp2.mincctv AND sp2.maxcctv AND ROWNUM = 1) AS pname,
        so.sdate 
      FROM ShopOrderLog log
      LEFT JOIN ShopOrderPending sop ON log.pno = sop.no 
      LEFT JOIN ShopOrder so ON log.ono = so.no 
      WHERE log.mno = :mno 
        AND log.ono = :ono 
        AND (:sno IS NULL OR log.sno = :sno)
        AND (:action IS NULL OR log.action = :action)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(log.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(log.cdate, 1, 10) <= :dateTo)
      ORDER BY log.cdate DESC
      """)
  Page<Object[]> searchByMno(
      @Param("mno") Long mno,
      @Param("ono") String ono,
      @Param("sno") Long sno,
      @Param("action") Integer action,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);

  /**
   * [관리자] 전체 회원 구독 변경 이력 조회
   * 검색어 : 매장명, 회원번호, 회원아이디, 메모
   * 필터 : 이벤트 종류, 발생일시
   */
  @Query("""
      SELECT log, m.id, s.title AS sname, 
        (SELECT sp1.pname FROM ShopPlan sp1
          WHERE sop.ccnt BETWEEN sp1.mincctv AND sp1.maxcctv AND ROWNUM = 1) AS newPname,
        sop.ccnt AS newCcnt,
        (SELECT sp2.pname FROM ShopPlan sp2
          WHERE log.ccnt BETWEEN sp2.mincctv AND sp2.maxcctv AND ROWNUM = 1) AS pname,
        so.sdate 
      FROM ShopOrderLog log 
      LEFT JOIN Member m ON log.mno = m.no 
      LEFT JOIN Shop s ON log.sno = s.no 
      LEFT JOIN ShopOrderPending sop ON log.pno = sop.no 
      LEFT JOIN ShopOrder so ON log.ono = so.no 
      WHERE (:word IS NULL OR :word = '' 
          OR s.title LIKE CONCAT('%', :word, '%') 
          OR m.id LIKE CONCAT('%', :word, '%') 
          OR CONCAT('', m.no) LIKE CONCAT('%', :word, '%')
          OR log.memo LIKE CONCAT('%', :word, '%')) 
        AND (:action IS NULL OR log.action = :action)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(log.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(log.cdate, 1, 10) <= :dateTo)
      ORDER BY log.cdate DESC
      """)
  Page<Object[]> searchAllAdmin(
      @Param("word") String word,
      @Param("action") Integer action,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
} 