package dev.jpa.allimio.survey;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.surveyanswer.SurveyAnswerDTO;
import dev.jpa.allimio.surveyresponse.SurveyResponseDTO;


/**
 * 설문조사 REST API Controller.
 *
 * 관리자 설문 관리, 점주 설문 응답,
 * 관리자 응답 확인, AI 평가점수 저장 API를 제공한다.
 */
@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * 관리자: 설문과 문항을 등록한다.
     */
    @PostMapping
    public ResponseEntity<SurveyDTO> createSurvey(
            @RequestBody SurveyDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(surveyService.createSurvey(dto));
    }

    /**
     * 관리자: 전체 설문 목록을 조회한다.
     */
    @GetMapping
    public ResponseEntity<List<SurveyDTO>> getSurveyList() {

        return ResponseEntity.ok(
                surveyService.getSurveyList());
    }

    /**
     * 점주: 현재 참여 가능한 설문 목록을 조회한다.
     */
    @GetMapping("/active")
    public ResponseEntity<List<SurveyDTO>> getActiveSurveyList() {

        return ResponseEntity.ok(
                surveyService.getActiveSurveyList());
    }

    /**
     * 관리자·점주: 설문 상세와 문항 목록을 조회한다.
     */
    @GetMapping("/{surveyNo}")
    public ResponseEntity<SurveyDTO> getSurvey(
            @PathVariable("surveyNo") Long surveyNo) {

        return ResponseEntity.ok(
                surveyService.getSurvey(surveyNo));
    }

    /**
     * 관리자: 설문과 문항을 수정한다.
     */
    @PutMapping("/{surveyNo}")
    public ResponseEntity<SurveyDTO> updateSurvey(
            @PathVariable("surveyNo") Long surveyNo,
            @RequestBody SurveyDTO dto) {

        return ResponseEntity.ok(
                surveyService.updateSurvey(
                        surveyNo,
                        dto));
    }

    /**
     * 관리자: 설문을 삭제한다.
     */
    @DeleteMapping("/{surveyNo}")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable("surveyNo") Long surveyNo) {

        surveyService.deleteSurvey(surveyNo);

        return ResponseEntity
                .noContent()
                .build();
    }

    /**
     * 점주: 설문 답변을 최종 제출한다.
     */
    @PostMapping("/{surveyNo}/responses")
    public ResponseEntity<SurveyResponseDTO> submitResponse(
            @PathVariable("surveyNo") Long surveyNo,
            @RequestBody SurveyResponseDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        surveyService.submitResponse(
                                surveyNo,
                                dto));
    }

    /**
     * 관리자: 특정 설문의 전체 점주 응답 목록을 조회한다.
     */
    @GetMapping("/{surveyNo}/responses")
    public ResponseEntity<List<SurveyResponseDTO>> getResponseList(
            @PathVariable("surveyNo") Long surveyNo) {

        return ResponseEntity.ok(
                surveyService.getResponseList(
                        surveyNo));
    }

    /**
     * 점주: 본인이 제출한 설문 결과를 조회한다.
     */
    @GetMapping("/{surveyNo}/responses/member/{memberNo}")
    public ResponseEntity<SurveyResponseDTO> getMemberResponse(
            @PathVariable("surveyNo") Long surveyNo,
            @PathVariable("memberNo") Long memberNo) {

        return ResponseEntity.ok(
                surveyService.getMemberResponse(
                        surveyNo,
                        memberNo));
    }

    /**
     * 관리자: 응답 한 건의 문항별 실제 답변을 조회한다.
     */
    @GetMapping("/responses/{responseNo}")
    public ResponseEntity<SurveyResponseDTO> getResponse(
            @PathVariable("responseNo") Long responseNo) {

        return ResponseEntity.ok(
                surveyService.getResponse(
                        responseNo));
    }

    /**
     * 관리자: 점주의 전체 설문 응답을 확인 처리한다.
     */
    @PatchMapping("/responses/{responseNo}/check")
    public ResponseEntity<SurveyResponseDTO> checkResponse(
            @PathVariable("responseNo") Long responseNo) {

        return ResponseEntity.ok(
                surveyService.checkResponse(
                        responseNo));
    }

    /**
     * AI 또는 관리자:
     * 문항별 AI 평가점수를 저장한다.
     *
     * 요청 예시:
     * {
     *   "evalScore": 4
     * }
     */
    @PatchMapping("/answers/{answerNo}/evaluation")
    public ResponseEntity<SurveyAnswerDTO> updateEvaluation(
            @PathVariable("answerNo") Long answerNo,
            @RequestBody Map<String, BigDecimal> request) {

        return ResponseEntity.ok(
                surveyService.updateEvaluation(
                        answerNo,
                        request.get("evalScore")));
    }
}