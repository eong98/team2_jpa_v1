package dev.jpa.allimio.dashboard;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/* ---------------------------------------------------------------------
   /user/dashboard 통계 전용 조회 Repository.

   - 기존 CctvIssueRepository / CctvVisitorRepository(목록 화면용)는 건드리지 않고,
     통계용 집계 쿼리(GROUP BY / COUNT / AVG)만 여기 모았습니다.
   - JpaRepository<T, ID>는 엔티티 1개에 묶이는데, 대시보드는 SHOP / SHOP_MEMBER / CCTV /
     CCTV_ISSUE / CCTV_VISITOR / CCTV_ISSUE_CODE 6개 테이블을 가로질러 집계하므로
     EntityManager로 JPQL을 직접 실행합니다.

   ⚠️ 날짜 컬럼(INTIME / CDATE)은 VARCHAR 'yyyy-MM-dd HH:mm:ss' 문자열입니다.
     기간 필터는 SUBSTRING(...)으로 자르지 않고 문자열 범위 비교
       col >= 'yyyy-MM-dd'(시작일)  AND  col < 'yyyy-MM-dd'(종료일 다음날)
     로 처리합니다. 사전순 비교가 날짜순과 같아서 결과가 동일하고, 컬럼에 함수를 씌우지
     않으니 나중에 INTIME/CDATE에 인덱스를 걸면 그대로 인덱스를 탈 수 있습니다.
     (GROUP BY 할 때만 SUBSTRING으로 날짜/시간 부분을 자릅니다)

   ⚠️ CCTV_ISSUE / CCTV_VISITOR에는 SNO(매장) 컬럼이 없어서, 기존 목록 쿼리와 똑같이
     CNO IN (SELECT c.no FROM Cctv c WHERE c.sno = :sno) 서브쿼리로 매장을 거릅니다.
--------------------------------------------------------------------- */
@Repository
public class DashboardRepository {

  @PersistenceContext
  private EntityManager em;

  // 매장(sno) 소유 CCTV 번호 서브쿼리 - 이슈/방문객 쿼리에서 공통으로 씀
  private static final String SHOP_CCTV = "(SELECT c.no FROM Cctv c WHERE c.sno = :sno)";

  /* =========================================================
     매장 / 소속매장
  ========================================================= */

  /** 점주: SHOP.MNO가 로그인 회원인 매장 [no, title] */
  public List<Object[]> findOwnedShops(long mno) {
    return em.createQuery("""
        SELECT s.no, s.title FROM Shop s
        WHERE s.mno = :mno
        ORDER BY s.cdate DESC
        """, Object[].class)
        .setParameter("mno", mno)
        .getResultList();
  }

  /** 직원: SHOP_MEMBER에 배정된 매장 [no, title] */
  public List<Object[]> findStaffShops(long mno) {
    return em.createQuery("""
        SELECT s.no, s.title FROM Shop s
        WHERE s.no IN (SELECT sm.sno FROM ShopMember sm WHERE sm.mno = :mno)
        ORDER BY s.cdate DESC
        """, Object[].class)
        .setParameter("mno", mno)
        .getResultList();
  }

  /* =========================================================
     방문객 (CCTV_VISITOR)
  ========================================================= */

  /** 기간 내 방문객 수 (INTIME 기준) */
  public long countVisitors(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT COUNT(v) FROM CctvVisitor v
        WHERE v.cno IN """ + SHOP_CCTV + """
          AND v.intime >= :from AND v.intime < :toNext
        """, Long.class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getSingleResult();
  }

  /** 기간 내 평균 체류시간(분). STAYTIME이 NULL(아직 입장중)인 행은 AVG에서 자동 제외 */
  public Double avgStayMinutes(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT AVG(v.staytime) FROM CctvVisitor v
        WHERE v.cno IN """ + SHOP_CCTV + """
          AND v.intime >= :from AND v.intime < :toNext
        """, Double.class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getSingleResult();
  }

  /** 상태값(state)별 방문객 수 - state만 넘기면 기간 무관, from/toNext까지 넘기면 기간 내 */
  public long countVisitorsByState(long sno, int state, String from, String toNext) {
    boolean ranged = from != null && toNext != null;
    var q = em.createQuery("""
        SELECT COUNT(v) FROM CctvVisitor v
        WHERE v.cno IN """ + SHOP_CCTV + """
          AND v.state = :state
        """ + (ranged ? " AND v.intime >= :from AND v.intime < :toNext" : ""), Long.class)
        .setParameter("sno", sno)
        .setParameter("state", state);
    if (ranged) {
      q.setParameter("from", from).setParameter("toNext", toNext);
    }
    return q.getSingleResult();
  }

