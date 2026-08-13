package dev.jpa.allimio.surveyanalysis;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 설문 전체 AI 분석 결과 조회 Service.
 *
 * Python AI 서버가 SURVEYANALYSIS 테이블에 저장한
 * 분석 결과를 조회하여 React에 전달한다.
 */
@Service
@Transactional(readOnly = true)
public class SurveyAnalysisService {

    private final SurveyAnalysisRepository surveyAnalysisRepository;


    public SurveyAnalysisService(
            SurveyAnalysisRepository surveyAnalysisRepository) {

        this.surveyAnalysisRepository =
                surveyAnalysisRepository;
    }


    /**
     * 설문번호로 AI 분석 결과를 조회한다.
     *
     * @param surveyNo 설문번호
     * @return AI 분석 결과 DTO
     */
    public SurveyAnalysisDTO getBySurveyNo(
            Long surveyNo) {

        if (surveyNo == null) {
            throw new IllegalArgumentException(
                    "설문번호는 필수입니다.");
        }

        SurveyAnalysis analysis =
                surveyAnalysisRepository
                        .findBySurveyNo(surveyNo)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 설문의 AI 분석 결과가 없습니다: "
                                                + surveyNo
                                )
                        );

        return toDTO(analysis);
    }


    /**
     * Entity -> DTO 변환.
     */
    private SurveyAnalysisDTO toDTO(
            SurveyAnalysis analysis) {

        SurveyAnalysisDTO dto =
                new SurveyAnalysisDTO();

        dto.setNo(
                analysis.getNo()
        );

        dto.setSurveyNo(
                analysis.getSurveyNo()
        );

        dto.setAiScore(
                analysis.getAiScore()
        );

        dto.setPositiveRate(
                analysis.getPositiveRate()
        );

        dto.setNeutralRate(
                analysis.getNeutralRate()
        );

        dto.setNegativeRate(
                analysis.getNegativeRate()
        );

        dto.setSummary(
                analysis.getSummary()
        );

        dto.setPositiveSummary(
                analysis.getPositiveSummary()
        );

        dto.setNegativeSummary(
                analysis.getNegativeSummary()
        );

        dto.setCdate(
                analysis.getCdate()
        );

        return dto;
    }
}