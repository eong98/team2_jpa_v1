package dev.jpa.allimio.shoprefund;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 환불계좌 Repository
 */
@Repository
public interface ShopRefundRepository extends JpaRepository<ShopRefund, Long> {

  /** 특정 주문의 환불계좌 (한 주문에 여러 번 취소가 있을 순 없지만, 안전하게 리스트로) */
  List<ShopRefund> findByPnoOrderByCdateDesc(Long pno);
  
  /** 특정 주문의 환불계좌를 최신순으로 조회 (승인 시점에 신청 시 만든 대기 건을 찾을 때 사용) */
  List<ShopRefund> findByOnoOrderByCdateDesc(String ono);

  /** 회원 기준 환불계좌 검색 + 페이징 조회 */
//  @Query("""
//      SELECT r FROM ShopRefund r
//      WHERE r.mno = :mno AND r.ono = :ono 
//        AND (:status IS NULL OR r.status = :status)
//        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(r.cdate, 1, 10) >= :dateFrom)
//        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(r.cdate, 1, 10) <= :dateTo)
//      ORDER BY r.cdate DESC
//      """)
//  Page<ShopRefund> searchByMno(
//      @Param("mno") Long mno,
//      @Param("status") Integer status,
//      @Param("dateFrom") String dateFrom,
//      @Param("dateTo") String dateTo,
//      Pageable pageable);

  /** 관리자용 전체(또는 특정 회원) 환불계좌 검색 + 페이징 조회 */
  @Query("""
      SELECT r FROM ShopRefund r
      WHERE (:mno IS NULL OR r.mno = :mno)
        AND (:status IS NULL OR r.status = :status)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(r.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(r.cdate, 1, 10) <= :dateTo)
      ORDER BY r.cdate DESC
      """)
  Page<ShopRefund> searchAllAdmin(
      @Param("mno") Long mno,
      @Param("status") Integer status,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
}