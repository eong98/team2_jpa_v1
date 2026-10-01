package dev.jpa.allimio.shopsurveyoption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 매장 설문 객관식 보기 Repository
 */
@Repository
public interface ShopSurveyOptionRepository extends JpaRepository<ShopSurveyOption, Long> {

  /**
   * 설문의 전체 보기 목록 (문항까지 JOIN FETCH, 문항 정렬 → 보기 정렬 순)
   * 문항별로 묶는 건 Service에서 합니다.
   */
  @Query("SELECT o FROM ShopSurveyOption o " +
         "JOIN FETCH o.question q " +
         "JOIN q.survey s " +
         "WHERE s.no = :svno " +
         "ORDER BY q.sort, q.no, o.sort, o.no")
  List<ShopSurveyOption> findBySvno(@Param("svno") Long svno);

  /**
   * 설문의 보기 일괄 삭제 (응답 0건인 설문 수정/삭제 시)
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM ShopSurveyOption o " +
         "WHERE o.question.no IN (SELECT q.no FROM ShopSurveyQuestion q WHERE q.survey.no = :svno)")
  int deleteBySvno(@Param("svno") Long svno);
}