  /** 일별 방문객 [yyyy-MM-dd, count] */
  public List<Object[]> visitorDaily(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT SUBSTRING(v.intime, 1, 10), COUNT(v) FROM CctvVisitor v
        WHERE v.cno IN """ + SHOP_CCTV + """
          AND v.intime >= :from AND v.intime < :toNext
        GROUP BY SUBSTRING(v.intime, 1, 10)
        """, Object[].class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 시간대별 방문객 [HH, count] - 'yyyy-MM-dd HH:mm:ss'의 12~13번째 글자가 시(HH) */
  public List<Object[]> visitorHourly(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT SUBSTRING(v.intime, 12, 2), COUNT(v) FROM CctvVisitor v
        WHERE v.cno IN """ + SHOP_CCTV + """
          AND v.intime >= :from AND v.intime < :toNext
        GROUP BY SUBSTRING(v.intime, 12, 2)
        """, Object[].class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /* =========================================================
     이슈 (CCTV_ISSUE)
  ========================================================= */

  /** 기간 내 이슈 수 (CDATE 기준) */
  public long countIssues(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT COUNT(ci) FROM CctvIssue ci
        WHERE ci.cno IN """ + SHOP_CCTV + """
          AND ci.cdate >= :from AND ci.cdate < :toNext
        """, Long.class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getSingleResult();
  }

  /** 기간 내 처리상태(state)별 이슈 수 [state, count] (0 미확인 / 1 정탐 / 2 오탐) */
  public List<Object[]> issueByState(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT ci.state, COUNT(ci) FROM CctvIssue ci
        WHERE ci.cno IN """ + SHOP_CCTV + """
          AND ci.cdate >= :from AND ci.cdate < :toNext
        GROUP BY ci.state
        """, Object[].class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 일별 이슈 [yyyy-MM-dd, count] */
  public List<Object[]> issueDaily(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT SUBSTRING(ci.cdate, 1, 10), COUNT(ci) FROM CctvIssue ci
        WHERE ci.cno IN """ + SHOP_CCTV + """
          AND ci.cdate >= :from AND ci.cdate < :toNext
        GROUP BY SUBSTRING(ci.cdate, 1, 10)
        """, Object[].class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /**
   * 이상행동 유형별 이슈 [code, codeName, severity, count].
   * CctvIssue ↔ CctvIssueCode는 @ManyToOne 연관관계가 없어서, Hibernate 6의
   * "엔티티 조인 + ON" 문법으로 코드명을 붙입니다. 코드 테이블에 없는 코드가 와도
   * 빠지지 않도록 LEFT JOIN (codeName은 NULL → 서비스에서 코드값으로 대체).
   */
  public List<Object[]> issueByCode(long sno, String from, String toNext) {
    return em.createQuery("""
        SELECT ci.code, cc.codeName, COALESCE(cc.severity, 1), COUNT(ci)
        FROM CctvIssue ci
        LEFT JOIN CctvIssueCode cc ON cc.code = ci.code
        WHERE ci.cno IN """ + SHOP_CCTV + """
          AND ci.cdate >= :from AND ci.cdate < :toNext
        GROUP BY ci.code, cc.codeName, cc.severity
        ORDER BY COUNT(ci) DESC
        """, Object[].class)
        .setParameter("sno", sno)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 최근 이슈 N건 [no, cno, code, codeName, state, reliability, cdate] (기간 무관, 최신순) */
  public List<Object[]> recentIssues(long sno, int limit) {
    return em.createQuery("""
        SELECT ci.no, ci.cno, ci.code, cc.codeName, ci.state, ci.reliability, ci.cdate
        FROM CctvIssue ci
        LEFT JOIN CctvIssueCode cc ON cc.code = ci.code
        WHERE ci.cno IN """ + SHOP_CCTV + """
        ORDER BY ci.cdate DESC
        """, Object[].class)
        .setParameter("sno", sno)
        .setMaxResults(limit) // Oracle 12c+ → FETCH FIRST n ROWS ONLY로 변환됨
        .getResultList();
  }

  /* =========================================================
     CCTV
  ========================================================= */

  /** 매장 CCTV 상태별 대수 [state, count] (0 정상 / 1 점검중 / 2 고장) */
  public List<Object[]> cctvByState(long sno) {
    return em.createQuery("""
        SELECT c.state, COUNT(c) FROM Cctv c
        WHERE c.sno = :sno
        GROUP BY c.state
        ORDER BY c.state
        """, Object[].class)
        .setParameter("sno", sno)
        .getResultList();
  }
}
