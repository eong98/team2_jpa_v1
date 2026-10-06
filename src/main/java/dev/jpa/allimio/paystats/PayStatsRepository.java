package dev.jpa.allimio.paystats;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/* ---------------------------------------------------------------------
   /dbms/paystats 구독권 결제 통계 전용 조회 Repository.

   - SHOP_PAYMENT(결제) / SHOP_ORDER(구독) / SHOP_PLAN(구독권) / SHOP_REFUND(환불) / MEMBER
     5개 테이블을 가로질러 집계하므로 EntityManager로 SQL을 직접 실행합니다.
     (기존 ShopPayment/ShopOrder Repository는 건드리지 않음)

   ⚠️ 날짜 컬럼(CDATE / UDATE)은 VARCHAR 'yyyy-MM-dd HH:mm:ss' 문자열입니다.
     DashboardRepository와 같은 방식으로 문자열 범위 비교
       col >= 'yyyy-MM-dd'(시작일)  AND  col < 'yyyy-MM-dd'(종료일 다음날)
     로 기간을 거르고, GROUP BY 할 때만 SUBSTR로 날짜 부분을 자릅니다.

   결제 상태 SHOP_PAYMENT.PSTATUS : 0 결제완료 / 1 결제실패 / 2 결제취소
   구독 상태 SHOP_ORDER.STATUS    : 0 매장연결 대기 / 1 정상 / 2 취소
   환불 상태 SHOP_REFUND.STATUS   : 0 대기 / 1 완료 / 2 반려
--------------------------------------------------------------------- */
@Repository
public class PayStatsRepository {

  @PersistenceContext
  private EntityManager em;

  private static final String PERIOD = " p.CDATE >= :from AND p.CDATE < :toNext ";

  /** 기간 내 결제완료 [금액 합계, 건수] */
  public Object[] paidSummary(String from, String toNext) {
    return (Object[]) em.createNativeQuery(
        "SELECT NVL(SUM(p.PRICE), 0), COUNT(*) FROM SHOP_PAYMENT p WHERE p.PSTATUS = 0 AND" + PERIOD)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getSingleResult();
  }

  /** 기간 내 결제 상태별 건수 [PSTATUS, 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> countByStatus(String from, String toNext) {
    return em.createNativeQuery(
        "SELECT p.PSTATUS, COUNT(*) FROM SHOP_PAYMENT p WHERE" + PERIOD + "GROUP BY p.PSTATUS")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 기간 내 환불 완료 [금액 합계, 건수] - 처리일(UDATE) 기준, 없으면 등록일 */
  public Object[] refundSummary(String from, String toNext) {
    return (Object[]) em.createNativeQuery("""
        SELECT NVL(SUM(r.AMOUNT), 0), COUNT(*) FROM SHOP_REFUND r
        WHERE r.STATUS = 1
          AND NVL(r.UDATE, r.CDATE) >= :from AND NVL(r.UDATE, r.CDATE) < :toNext
        """)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getSingleResult();
  }

  /** 처리 대기 중인 환불 건수 (현재) */
  public long countRefundPending() {
    return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM SHOP_REFUND WHERE STATUS = 0")
        .getSingleResult()).longValue();
  }

  /** 구독 상태별 건수 (현재) [STATUS, 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> orderByStatus() {
    return em.createNativeQuery("SELECT STATUS, COUNT(*) FROM SHOP_ORDER GROUP BY STATUS")
        .getResultList();
  }

  /** 일별 결제완료 [yyyy-MM-dd, 금액, 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> daily(String from, String toNext) {
    return em.createNativeQuery("""
        SELECT SUBSTR(p.CDATE, 1, 10), NVL(SUM(p.PRICE), 0), COUNT(*)
        FROM SHOP_PAYMENT p
        WHERE p.PSTATUS = 0 AND p.CDATE >= :from AND p.CDATE < :toNext
        GROUP BY SUBSTR(p.CDATE, 1, 10)
        """)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /**
   * 구독권별 누적 결제완료 건수 [구독권번호, 건수] (기간 무관, 전체)
   * 사용자 결제화면의 "인기" 구독권 표시에 사용 (ShopPlanService.findAllList)
   */
  @SuppressWarnings("unchecked")
  public List<Object[]> paidCountByPlanAll() {
    return em.createNativeQuery("""
        SELECT o.PNO, COUNT(*)
        FROM SHOP_PAYMENT p
        JOIN SHOP_ORDER o ON o.NO = p.ONO
        WHERE p.PSTATUS = 0
        GROUP BY o.PNO
        """)
        .getResultList();
  }

  /** 구독권별 결제완료 [구독권번호, 구독권명, 금액, 건수] - 금액 큰 순 */
  @SuppressWarnings("unchecked")
  public List<Object[]> byPlan(String from, String toNext) {
    return em.createNativeQuery("""
        SELECT o.PNO, MAX(s.PNAME), NVL(SUM(p.PRICE), 0), COUNT(*)
        FROM SHOP_PAYMENT p
        JOIN SHOP_ORDER o ON o.NO = p.ONO
        LEFT JOIN SHOP_PLAN s ON s.NO = o.PNO
        WHERE p.PSTATUS = 0 AND p.CDATE >= :from AND p.CDATE < :toNext
        GROUP BY o.PNO
        ORDER BY 3 DESC
        """)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 결제수단별 결제완료 [PMETHOD, 금액, 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> byMethod(String from, String toNext) {
    return em.createNativeQuery("""
        SELECT p.PMETHOD, NVL(SUM(p.PRICE), 0), COUNT(*)
        FROM SHOP_PAYMENT p
        WHERE p.PSTATUS = 0 AND p.CDATE >= :from AND p.CDATE < :toNext
        GROUP BY p.PMETHOD
        ORDER BY 2 DESC
        """)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 구독기간(6/12개월)별 결제완료 [PMONTH, 금액, 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> byMonth(String from, String toNext) {
    return em.createNativeQuery("""
        SELECT o.PMONTH, NVL(SUM(p.PRICE), 0), COUNT(*)
        FROM SHOP_PAYMENT p
        JOIN SHOP_ORDER o ON o.NO = p.ONO
        WHERE p.PSTATUS = 0 AND p.CDATE >= :from AND p.CDATE < :toNext
        GROUP BY o.PMONTH
        ORDER BY o.PMONTH
        """)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /**
   * 최근 결제 N건 (상태 무관)
   * [NO, CDATE, PRICE, PMETHOD, PSTATUS, 구독권명, 구독기간, CCTV대수, 회원명]
   */
  @SuppressWarnings("unchecked")
  public List<Object[]> recent(int limit) {
    return em.createNativeQuery("""
        SELECT p.NO, p.CDATE, p.PRICE, p.PMETHOD, p.PSTATUS, s.PNAME, o.PMONTH, o.CCNT, m.MNAME
        FROM SHOP_PAYMENT p
        LEFT JOIN SHOP_ORDER o ON o.NO = p.ONO
        LEFT JOIN SHOP_PLAN s ON s.NO = o.PNO
        LEFT JOIN MEMBER m ON m.NO = p.MNO
        ORDER BY p.CDATE DESC, p.NO DESC
        """)
        .setMaxResults(limit)
        .getResultList();
  }
}
