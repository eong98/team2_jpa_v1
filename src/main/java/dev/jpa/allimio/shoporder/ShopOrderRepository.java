package dev.jpa.allimio.shoporder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
      LEFT JOIN ShopOrderPending pending ON so.no = pending.ono AND pending.status = 0 
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
   * [관리자] 전체 회원 구독권 조회
   * 검색어 : 플랜명, 매장명, 회원아이디
   * 필터 : 상태, 구독개월, 구매일기준 기간
   */
  @Query("""
      SELECT so, sp.pname, s.title AS sname, m.id  
      FROM ShopOrder so 
      LEFT JOIN ShopPlan sp ON so.pno = sp.no 
      LEFT JOIN Shop s ON so.sno = s.no 
      LEFT JOIN Member m ON so.mno = m.no 
      WHERE (:word IS NULL OR :word = '' 
          OR s.title LIKE CONCAT('%', :word, '%') OR sp.pname LIKE CONCAT('%', :word, '%')
          OR m.id LIKE CONCAT('%', :word, '%')) 
        AND (:status IS NULL 
          OR (:status = 1 AND so.status = 1 AND so.edate >= :today)  
          OR (:status = 3 AND so.status = 1 AND so.edate < :today)  
          OR (:status IN (0, 2) AND so.status = :status) 
          )
        AND (:pmonth IS NULL OR sp.pmonth = :pmonth)
        AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
        AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo) 
     ORDER BY so.cdate DESC 
      """)
  Page<Object[]> searchAllAdmin(
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pmonth") Integer pmonth,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      @Param("today") String today,
      Pageable pageable);

  
  
  
  /**
   * 회원 등급 변경 로직
   * -- 1. MEMBER 테이블(별칭 m)을 대상으로 MERGE 수행
   * -- 2. 단일 조건 체크를 위해 가상 테이블 DUAL 사용
   * -- 3. 매핑 조건: 파라미터로 받은 회원 번호(:no)가 PK(or 회원번호)와 일치하는지 검사
   * -- 4. 조건에 일치하는 회원 레코드가 존재할 경우 (UPDATE 수행)
   * -- 5. 해당 회원의 등급(grade)을 전달받은 파라미터값(:grade)으로 수정
   * 
   * @param mno
   * @param grade
   * @return
   */
  @Transactional // 데이터 변경 작업을 처리하므로 트랜잭션 범위 내에서 실행되어야 함
  // - clearAutomatically = true: 쿼리 실행 후 영속성 컨텍스트(1차 캐시)를 강제로 초기화(clear)하여 DB 데이터와의 불일치(Stale Data) 방지
  // - flushAutomatically = true: 쿼리 실행 전 영속성 컨텍스트의 지연 쓰기 데이터(Pending Changes)를 DB에 미리 반영(flush)
  @Modifying(clearAutomatically = true, flushAutomatically = true) // CUD(INSERT/UPDATE/DELETE) 쿼리 선언
  @Query(value = """
    MERGE INTO MEMBER m 
      USING DUAL 
      ON (m.no = :mno) 
      WHEN MATCHED THEN 
        UPDATE SET m.grade = :grade 
    """, nativeQuery = true) // JPA JPQL이 아닌 DB 고유의 SQL 문법을 직접 실행하는 Native Query로 지정
  int mergeMemberGrade(@Param("mno") Long mno, @Param("grade") Integer grade);
  
  /** 회원 현재 등급 조회 (MEMBER 도메인 파일 안 거치고 직접 조회) */
  @Query(value = "SELECT grade FROM MEMBER WHERE no = :mno", nativeQuery = true)
  Integer findMemberGrade(@Param("mno") Long mno);
  
  /**
   * 이 회원이 정상, 대기중인 (status=0, 1, 만료 안 됨) 구독권을 하나라도
   * 가지고 있는지 확인합니다. 등급 강등 조건 판단에 사용합니다.
   */
  @Query(value = """
      SELECT COUNT(*) FROM SHOP_ORDER
      WHERE mno = :mno AND (status = 0 OR (status = 1 AND edate >= :today))
      """, nativeQuery = true)
  int countActiveByMno(@Param("mno") Long mno, @Param("today") String today);
  

  /**
   * 구독권 만료시 등급변경을 위한 조회
   * @param status
   * @param edate
   * @return
   */
  List<ShopOrder> findAllByStatusAndEdateBefore(Integer status, String edate);
  
}