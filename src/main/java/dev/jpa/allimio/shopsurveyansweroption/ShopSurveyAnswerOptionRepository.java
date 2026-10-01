package dev.jpa.allimio.shopsurveyansweroption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 매장 설문 객관식 답(선택한 보기) Repository
 */
@Repository
public interface ShopSurveyAnswerOptionRepository
    extends JpaRepository<ShopSurveyAnswerOption, ShopSurveyAnswerOption.Pk> {

  /**
   * 여러 응답에서 선택된 보기 (보기 JOIN FETCH → 보기내용 표시용)
   */
  @Query("SELECT ao FROM ShopSurveyAnswerOption ao " +
         "JOIN FETCH ao.option o " +
         "JOIN ao.answer a " +
         "JOIN a.response r " +
         "WHERE r.no IN :srnos " +
         "ORDER BY o.sort, o.no")
  List<ShopSurveyAnswerOption> findByResponseNos(@Param("srnos") List<Long> srnos);

  /**
   * 보기별 선택 수 집계
   * 결과: [0] 보기번호(Long), [1] 선택 수(Long)
   * 한 번도 선택되지 않은 보기도 0으로 나오도록 LEFT JOIN
   */
  @Query("SELECT o.no, COUNT(ao.answer) " +
         "FROM ShopSurveyOption o " +
         "JOIN o.question q " +
         "JOIN q.survey s " +
         "LEFT JOIN ShopSurveyAnswerOption ao ON ao.option = o " +
         "WHERE s.no = :svno " +
         "GROUP BY o.no")
  List<Object[]> countBySvno(@Param("svno") Long svno);
}
