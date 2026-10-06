package dev.jpa.allimio.shopsurvey;

import dev.jpa.allimio.shopsurveyanswer.ShopSurveyAnswerDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.allimio.tool.PageResponse;
import lombok.RequiredArgsConstructor;

/**
 * 매장 설문조사 Controller
 *
 * [점주, 로그인 필요]
 *   GET    /shop_survey/list/{sno}            매장별 설문 목록
 *   GET    /shop_survey/{svno}                설문 상세 (폼 데이터)
 *   POST   /shop_survey/draft                 임시저장
 *   POST   /shop_survey/publish               게시
 *   PUT    /shop_survey/{svno}                게시된 설문 수정 (응답 0건일 때만)
 *   PATCH  /shop_survey/{svno}/status         OPEN <-> CLOSED
 *   DELETE /shop_survey/{svno}                삭제
 *   GET    /shop_survey/{svno}/responses      응답 목록
 *   GET    /shop_survey/{svno}/stats          문항별 집계
 *   POST   /shop_survey/{svno}/summary        AI 요약 + 긍정/부정 점수
 *   (responses / stats / summary 는 ?from=yyyy-MM-dd&to=yyyy-MM-dd 응답일 필터 지원)
 *
 * [고객, 비로그인] SecurityConfig에서 /shop_survey/public/** permitAll
 *   GET    /shop_survey/public/{qrid}         설문 조회
 *   POST   /shop_survey/public/{qrid}/submit  응답 제출 (multipart)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/shop_survey")
public class ShopSurveyCont {

  private final ShopSurveyService surveyService;
  private final ObjectMapper objectMapper;

  /** 사진 파트 이름 접두어: file_{문항번호} */
  private static final String FILE_PREFIX = "file_";

  // =====================================================================
  // [점주]
  // =====================================================================

  /**
   * 매장별 설문 목록
   * GET /shop_survey/list/{sno}?status=OPEN&page=0&size=10
   */
  @GetMapping("/list/{sno}")
  public ResponseEntity<PageResponse<ShopSurveyListDTO>> list(
      Authentication authentication,
      @PathVariable("sno") Long sno,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    return ResponseEntity.ok(surveyService.list(ownerNo(authentication), sno, status, page, size));
  }

  /**
   * 설문 상세 (수정 폼에 채울 데이터)
   * GET /shop_survey/{svno}
   */
  @GetMapping("/{svno}")
  public ResponseEntity<ShopSurveyDTO> detail(
      Authentication authentication,
      @PathVariable("svno") Long svno) {
    return ResponseEntity.ok(surveyService.detail(ownerNo(authentication), svno));
  }

  /**
   * 임시저장 (no가 없으면 신규, 있으면 덮어쓰기)
   * POST /shop_survey/draft
   */
  @PostMapping("/draft")
  public ResponseEntity<Map<String, Object>> saveDraft(
      Authentication authentication,
      @RequestBody ShopSurveyDTO form) {
    Long no = surveyService.saveDraft(ownerNo(authentication), form);
    return ResponseEntity.ok(Map.of("success", true, "no", no));
  }

  /**
   * 게시 (신규 바로 게시 또는 임시저장 → 게시)
   * POST /shop_survey/publish
   */
  @PostMapping("/publish")
  public ResponseEntity<Map<String, Object>> publish(
      Authentication authentication,
      @RequestBody ShopSurveyDTO form) {
    ShopSurvey survey = surveyService.publish(ownerNo(authentication), form);
    return ResponseEntity.ok(Map.of("success", true, "no", survey.getNo(), "qrid", survey.getQrid()));
  }

  /**
   * 게시된 설문 수정 (응답 0건일 때만)
   * PUT /shop_survey/{svno}
   */
  @PutMapping("/{svno}")
  public ResponseEntity<Map<String, Object>> update(
      Authentication authentication,
      @PathVariable("svno") Long svno,
      @RequestBody ShopSurveyDTO form) {
    surveyService.update(ownerNo(authentication), svno, form);
    return ResponseEntity.ok(Map.of("success", true));
  }

  /**
   * 상태 변경 (OPEN <-> CLOSED)
   * PATCH /shop_survey/{svno}/status   body: { "status": "CLOSED" }
   */
  @PatchMapping("/{svno}/status")
  public ResponseEntity<Map<String, Object>> changeStatus(
      Authentication authentication,
      @PathVariable("svno") Long svno,
      @RequestBody ShopSurveyDTO.StatusRequest request) {
    surveyService.changeStatus(ownerNo(authentication), svno, request.getStatus());
    return ResponseEntity.ok(Map.of("success", true));
  }

  /**
   * 삭제 (응답 0건이면 실제 삭제, 있으면 DELETE 상태로 숨김)
   * DELETE /shop_survey/{svno}
   */
  @DeleteMapping("/{svno}")
  public ResponseEntity<Map<String, Object>> delete(
      Authentication authentication,
      @PathVariable("svno") Long svno) {
    boolean removed = surveyService.delete(ownerNo(authentication), svno);
    return ResponseEntity.ok(Map.of("success", true, "removed", removed));
  }

  /**
   * 응답 목록 (최신순)
   * GET /shop_survey/{svno}/responses?page=0&size=10
   */
  @GetMapping("/{svno}/responses")
  public ResponseEntity<PageResponse<ShopSurveyAnswerDTO.Response>> responses(
      Authentication authentication,
      @PathVariable("svno") Long svno,
      @RequestParam(name = "from", required = false) String from,
      @RequestParam(name = "to", required = false) String to,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    return ResponseEntity.ok(surveyService.responses(ownerNo(authentication), svno, from, to, page, size));
  }

  /**
   * 문항별 집계
   * GET /shop_survey/{svno}/stats
   */
  @GetMapping("/{svno}/stats")
  public ResponseEntity<ShopSurveyAnswerDTO.Stats> stats(
      Authentication authentication,
      @PathVariable("svno") Long svno,
      @RequestParam(name = "from", required = false) String from,
      @RequestParam(name = "to", required = false) String to) {
    return ResponseEntity.ok(surveyService.stats(ownerNo(authentication), svno, from, to));
  }
  
 /**
   * AI 요약 + 긍정/부정 점수 (0 ~ 10)
   * POST /shop_survey/{svno}/summary?from=&to=
   */
  @PostMapping("/{svno}/summary")
  public ResponseEntity<ShopSurveyAnswerDTO.Summary> summary(
      Authentication authentication,
      @PathVariable("svno") Long svno,
      @RequestParam(name = "from", required = false) String from,
      @RequestParam(name = "to", required = false) String to) {
    return ResponseEntity.ok(surveyService.summarize(ownerNo(authentication), svno, from, to));
  }

  // =====================================================================
  // [고객] 비로그인
  // =====================================================================

  /**
   * QR로 접속한 고객용 설문 조회 (OPEN만)
   * GET /shop_survey/public/{qrid}
   */
  @GetMapping("/public/{qrid}")
  public ResponseEntity<ShopSurveyDTO> getPublic(@PathVariable("qrid") String qrid) {
    return ResponseEntity.ok(surveyService.getPublic(qrid));
  }

  /**
   * 응답 제출 (multipart/form-data)
   * POST /shop_survey/public/{qrid}/submit
   *
   *   data       : JSON 문자열 { "answers": [ { "sqno": 1, "content": "...", "scale": 8.5, "sonos": [3, 4] } ] }
   *   file_{sqno}: 해당 문항 첨부 사진 (같은 이름으로 여러 장)
   */
  @PostMapping("/public/{qrid}/submit")
  public ResponseEntity<Map<String, Object>> submit(
      @PathVariable("qrid") String qrid,
      @RequestParam("data") String data,
      MultipartHttpServletRequest request) {

    ShopSurveyAnswerDTO.Submit submit;
    try {
      submit = objectMapper.readValue(data, ShopSurveyAnswerDTO.Submit.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("제출 데이터 형식이 올바르지 않습니다.");
    }

    // file_{sqno} 파트를 문항번호별로 묶기
    Map<Long, List<MultipartFile>> filesBySqno = new HashMap<>();
    for (Map.Entry<String, List<MultipartFile>> entry : request.getMultiFileMap().entrySet()) {
      String name = entry.getKey();
      if (!name.startsWith(FILE_PREFIX)) continue;

      Long sqno;
      try {
        sqno = Long.parseLong(name.substring(FILE_PREFIX.length()));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("사진 파트 이름이 올바르지 않습니다: " + name);
      }

      List<MultipartFile> files = new ArrayList<>();
      for (MultipartFile f : entry.getValue()) {
        if (f != null && !f.isEmpty()) files.add(f);
      }
      if (!files.isEmpty()) filesBySqno.put(sqno, files);
    }

    Long srno = surveyService.submit(qrid, submit, filesBySqno, request.getRemoteAddr());
    return ResponseEntity.ok(Map.of("success", true, "no", srno));
  }

  // =====================================================================
  // [내부]
  // =====================================================================

  /**
   * 로그인 회원번호 (점주는 MEMBER 테이블 소속이라 ROLE_MEMBER만 허용)
   * MEMBER/MANAGER 번호가 겹칠 수 있어서 role 확인이 필요합니다.
   */
  private Long ownerNo(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Long no)) {
      throw new SecurityException("로그인이 필요합니다.");
    }
    boolean isMember = authentication.getAuthorities().stream()
        .anyMatch(a -> "ROLE_MEMBER".equals(a.getAuthority()));
    if (!isMember) {
      throw new SecurityException("점주만 이용할 수 있습니다.");
    }
    return no;
  }
}
