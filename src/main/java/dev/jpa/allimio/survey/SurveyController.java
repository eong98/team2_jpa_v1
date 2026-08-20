package dev.jpa.allimio.survey;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.surveyresponse.SurveyResponseDTO;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    // 관리자: 설문 등록
    @PostMapping
    public ResponseEntity<SurveyDTO> createSurvey(@RequestBody SurveyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveyService.createSurvey(dto));
    }

    // 관리자: 전체 설문 조회
    @GetMapping
    public ResponseEntity<List<SurveyDTO>> getSurveyList() {
        return ResponseEntity.ok(surveyService.getSurveyList());
    }

    // 점주: 진행 중 설문 조회
    @GetMapping("/active")
    public ResponseEntity<List<SurveyDTO>> getActiveSurveyList() {
        return ResponseEntity.ok(surveyService.getActiveSurveyList());
    }

    // 설문 상세 조회
    @GetMapping("/{surveyNo}")
    public ResponseEntity<SurveyDTO> getSurvey(@PathVariable("surveyNo") Long surveyNo) {
        return ResponseEntity.ok(surveyService.getSurvey(surveyNo));
    }

    // 관리자: 설문 수정
    @PutMapping("/{surveyNo}")
    public ResponseEntity<SurveyDTO> updateSurvey(
            @PathVariable("surveyNo") Long surveyNo,
            @RequestBody SurveyDTO dto) {

        return ResponseEntity.ok(surveyService.updateSurvey(surveyNo, dto));
    }

    // 관리자: 설문 삭제
    @DeleteMapping("/{surveyNo}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable("surveyNo") Long surveyNo) {
        surveyService.deleteSurvey(surveyNo);
        return ResponseEntity.noContent().build();
    }

    // 점주: 설문 최초 제출
    @PostMapping("/{surveyNo}/responses")
    public ResponseEntity<SurveyResponseDTO> submitResponse(
            @PathVariable("surveyNo") Long surveyNo,
            @RequestBody SurveyResponseDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveyService.submitResponse(surveyNo, dto));
    }

    // 점주: 설문 기간 중 본인 응답 수정
    @PutMapping("/{surveyNo}/responses/member/{memberNo}")
    public ResponseEntity<SurveyResponseDTO> updateResponse(
            @PathVariable("surveyNo") Long surveyNo,
            @PathVariable("memberNo") Long memberNo,
            @RequestBody SurveyResponseDTO dto) {

        return ResponseEntity.ok(
                surveyService.updateResponse(surveyNo, memberNo, dto));
    }

    // 관리자: 설문 응답 목록
    @GetMapping("/{surveyNo}/responses")
    public ResponseEntity<List<SurveyResponseDTO>> getResponseList(
            @PathVariable("surveyNo") Long surveyNo) {

        return ResponseEntity.ok(
                surveyService.getResponseList(surveyNo));
    }

    // 점주: 본인 응답 조회
    @GetMapping("/{surveyNo}/responses/member/{memberNo}")
    public ResponseEntity<SurveyResponseDTO> getMemberResponse(
            @PathVariable("surveyNo") Long surveyNo,
            @PathVariable("memberNo") Long memberNo) {

        return ResponseEntity.ok(
                surveyService.getMemberResponse(surveyNo, memberNo));
    }

    // 관리자: 응답 상세 조회
    @GetMapping("/responses/{responseNo}")
    public ResponseEntity<SurveyResponseDTO> getResponse(
            @PathVariable("responseNo") Long responseNo) {

        return ResponseEntity.ok(
                surveyService.getResponse(responseNo));
    }

    // 관리자: 응답 확인
    @PatchMapping("/responses/{responseNo}/check")
    public ResponseEntity<SurveyResponseDTO> checkResponse(
            @PathVariable("responseNo") Long responseNo) {

        return ResponseEntity.ok(
                surveyService.checkResponse(responseNo));
    }
}