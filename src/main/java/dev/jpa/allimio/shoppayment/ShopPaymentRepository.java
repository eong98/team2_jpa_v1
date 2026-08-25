package dev.jpa.allimio.shoppayment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 결제 내역 Repository
 *
 * SHOP_PAYMENT 테이블 조회 전용 — 결제 등록은 ShopOrderService가
 * ShopPaymentService.pay()/refund()를 통해서만 수행합니다.
 */
@Repository
public interface ShopPaymentRepository extends JpaRepository<ShopPayment, Long> {

  /** 특정 주문의 결제 내역을 최신순으로 조회 */
  List<ShopPayment> findByOnoOrderByCdateDesc(String ono);

  /**
   * 회원 기준 결제 내역 검색 + 페이징 조회.
   * 매장별(sno)은 SHOP_ORDER를 거쳐야 하는 조건이라 여기선 안 받고,
   * 결제수단(pmethod)/결제상태(pstatus)/기간(cdate) 검색만 지원합니다.
   */
  @Query("""
      SELECT p FROM ShopPayment p
      WHERE p.mno = :mno
        AND (:pmethod IS NULL OR p.pmethod = :pmethod)
        AND (:pstatus IS NULL OR p.pstatus = :pstatus)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(p.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(p.cdate, 1, 10) <= :dateTo)
      ORDER BY p.cdate DESC
      """)
  Page<ShopPayment> searchByMno(
      @Param("mno") Long mno,
      @Param("pmethod") Integer pmethod,
      @Param("pstatus") Integer pstatus,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);

  /** 관리자용 전체(또는 특정 회원) 결제 내역 검색 + 페이징 조회 */
  @Query("""
      SELECT p FROM ShopPayment p
      WHERE (:mno IS NULL OR p.mno = :mno)
        AND (:pmethod IS NULL OR p.pmethod = :pmethod)
        AND (:pstatus IS NULL OR p.pstatus = :pstatus)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(p.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(p.cdate, 1, 10) <= :dateTo)
      ORDER BY p.cdate DESC
      """)
  Page<ShopPayment> searchAllAdmin(
      @Param("mno") Long mno,
      @Param("pmethod") Integer pmethod,
      @Param("pstatus") Integer pstatus,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
}