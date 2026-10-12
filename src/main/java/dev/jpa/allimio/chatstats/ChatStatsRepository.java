package dev.jpa.allimio.chatstats;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/* ---------------------------------------------------------------------
   /dbms/chatstats 챗봇 상담 만족도 통계 전용 조회 Repository.
   CHAT_SESSION 한 테이블을 집계합니다. (기존 ChatSessionRepository는 건드리지 않음)

   기준
   - 대상: 종료된 상담 (CMODE = 2)
   - 날짜: 종료 시각 CLOSEDAT (없으면 마지막 활동 UDATE) — 만족도는 종료할 때 기록되므로
   - 날짜 컬럼은 VARCHAR 'yyyy-MM-dd HH:mm:ss' 문자열 → PayStatsRepository와 같이
       col >= 'yyyy-MM-dd'(시작일) AND col < 'yyyy-MM-dd'(종료일 다음날) 문자열 범위 비교

   만족도 SATISFY : 1 만족 / 0 불만족 / NULL 미응답
   불만족 사유 SREASON : 0 답변부정확 / 1 응답느림 / 2 원하는답못찾음 / 9 기타(SMEMO 직접입력)
   종료 사유 CREASON : 0 사용자 종료 / 1 관리자연결 / 2 자동종료(30분 방치)
--------------------------------------------------------------------- */
@Repository
public class ChatStatsRepository {

  @PersistenceContext
  private EntityManager em;

  /** 종료 시각 (없으면 마지막 활동 시각) */
  private static final String CLOSED_AT = "NVL(s.CLOSEDAT, s.UDATE)";

  /** 기간 내 종료된 상담 */
  private static final String PERIOD =
      " s.CMODE = 2 AND " + CLOSED_AT + " >= :from AND " + CLOSED_AT + " < :toNext ";

  /**
   * 기간 요약 [종료 수, 응답 수, 만족, 불만족, 사용자종료, 관리자연결, 자동종료]
   */
  public Object[] summary(String from, String toNext) {
    return (Object[]) em.createNativeQuery("""
        SELECT COUNT(*),
               COUNT(s.SATISFY),
               NVL(SUM(CASE WHEN s.SATISFY = 1 THEN 1 ELSE 0 END), 0),
               NVL(SUM(CASE WHEN s.SATISFY = 0 THEN 1 ELSE 0 END), 0),
               NVL(SUM(CASE WHEN s.CREASON = 0 THEN 1 ELSE 0 END), 0),
               NVL(SUM(CASE WHEN s.CREASON = 1 THEN 1 ELSE 0 END), 0),
               NVL(SUM(CASE WHEN s.CREASON = 2 THEN 1 ELSE 0 END), 0)
        FROM CHAT_SESSION s
        WHERE""" + PERIOD)
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getSingleResult();
  }

