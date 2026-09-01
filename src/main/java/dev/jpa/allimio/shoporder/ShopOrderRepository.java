package dev.jpa.allimio.shoporder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jpa.allimio.shop.Shop;
import jakarta.persistence.Tuple;

/**
 * 구독 내역 Repository
 *
 * SHOP_ORDER 테이블의 데이터를 조회, 등록, 수정할 때 사용합니다.
 */
@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, String> {

  /** 회원번호로 구독 내역을 최신순으로 조회 (마이페이지 요약용, 페이징 없는 단순 목록) */
  List<ShopOrder> findByMnoOrderByCdateDesc(Long mno);

  /** 매장번호로 구독 내역을 최신순으로 조회 */
  List<ShopOrder> findBySnoOrderByCdateDesc(Long sno);

  /**
   * 매장에 연결된 특정 상태의 구독 내역을 조회합니다.
   * 매장 선택 확정 시 "이미 활성 구독이 걸려있는지" 검증용으로 씁니다.
   */
  Optional<ShopOrder> findBySnoAndStatus(Long sno, Integer status);

  /**
   * "구독 결제 완료 후 연결 가능한 매장" 목록용.
   * 특정 회원(mno) 소유 매장 중, 정상(STATUS=2) 구독이 걸려있지 않은 매장만 조회.
   */
  @Query("""
      SELECT s FROM Shop s
      WHERE s.mno = :mno
        AND s.no NOT IN (
          SELECT so.sno FROM ShopOrder so WHERE so.status = 2 AND so.sno IS NOT NULL
        )
      """)
  List<Shop> findLinkableShops(@Param("mno") long mno);

  
  /**
   * "매장에 연결 가능한 구독권" 목록. 매장(SNO) 미연결이면서 매장연결대기(0)
   * 또는 정상(2) 상태인 것만 조회. (승인대기(1)/만료(3)/취소(4)는 제외)
   */
  @Query("""
      SELECT so, sp.pname
      FROM ShopOrder so
      LEFT JOIN ShopPlan sp ON so.pno = sp.no
      WHERE so.mno = :mno
        AND so.sno IS NULL
        AND so.status IN (0, 2)
      ORDER BY so.cdate DESC
      """)
  List<Object[]> findLinkableOrders(@Param("mno") Long mno);
  
  /**
   * 회원 기준 구독 내역 검색 + 페이징 조회.
   * dateFrom~dateTo 하나의 기간으로 그 컬럼만 비교합니다.
   * 검색어 : 매장이름/플랜이름 
   * 필터 검색 : 구독권 기간, 종류, 상태, 연결된매장 여부, 등록일
   */
  @Query("""
      SELECT so, sp.pname, s.title
      FROM ShopOrder so
      LEFT JOIN ShopPlan sp ON so.pno = sp.no
      LEFT JOIN Shop s ON so.sno = s.no
      WHERE so.mno = :mno
        AND (
          :word IS NULL OR :word = ''
          OR sp.pname LIKE CONCAT('%', :word, '%')
          OR s.title LIKE CONCAT('%', :word, '%')
        )
        AND (:status IS NULL OR so.status = :status)
        AND (:pname IS NULL OR sp.pname = :pname)
        AND (:pmonth IS NULL OR sp.pmonth = :pmonth)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo) 
       ORDER BY so.status, so.cdate DESC
      """)
  Page<Object[]> searchAllWithJoin(
      @Param("mno") Long mno,
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pname") String pname,
      @Param("pmonth") Integer pmonth,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
  
  
  /**
   * 회원 + 매장 기준 구독 내역 검색 + 페이징 조회.
   * dateFrom~dateTo 하나의 기간으로 그 컬럼만 비교합니다.
   * 검색어 : 매장이름/플랜이름 
   * 필터 검색 : 구독권 기간, 종류, 상태, 연결된매장 여부, 등록일
   */
  @Query("""
      SELECT so, sp.pname, s.title
      FROM ShopOrder so
      LEFT JOIN ShopPlan sp ON so.pno = sp.no
      LEFT JOIN Shop s ON so.sno = s.no
      WHERE so.mno = :mno AND so.sno = :sno 
        AND (
          :word IS NULL OR :word = ''
          OR sp.pname LIKE CONCAT('%', :word, '%')
          OR s.title LIKE CONCAT('%', :word, '%')
        )
        AND (:status IS NULL OR so.status = :status)
        AND (:pname IS NULL OR sp.pname = :pname)
        AND (:pmonth IS NULL OR sp.pmonth = :pmonth)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo) 
       ORDER BY so.status, so.cdate DESC
      """)
  Page<Object[]> searchSnoAndMno(
      @Param("mno") Long mno,
      @Param("sno") Long sno,
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pname") String pname,
      @Param("pmonth") Integer pmonth,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
  
  /** 단건 조회 (조인 데이터 Object[] 배열 반환) */
  @Query("""
      SELECT so AS order, sp.pname AS pname, s.title AS sname, 
        (SELECT MIN(p2.mincctv) FROM ShopPlan p2 WHERE p2.pmonth = so.pmonth) AS minCcnt,
        (SELECT MAX(p2.maxcctv) FROM ShopPlan p2 WHERE p2.pmonth = so.pmonth) AS maxCcnt 
      FROM ShopOrder so
      LEFT JOIN ShopPlan sp ON so.pno = sp.no
      LEFT JOIN Shop s ON so.sno = s.no
      WHERE so.no = :no
      """)
  Optional<Tuple> findWithJoinById(@Param("no") String no);

  /**
   * 관리자용 구독 내역 검색 + 페이징 조회. mno 포함 모든 조건이 선택사항이라
   * mno를 안 넘기면 전체 회원 대상으로 조회됩니다.
   */
//  @Query("""
//      SELECT so FROM ShopOrder so
//      WHERE (:mno IS NULL OR so.mno = :mno)
//        AND (:word IS NULL OR :word = '')
//        AND (:status IS NULL OR so.status = :status)
//        AND (:pno IS NULL OR so.pno = :pno)
//        AND (:sno IS NULL OR so.sno = :sno)
//        AND (
//          :dateType IS NULL OR :dateType = ''
//          OR (:dateType = 'sdate'
//              AND (:dateFrom IS NULL OR :dateFrom = '' OR so.sdate >= :dateFrom)
//              AND (:dateTo IS NULL OR :dateTo = '' OR so.sdate <= :dateTo))
//          OR (:dateType = 'edate'
//              AND (:dateFrom IS NULL OR :dateFrom = '' OR so.edate >= :dateFrom)
//              AND (:dateTo IS NULL OR :dateTo = '' OR so.edate <= :dateTo))
//          OR (:dateType = 'cdate'
//              AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
//              AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo))
//        )
//      """)
//  Page<ShopOrder> searchAllAdmin(
//      @Param("mno") Long mno,
//      @Param("word") String word,
//      @Param("status") Integer status,
//      @Param("pno") Long pno,
//      @Param("sno") Long sno,
//      @Param("dateType") String dateType,
//      @Param("dateFrom") String dateFrom,
//      @Param("dateTo") String dateTo,
//      Pageable pageable);
  
  
  
  
  /** 관리자용 — CCTV 대수 변경 승인 대기(STATUS=1) 목록 조회. PENDING_CCNT 체크 불필요. */
  List<ShopOrder> findByStatus(Integer status);
  
  
  
}