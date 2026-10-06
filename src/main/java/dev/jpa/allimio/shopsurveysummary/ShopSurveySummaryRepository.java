package dev.jpa.allimio.shopsurveysummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 매장 설문 AI 요약 결과 Repository
 *
 * PK가 설문번호(SVNO)라서 save()가 그대로 덮어쓰기입니다.
 * (같은 SVNO가 있으면 UPDATE, 없으면 INSERT)
 */
@Repository
public interface ShopSurveySummaryRepository extends JpaRepository<ShopSurveySummary, Long> {
}