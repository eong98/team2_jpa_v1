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
  List<ShopOrderLog> findByOrdernoOrderByCdateDesc(String orderno);

  /**
   * 회원 기준 변경 이력 검색 + 페이징 조회.
   * 매장별(sno), 이벤트 종류별(action), 기간별(cdate) 검색을 지원합니다.
   */
  @Query("""
      SELECT l FROM ShopOrderLog l
      WHERE l.mno = :mno
        AND (:sno IS NULL OR l.sno = :sno)
        AND (:action IS NULL OR l.action = :action)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(l.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(l.cdate, 1, 10) <= :dateTo)
      ORDER BY l.cdate DESC
      """)
  Page<ShopOrderLog> searchByMno(
      @Param("mno") Long mno,
      @Param("sno") Long sno,
      @Param("action") Integer action,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);

  /**
   * 관리자용 전체(또는 특정 회원) 변경 이력 검색 + 페이징 조회.
   */
  @Query("""
      SELECT l FROM ShopOrderLog l
      WHERE (:mno IS NULL OR l.mno = :mno)
        AND (:sno IS NULL OR l.sno = :sno)
        AND (:action IS NULL OR l.action = :action)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(l.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(l.cdate, 1, 10) <= :dateTo)
      ORDER BY l.cdate DESC
      """)
  Page<ShopOrderLog> searchAllAdmin(
      @Param("mno") Long mno,
      @Param("sno") Long sno,
      @Param("action") Integer action,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
}