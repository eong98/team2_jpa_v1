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

/**
 * 구독 내역 Repository
 *
 * SHOP_ORDER 테이블의 데이터를 조회, 등록, 수정할 때 사용합니다.
 * (삭제 API는 없음 — 취소는 STATUS=2로 소프트 처리, 실제 행 삭제는 지원하지 않음)
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
   * 특정 회원(mno) 소유 매장 중, 활성(STATUS=0) 구독이 걸려있지 않은 매장만 조회.
   * (매장이 아예 구독이 없거나, 있었더라도 만료/취소된 경우 둘 다 포함)
   */
  @Query("""
      SELECT s FROM Shop s
      WHERE s.mno = :mno
        AND s.no NOT IN (
          SELECT so.sno FROM ShopOrder so WHERE so.status = 0 AND so.sno IS NOT NULL
        )
      """)
  List<Shop> findLinkableShops(@Param("mno") long mno);

  /**
   * 회원 기준 구독 내역 검색 + 페이징 조회.
   * 날짜 검색은 dateType으로 어느 컬럼(구독시작일/구독종료일/구매일)을 볼지 정하고,
   * dateFrom~dateTo 하나의 기간으로 그 컬럼만 비교합니다 — 세 개 날짜 필터를
   * 따로 두지 않고 "기준 선택 + 기간 하나"로 통합한 방식입니다.
   */
  @Query("""
      SELECT so FROM ShopOrder so
      WHERE so.mno = :mno
        AND (:word IS NULL OR :word = '' OR so.orderno LIKE CONCAT('%', :word, '%'))
        AND (:status IS NULL OR so.status = :status)
        AND (:pno IS NULL OR so.pno = :pno)
        AND (:sno IS NULL OR so.sno = :sno)
        AND (
          :dateType IS NULL OR :dateType = ''
          OR (:dateType = 'sdate'
              AND (:dateFrom IS NULL OR :dateFrom = '' OR so.sdate >= :dateFrom)
              AND (:dateTo IS NULL OR :dateTo = '' OR so.sdate <= :dateTo))
          OR (:dateType = 'edate'
              AND (:dateFrom IS NULL OR :dateFrom = '' OR so.edate >= :dateFrom)
              AND (:dateTo IS NULL OR :dateTo = '' OR so.edate <= :dateTo))
          OR (:dateType = 'cdate'
              AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
              AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo))
        )
      """)
  Page<ShopOrder> searchByMno(
      @Param("mno") Long mno,
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pno") Long pno,
      @Param("sno") Long sno,
      @Param("dateType") String dateType,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);

  /**
   * 관리자용 구독 내역 검색 + 페이징 조회. mno 포함 모든 조건이 선택사항이라
   * mno를 안 넘기면 전체 회원 대상으로 조회됩니다. 날짜 검색 방식은 searchByMno와 동일합니다.
   */
  @Query("""
      SELECT so FROM ShopOrder so
      WHERE (:mno IS NULL OR so.mno = :mno)
        AND (:word IS NULL OR :word = '' OR so.orderno LIKE CONCAT('%', :word, '%'))
        AND (:status IS NULL OR so.status = :status)
        AND (:pno IS NULL OR so.pno = :pno)
        AND (:sno IS NULL OR so.sno = :sno)
        AND (
          :dateType IS NULL OR :dateType = ''
          OR (:dateType = 'sdate'
              AND (:dateFrom IS NULL OR :dateFrom = '' OR so.sdate >= :dateFrom)
              AND (:dateTo IS NULL OR :dateTo = '' OR so.sdate <= :dateTo))
          OR (:dateType = 'edate'
              AND (:dateFrom IS NULL OR :dateFrom = '' OR so.edate >= :dateFrom)
              AND (:dateTo IS NULL OR :dateTo = '' OR so.edate <= :dateTo))
          OR (:dateType = 'cdate'
              AND (:dateFrom IS NULL OR :dateFrom = '' OR SUBSTRING(so.cdate, 1, 10) >= :dateFrom)
              AND (:dateTo IS NULL OR :dateTo = '' OR SUBSTRING(so.cdate, 1, 10) <= :dateTo))
        )
      """)
  Page<ShopOrder> searchAllAdmin(
      @Param("mno") Long mno,
      @Param("word") String word,
      @Param("status") Integer status,
      @Param("pno") Long pno,
      @Param("sno") Long sno,
      @Param("dateType") String dateType,
      @Param("dateFrom") String dateFrom,
      @Param("dateTo") String dateTo,
      Pageable pageable);
}