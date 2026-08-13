package dev.jpa.allimio.surveyanalysis;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 설문 전체 AI 분석 결과 Controller.
 *
 * Python AI 서버가 Oracle DB에 저장한
 * 설문 AI 분석 결과 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/survey-analysis")
public class SurveyAnalysisController {

    private final SurveyAnalysisService surveyAnalysisService;


    public SurveyAnalysisController(
            SurveyAnalysisService surveyAnalysisService) {

        this.surveyAnalysisService =
                surveyAnalysisService;
    }


    /**
     * 특정 설문의 AI 분석 결과를 조회한다.
     *
     * GET /api/survey-analysis/{surveyNo}
     */
    @GetMapping("/{surveyNo}")
    public ResponseEntity<SurveyAnalysisDTO> getBySurveyNo(
            @PathVariable("surveyNo") Long surveyNo) {

        return ResponseEntity.ok(
                surveyAnalysisService
                        .getBySurveyNo(surveyNo)
        );
    }
}