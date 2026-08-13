package dev.jpa.allimio.surveyanalysis;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 설문 전체 AI 분석 결과 Repository.
 */
@Repository
public interface SurveyAnalysisRepository
        extends JpaRepository<SurveyAnalysis, Long> {

    /**
     * 설문번호로 AI 분석 결과를 조회한다.
     *
     * 설문 하나당 분석 결과는 최대 한 건이므로
     * Optional로 반환한다.
     */
    Optional<SurveyAnalysis> findBySurveyNo(Long surveyNo);


    /**
     * 해당 설문의 AI 분석 결과가 존재하는지 확인한다.
     */
    boolean existsBySurveyNo(Long surveyNo);
}