  /** 현재 진행 중인 상담 수 (종료되지 않은 세션) */
  public long countInProgress() {
    return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM CHAT_SESSION WHERE CMODE <> 2")
        .getSingleResult()).longValue();
  }

  /** 일별 [yyyy-MM-dd, 종료 수, 만족, 불만족] */
  @SuppressWarnings("unchecked")
  public List<Object[]> daily(String from, String toNext) {
    return em.createNativeQuery(
        "SELECT SUBSTR(" + CLOSED_AT + ", 1, 10), COUNT(*), "
            + "NVL(SUM(CASE WHEN s.SATISFY = 1 THEN 1 ELSE 0 END), 0), "
            + "NVL(SUM(CASE WHEN s.SATISFY = 0 THEN 1 ELSE 0 END), 0) "
            + "FROM CHAT_SESSION s WHERE" + PERIOD
            + "GROUP BY SUBSTR(" + CLOSED_AT + ", 1, 10)")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 불만족 사유별 [SREASON(없으면 NULL), 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> byReason(String from, String toNext) {
    return em.createNativeQuery(
        "SELECT s.SREASON, COUNT(*) FROM CHAT_SESSION s WHERE s.SATISFY = 0 AND" + PERIOD
            + "GROUP BY s.SREASON ORDER BY COUNT(*) DESC")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 종료 사유별 [CREASON(없으면 NULL), 건수] */
  @SuppressWarnings("unchecked")
  public List<Object[]> byCloseReason(String from, String toNext) {
    return em.createNativeQuery(
        "SELECT s.CREASON, COUNT(*) FROM CHAT_SESSION s WHERE" + PERIOD
            + "GROUP BY s.CREASON ORDER BY COUNT(*) DESC")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 회원/비회원별 [1 회원 | 0 비회원, 종료 수, 만족, 불만족] */
  @SuppressWarnings("unchecked")
  public List<Object[]> byUserType(String from, String toNext) {
    return em.createNativeQuery(
        "SELECT CASE WHEN s.MNO IS NULL THEN 0 ELSE 1 END, COUNT(*), "
            + "NVL(SUM(CASE WHEN s.SATISFY = 1 THEN 1 ELSE 0 END), 0), "
            + "NVL(SUM(CASE WHEN s.SATISFY = 0 THEN 1 ELSE 0 END), 0) "
            + "FROM CHAT_SESSION s WHERE" + PERIOD
            + "GROUP BY CASE WHEN s.MNO IS NULL THEN 0 ELSE 1 END")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /** 불만족 고객이 직접 쓴 의견 (최근순) — AI 분석용 */
  @SuppressWarnings("unchecked")
  public List<Object> dissatisfiedMemos(String from, String toNext, int limit) {
    return em.createNativeQuery(
        "SELECT s.SMEMO FROM CHAT_SESSION s WHERE s.SATISFY = 0 AND s.SMEMO IS NOT NULL AND" + PERIOD
            + "ORDER BY " + CLOSED_AT + " DESC FETCH FIRST " + limit + " ROWS ONLY")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /**
   * 불만족 상담에서 고객이 고른 메뉴(MTYPE=0)와 입력한 글(MTYPE=2) (최근순) — AI 분석용
   * 자유 입력에는 [상담 종료]·[불만족] 같은 버튼 문구도 같이 기록되므로 서비스에서 걸러 냄
   */
  @SuppressWarnings("unchecked")
  public List<Object> dissatisfiedUserMessages(String from, String toNext, int limit) {
    return em.createNativeQuery(
        "SELECT DBMS_LOB.SUBSTR(l.CONTENT, 300, 1) FROM CHAT_LOG l JOIN CHAT_SESSION s ON s.NO = l.SNO "
            + "WHERE l.SENDER = 0 AND l.MTYPE IN (0, 2) AND s.SATISFY = 0 AND" + PERIOD
            + "ORDER BY l.NO DESC FETCH FIRST " + limit + " ROWS ONLY")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }

  /**
   * 기간 내 최근 불만족 의견 [NO, 종료시각, STITLE, SREASON, SMEMO, MNO, 마지막으로 고른 메뉴 이름]
   * 메뉴 이름은 상담 제목(STITLE, AI 요약)이 아직 없을 때 대신 보여주는 용도
   */
  @SuppressWarnings("unchecked")
  public List<Object[]> recentDissatisfied(String from, String toNext, int limit) {
    return em.createNativeQuery(
        "SELECT s.NO, " + CLOSED_AT + ", s.STITLE, s.SREASON, s.SMEMO, s.MNO, m.LABEL "
            + "FROM CHAT_SESSION s LEFT JOIN CHAT_MENU m ON m.NO = s.CNO WHERE s.SATISFY = 0 AND" + PERIOD
            + "ORDER BY " + CLOSED_AT + " DESC FETCH FIRST " + limit + " ROWS ONLY")
        .setParameter("from", from)
        .setParameter("toNext", toNext)
        .getResultList();
  }
}
