package dev.jpa.allimio.shopsurveyanswer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jpa.allimio.attach.Attach;

/**
 * 매장 설문 문항별 답 Repository
 */
@Repository
public interface ShopSurveyAnswerRepository extends JpaRepository<ShopSurveyAnswer, Long> {

  /**
   * 여러 응답의 문항별 답 (응답·문항 JOIN FETCH)
   * 응답 목록 화면에서 현재 페이지 응답들의 답을 한 번에 가져옵니다.
   */
  @Query("SELECT a FROM ShopSurveyAnswer a " +
         "JOIN FETCH a.response r " +
         "JOIN FETCH a.question q " +
         "WHERE r.no IN :srnos " +
         "ORDER BY r.no, q.sort, q.no")
  List<ShopSurveyAnswer> findByResponseNos(@Param("srnos") List<Long> srnos);

  /**
   * 여러 응답에 첨부된 사진 (ATTACH ↔ SHOP_SURVEY_ANSWER JOIN)
   * ATTACH는 FK 없이 TNAME + BNO로 연결되어 있어서 ON 절로 조인합니다.
   */
  @Query("SELECT at FROM Attach at " +
         "JOIN ShopSurveyAnswer a ON at.bno = a.no " +
         "JOIN a.response r " +
         "WHERE at.tname = 'SHOP_SURVEY_ANSWER' AND r.no IN :srnos " +
         "ORDER BY at.no")
  List<Attach> findAttachByResponseNos(@Param("srnos") List<Long> srnos);

  /**
   * 문항별 답 수 / 평균 점수 집계 (응답일시 기간 필터)
   * 결과: [0] 문항번호(Long), [1] 답 수(Long), [2] 평균 점수(Double, SCALE 외 null)
   * 답이 0건인 문항은 결과에 없으므로 Service에서 0으로 채웁니다.  
   */
  @Query("SELECT q.no, COUNT(a.no), AVG(a.scale) " +
      "FROM ShopSurveyAnswer a " +
               "JOIN a.question q " +
               "JOIN a.response r " +
               "JOIN r.survey s " +
               "WHERE s.no = :svno " +
               "AND (:from IS NULL OR r.cdate >= :from) " +
               "AND (:to IS NULL OR r.cdate <= :to) " +
               "GROUP BY q.no")
  List<Object[]> countAndAvgBySvno(@Param("svno") Long svno, @Param("from") String from, @Param("to") String to);
}
