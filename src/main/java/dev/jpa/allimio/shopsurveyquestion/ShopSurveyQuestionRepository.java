package dev.jpa.allimio.shopsurveyquestion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 매장 설문 문항 Repository
 */
@Repository
public interface ShopSurveyQuestionRepository extends JpaRepository<ShopSurveyQuestion, Long> {

  /**
   * 설문별 문항 목록 (정렬순서)
   */
  @Query("SELECT q FROM ShopSurveyQuestion q " +
         "JOIN q.survey s " +
         "WHERE s.no = :svno " +
         "ORDER BY q.sort, q.no")
  List<ShopSurveyQuestion> findBySvno(@Param("svno") Long svno);

  /**
   * 설문의 문항 일괄 삭제 (응답 0건인 설문 수정/삭제 시)
   * 보기(SHOP_SURVEY_OPTION)를 먼저 지운 뒤 호출해야 합니다.
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM ShopSurveyQuestion q WHERE q.survey.no = :svno")
  int deleteBySvno(@Param("svno") Long svno);
}
