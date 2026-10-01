package dev.jpa.allimio.shopsurveyresponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 매장 설문 답변(고객 1회 제출) Repository
 */
@Repository
public interface ShopSurveyResponseRepository extends JpaRepository<ShopSurveyResponse, Long> {

  /**
   * 설문별 응답 목록 (최신순, 페이징)
   */
  @Query(value = "SELECT r FROM ShopSurveyResponse r " +
                 "JOIN r.survey s " +
                 "WHERE s.no = :svno " +
                 "ORDER BY r.cdate DESC, r.no DESC",
         countQuery = "SELECT COUNT(r) FROM ShopSurveyResponse r " +
                      "JOIN r.survey s " +
                      "WHERE s.no = :svno")
  Page<ShopSurveyResponse> findPageBySvno(@Param("svno") Long svno, Pageable pageable);

  /**
   * 설문별 응답 수 (수정 잠금/삭제 방식 판단, 집계용)
   */
  @Query("SELECT COUNT(r) FROM ShopSurveyResponse r " +
         "JOIN r.survey s " +
         "WHERE s.no = :svno")
  long countBySvno(@Param("svno") Long svno);

  /**
   * 같은 IP가 특정 시각 이후 이 설문에 제출한 횟수 (도배 방지)
   *
   * @param from 기준 시각 (yyyy-MM-dd HH:mm:ss, 문자열 비교)
   */
  @Query("SELECT COUNT(r) FROM ShopSurveyResponse r " +
         "JOIN r.survey s " +
         "WHERE s.no = :svno AND r.ipAddr = :ip AND r.cdate >= :from")
  long countRecentByIp(@Param("svno") Long svno, @Param("ip") String ip, @Param("from") String from);
}
