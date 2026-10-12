package dev.jpa.allimio.qa.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import dev.jpa.allimio.qa.mail.QaMailDTO.LogPage;
import dev.jpa.allimio.qa.mail.QaMailDTO.LogRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/* ---------------------------------------------------------------------
   문의 메일 발송 내역 — 관리자 목록 조회 전용 (ChatStatsRepository와 같은 네이티브 쿼리 방식)

   QA_SEND_LOG 를 기준으로 QA(문의 제목·삭제 여부), MEMBER(회원 이름)를 LEFT JOIN 합니다.
   - 문의글이 영구 삭제됐으면 QA 조인 결과가 없고, 소프트 삭제됐으면 ISDEL='Y' → 둘 다 "삭제된 게시글"
   - 비회원 글이면 MNO가 없어 MEMBER 조인 결과가 없음
--------------------------------------------------------------------- */
@Repository
public class QaSendLogQuery {

  @PersistenceContext
  private EntityManager em;

  private static final String FROM = """
       FROM QA_SEND_LOG l
       LEFT JOIN QA q ON q.NO = l.QNO
       LEFT JOIN MEMBER m ON m.NO = l.MNO
      """;

  /**
   * 발송 내역 목록 (최근 발송 순)
   *
   * @param word   받는 사람 이메일 · 메일 제목 · 문의 제목 검색어 (없으면 전체)
   * @param stype  1 답변 알림 / 2 재설정 인증 (null이면 전체)
   * @param result Y 성공 / N 실패 (null이면 전체)
   * @param page   0부터
   */
  public LogPage search(String word, Integer stype, String result, int page, int size) {
    StringBuilder where = new StringBuilder(" WHERE 1 = 1");
    Map<String, Object> params = new HashMap<>();

    if (word != null && !word.isBlank()) {
      where.append(" AND (LOWER(l.EMAIL) LIKE :word OR LOWER(l.TITLE) LIKE :word OR LOWER(q.TITLE) LIKE :word)");
      params.put("word", "%" + word.trim().toLowerCase() + "%");
    }
    if (stype != null) {
      where.append(" AND l.STYPE = :stype");
      params.put("stype", stype);
    }
    if ("Y".equals(result) || "N".equals(result)) {
      where.append(" AND l.RESULT = :result");
      params.put("result", result);
    }

    Query countQuery = em.createNativeQuery("SELECT COUNT(*)" + FROM + where);
    params.forEach(countQuery::setParameter);
    long total = ((Number) countQuery.getSingleResult()).longValue();

    String sql = """
        SELECT l.NO, l.QNO,
               CASE WHEN q.NO IS NULL OR q.ISDEL = 'Y' THEN NULL ELSE q.TITLE END AS QA_TITLE,
               CASE WHEN q.NO IS NULL OR q.ISDEL = 'Y' THEN 1 ELSE 0 END AS QA_DELETED,
               l.MNO, m.MNAME, l.EMAIL, l.STYPE, l.TITLE, l.CONTENT, l.RESULT, l.MSG, l.CDATE,
               CASE WHEN l.STYPE = 1 AND l.RESULT = 'N' AND q.NO IS NOT NULL AND q.ISDEL = 'N'
                         AND NOT EXISTS (SELECT 1 FROM QA_SEND_LOG s
                                          WHERE s.QNO = l.QNO AND s.STYPE = 1 AND s.RESULT = 'Y' AND s.NO > l.NO)
                    THEN 1 ELSE 0 END AS CAN_RESEND
        """ + FROM + where + " ORDER BY l.NO DESC";

    Query query = em.createNativeQuery(sql);
    params.forEach(query::setParameter);
    query.setFirstResult(page * size);
    query.setMaxResults(size);

    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    List<LogRow> content = new ArrayList<>();
    for (Object[] r : rows) {
      content.add(new LogRow(
          num(r[0]), num(r[1]), str(r[2]), num(r[3]) == 1,
          r[4] == null ? null : num(r[4]), str(r[5]), str(r[6]), (int) num(r[7]),
          str(r[8]), str(r[9]), str(r[10]), str(r[11]), str(r[12]), num(r[13]) == 1));
    }

    int totalPages = (int) Math.ceil(total / (double) size);
    return new LogPage(content, total, totalPages, page, size);
  }

  private static long num(Object o) {
    return o == null ? 0 : ((Number) o).longValue();
  }

  private static String str(Object o) {
    return o == null ? null : String.valueOf(o).trim(); // CHAR(1) 뒤 공백 제거 겸용
  }
}
