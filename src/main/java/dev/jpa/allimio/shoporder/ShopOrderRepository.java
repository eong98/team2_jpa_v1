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

  /** 회원번호로 구독 내역을 최신순으로 조회 */
  List<ShopOrder> findByMnoOrderByCdateDesc(Long mno);
  
  /**
   * 만료된 구독권에 갱신버튼 노출 조건 설정을 위한 목록조회
   * @param sno : 매장별
   * @param status : 상태가 1 (정상)
   * @param edate : 오늘이 edate 보다 이전 (만료전)
   * @return 목록 반환
   */
  List<ShopOrder> findAllBySnoAndStatus(Long sno, Integer status);

  
  /**
   * 매장 선택 확정 검증
   * 특정 매장 + 구독권 상태로 조회
   * @param sno
   * @param status
   * @return
   */
  Optional<ShopOrder> findBySnoAndStatus(Long sno, Integer status);

  /**
   * 
   * 로그인된 점주의 연결 가능한 매장 목록 조회
   * 
   * 1. 연결된 구독권이 없는 매장
   * 2. 연결된 구독권이 있지만 취소된 매장
   * 3. 연결된 구독권의 종료일자가 지난 매장
   * 
   * @param mno : 로그인된 회원
   * @param today : 만료일 확인용
   * @return 매장목록 반환 : ShopMatch.tsx
   */
  @Query("""
      SELECT s FROM Shop s
      WHERE s.mno = :mno
        AND s.no NOT IN (
          SELECT so.sno FROM ShopOrder so
          WHERE so.sno IS NOT NULL AND so.status = 1 AND so.edate >= :today)
      """)
  List<Shop> findLinkableShops(@Param("mno") long mno, @Param("today") String today);

  
  /**
   * 로그인된 점주의 매장1에 연결 가능한 구독권 목록 조회
   * 
   * 1. 연결되지 않은 구독권(대기상태)
   * 2. 연결된(정상,만료), 취소된 구독권은 대상아님
   * 
   * @return 구독권 목록 반환 : ShopOrderMatch.tsx
   */
  @Query("""
      SELECT so, sp.pname
      FROM ShopOrder so
      LEFT JOIN ShopPlan sp ON so.pno = sp.no
      WHERE so.mno = :mno AND so.sno IS NULL AND so.status = 0 ORDER BY so.cdate DESC
      """)
  List<Object[]> findLinkableOrders(@Param("mno") Long mno);
  
  /**
   * 검색어 : 매장이름, 구독권 이름 
   * 필터 검색 : 구독상태, 구독기간(6/12), 구매일
   * 
   * @param mno
   * @param word
   * @param status
   * @param pmonth
   * @param dateFrom
   * @param dateTo
   * @param pageable
   * @return 내 구독내역 전체 : /user/shoporder/ShopOrderList.tsx
   */
  @Query("""
      SELECT so, sp.pname, s.title AS sname, pending.status AS pstatus 
      FROM ShopOrder so
      LEFT JOIN ShopOrderPending pending ON so.no = pending.ono 
      LEFT JOIN ShopPlan sp ON so.pno = sp.no
      LEFT JOIN Shop s ON so.sno = s.no
      WHERE so.mno = :mno
        AND (:word IS NULL OR :word = '' OR s.title LIKE CONCAT('%', :word, '%') OR sp.pname LIKE CONCAT('%', :word, '%'))
        AND (:status IS NULL 
          OR (:status = 1 AND so.status = 1 AND so.edate >= :today)  
          OR (:status = 3 AND so.status = 1 AND so.edate < :today)  
          OR (:status IN (0, 2) AND so.status = :status) 
          )
        AND (:pmonth IS NULL OR sp.pmonth = :pmonth)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo) 
       ORDER BY so.status, so.edate DESC, so.cdate DESC
      """)
  Page<Object[]> searchAllWithJoin(
      @Param("mno") Long mno,
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pmonth") Integer pmonth,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      @Param("today") String today,
      Pageable pageable);
  
  
  /**
   * 
   * 검색어 : 구독권 이름 
   * 필터 검색 : 구독상태, 구독기간(6/12), 구매일
   * 
   * @param mno
   * @param sno
   * @param word
   * @param status
   * @param pmonth
   * @param dateFrom
   * @param dateTo
   * @param pageable
   * 
   * @return 사용자(점주) + 매장1 의 구독내역 전체 : /user/shop/ShopOrderList.tsx
   */
  @Query("""
      SELECT so, sp.pname, pending.status AS pstatus 
      FROM ShopOrder so 
      LEFT JOIN ShopOrderPending pending ON so.no = pending.ono 
      LEFT JOIN ShopPlan sp ON so.pno = sp.no 
      WHERE so.mno = :mno AND so.sno = :sno AND NOT (so.status = 1 AND so.edate >= :today) 
        AND (:word IS NULL OR :word = '' OR sp.pname LIKE CONCAT('%', :word, '%'))
        AND (:status IS NULL  
          OR (:status = 3 AND so.status = 1 AND so.edate < :today) 
          OR (:status IN (0, 2) AND so.status = :status) 
          )
        AND (:pmonth IS NULL OR sp.pmonth = :pmonth)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo) 
       ORDER BY so.status, so.edate DESC, so.cdate DESC
      """)
  Page<Object[]> searchSnoAndMno(
      @Param("mno") Long mno,
      @Param("sno") Long sno,
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pmonth") Integer pmonth,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      @Param("today") String today,
      Pageable pageable);
  
  
  /**
   * 매장별 구독내역 상단 노출 
   * @param sno
   * @param status
   * @return
   */
  @Query("""
      SELECT so, sp.pname, pending.status AS pstatus 
      FROM ShopOrder so 
      LEFT JOIN ShopOrderPending pending ON so.no = pending.ono 
      LEFT JOIN ShopPlan sp ON so.pno = sp.no 
      WHERE so.mno = :mno AND so.sno = :sno AND so.status = :status
      """) 
  List<Object[]> findByShopTop(
      @Param("mno") Long mno, 
      @Param("sno") Long sno, 
      @Param("status") Integer status);
  

  /**
   * 주문내역 상세조회
   * @param no
   * @return
   */
  @Query("""
      SELECT so, sp.pname, s.title AS sname 
      FROM ShopOrder so 
      LEFT JOIN ShopPlan sp ON so.pno = sp.no 
      LEFT JOIN Shop s ON so.sno = s.no 
      WHERE so.no = :no
      """)
  List<Object[]> findByIdWithJoin(@Param("no") String no);
  
  
  /**
   * 변경 신청 시 대수변경 조건 호출
   * @param no
   * @return
   */
  @Query("""
      SELECT so, 
         (SELECT MIN(p.mincctv) FROM ShopPlan p WHERE p.pmonth = so.pmonth) AS minCcnt, 
         (SELECT MAX(p.maxcctv) FROM ShopPlan p WHERE p.pmonth = so.pmonth) AS maxCcnt 
      FROM ShopOrder so 
      LEFT JOIN ShopPlan sp ON so.pno = sp.no 
      LEFT JOIN Shop s ON so.sno = s.no 
      WHERE so.no = :no
      """)
  List<Object[]> findByNoWithChange(@Param("no") String no);
  
  

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
  
  
  
}