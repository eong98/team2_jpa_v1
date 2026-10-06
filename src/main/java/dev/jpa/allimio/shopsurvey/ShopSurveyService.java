package dev.jpa.allimio.shopsurvey;

import dev.jpa.allimio.shopsurveyanswer.ShopSurveyAnswer;
import dev.jpa.allimio.shopsurveyanswer.ShopSurveyAnswerDTO;
import dev.jpa.allimio.shopsurveyanswer.ShopSurveyAnswerRepository;
import dev.jpa.allimio.shopsurveyansweroption.ShopSurveyAnswerOption;
import dev.jpa.allimio.shopsurveyansweroption.ShopSurveyAnswerOptionRepository;
import dev.jpa.allimio.shopsurveyoption.ShopSurveyOption;
import dev.jpa.allimio.shopsurveyoption.ShopSurveyOptionRepository;
import dev.jpa.allimio.shopsurveyquestion.ShopSurveyQuestion;
import dev.jpa.allimio.shopsurveyquestion.ShopSurveyQuestionRepository;
import dev.jpa.allimio.shopsurveyresponse.ShopSurveyResponse;
import dev.jpa.allimio.shopsurveyresponse.ShopSurveyResponseRepository;
import dev.jpa.allimio.shopsurveysummary.ShopSurveySummary;
import dev.jpa.allimio.shopsurveysummary.ShopSurveySummaryRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.allimio.attach.Attach;
import dev.jpa.allimio.attach.AttachDTO;
import dev.jpa.allimio.attach.AttachService;
import dev.jpa.allimio.shop.Shop;
import dev.jpa.allimio.tool.PageResponse;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 매장 설문조사 Service
 *
 * [점주]  임시저장 / 게시 / 수정 / 상태변경 / 삭제 / 목록 / 상세 / 응답목록 / 집계
 * [고객]  QR 토큰으로 설문 조회 / 응답 제출 (사진 첨부 포함)
 *
 * 상태 흐름: DRAFT(임시저장) → OPEN(진행중) ↔ CLOSED(종료) → DELETE(삭제, 응답이 있을 때만)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShopSurveyService {

  private final ShopSurveyRepository surveyRepository;
  private final ShopSurveyQuestionRepository questionRepository;
  private final ShopSurveyOptionRepository optionRepository;
  private final ShopSurveyResponseRepository responseRepository;
  private final ShopSurveyAnswerRepository answerRepository;
  private final ShopSurveyAnswerOptionRepository answerOptionRepository;
  private final AttachService attachService;
  private final ObjectMapper objectMapper;
  private final ShopSurveyAiClient aiClient;
  private final ShopSurveySummaryRepository summaryRepository;

  /** ATTACH.TNAME (관리자메뉴/매장메뉴에 같은 TNAME으로 등록되어 있어야 TNO가 채워짐) */
  public static final String ATTACH_TNAME = "SHOP_SURVEY_ANSWER";

  /** 답변타입 */
  private static final Set<String> ATYPES = Set.of("SHORT", "LONG", "SINGLE", "MULTI", "SCALE");

  /** 문항당 최대 첨부 사진 수 (고정) */
  private static final int MAX_FILES = 10;

  /** 점수 최대값 (고정) */
  private static final BigDecimal SCALE_MAX = BigDecimal.TEN;

  /** 도배 방지: 같은 IP가 WINDOW_MINUTES 안에 RATE_LIMIT회까지 제출 가능 */
  private static final int RATE_LIMIT = 5;
  private static final int WINDOW_MINUTES = 10;

  /** 컬럼 크기 (VARCHAR2 바이트 기준) */
  private static final int TITLE_BYTES = 500;
  private static final int DESCRIPTION_BYTES = 1000;
  private static final int LABEL_BYTES = 300;
  private static final int CONTENT_BYTES = 3000;
  
  /** AI 요약에 넘길 최대 응답 수 / 서술형 답변 수 (최신순) - 프롬프트 길이 제한 */
  private static final int SUMMARY_MAX_RESPONSES = 300;
  private static final int SUMMARY_MAX_TEXTS = 200;
  private static final int SUMMARY_TEXT_LENGTH = 300;

  /** 임시저장 제목이 비었을 때 기본값 (TITLE NOT NULL) */
  private static final String DEFAULT_TITLE = "제목 없음";

  // =====================================================================
  // [점주] 목록 / 상세
  // =====================================================================

  /**
   * 매장별 설문 목록 (삭제 제외)
   */
  public PageResponse<ShopSurveyListDTO> list(Long mno, Long sno, String status, int page, int size) {
    getOwnedShop(sno, mno);
    Page<ShopSurveyListDTO> result =
        surveyRepository.findListBySno(sno, mno, status, PageRequest.of(page, size));
    return PageResponse.of(result);
  }

  /**
   * 설문 상세 (수정 폼에 채울 데이터)
   * DRAFT는 임시저장 JSON을, 그 외는 문항/보기 테이블을 읽어 같은 형식으로 반환합니다.
   */
  public ShopSurveyDTO detail(Long mno, Long svno) {
    ShopSurvey survey = getOwnedSurvey(svno, mno);

    ShopSurveyDTO dto;
    if ("DRAFT".equals(survey.getStatus()) && survey.getDraft() != null) {
      dto = readDraft(survey.getDraft());
      String draftTitle = dto.getTitle(); // 사용자가 비워둔 제목은 '제목 없음' 대신 빈 값으로 돌려줌
      if (dto.getQuestions() == null) dto.setQuestions(new ArrayList<>());
      fillMeta(dto, survey);
      dto.setTitle(draftTitle);
    } else {
      dto = new ShopSurveyDTO();
      dto.setQuestions(loadQuestions(svno));
      fillMeta(dto, survey);
    }

    dto.setResponseCount(responseRepository.countBySvno(svno));
    return dto;
  }

  // =====================================================================
  // [점주] 임시저장 / 게시 / 수정
  // =====================================================================

  /**
   * 임시저장 (신규 또는 기존 DRAFT 덮어쓰기)
   * 문항은 테이블에 넣지 않고 폼 JSON 그대로 DRAFT 컬럼에 저장합니다.
   *
   * @return 설문번호
   */
  @Transactional
  public Long saveDraft(Long mno, ShopSurveyDTO form) {
    String now = Tool.getDate();
    ShopSurvey survey;

    if (form.getNo() != null) {
      survey = getOwnedSurvey(form.getNo(), mno);
      if (!"DRAFT".equals(survey.getStatus())) {
        throw new IllegalStateException("게시된 설문은 임시저장할 수 없습니다.");
      }
      survey.setUdate(now);
    } else {
      Shop shop = getOwnedShop(form.getSno(), mno);
      survey = ShopSurvey.builder()
          .shop(shop)
          .status("DRAFT")
          .qrid(createQrid())
          .cdate(now)
          .build();
    }

    String title = isBlank(form.getTitle()) ? DEFAULT_TITLE : form.getTitle().trim();
    checkBytes(title, TITLE_BYTES, "설문제목");
    checkBytes(form.getDescription(), DESCRIPTION_BYTES, "설문설명");

    survey.setTitle(title);
    survey.setDescription(form.getDescription());
    survey.setDraft(writeDraft(form));
    survey.setAiyn(mergeAiyn(survey.getAiyn(), form.getAiyn()));

    return surveyRepository.save(survey).getNo();
  }

  /**
   * 게시 (신규 바로 게시 또는 DRAFT → OPEN)
   * 폼을 검증한 뒤 문항/보기를 테이블에 저장하고 DRAFT JSON은 비웁니다.
   *
   * @return 게시된 설문 (no, qrid 사용)
   */
  @Transactional
  public ShopSurvey publish(Long mno, ShopSurveyDTO form) {
    validateForm(form);
    String now = Tool.getDate();
    ShopSurvey survey;

    if (form.getNo() != null) {
      survey = getOwnedSurvey(form.getNo(), mno);
      if (!"DRAFT".equals(survey.getStatus())) {
        throw new IllegalStateException("이미 게시된 설문입니다. 수정 기능을 이용해주세요.");
      }
      survey.setUdate(now);
    } else {
      Shop shop = getOwnedShop(form.getSno(), mno);
      survey = ShopSurvey.builder()
          .shop(shop)
          .qrid(createQrid())
          .cdate(now)
          .build();
    }

    survey.setTitle(form.getTitle().trim());
    survey.setDescription(form.getDescription());
    survey.setAiyn(mergeAiyn(survey.getAiyn(), form.getAiyn()));
    survey.setStatus("OPEN");
    survey.setDraft(null);
    surveyRepository.save(survey);

    insertQuestions(survey, form.getQuestions());
    return survey;
  }

  /**
   * 게시된 설문 수정 (응답 0건일 때만)
   * 기존 문항/보기를 지우고 폼 내용으로 다시 저장합니다.
   */
  @Transactional
  public void update(Long mno, Long svno, ShopSurveyDTO form) {
    ShopSurvey survey = getOwnedSurvey(svno, mno);

    if ("DRAFT".equals(survey.getStatus())) {
      throw new IllegalStateException("작성중인 설문은 임시저장 또는 게시를 이용해주세요.");
    }
    if (responseRepository.countBySvno(svno) > 0) {
      throw new IllegalStateException("응답이 있는 설문은 수정할 수 없습니다. 복사해서 새로 만들어주세요.");
    }
    validateForm(form);

    // 벌크 삭제가 영속성 컨텍스트를 비우므로(clearAutomatically) survey는 이후 merge된 객체를 사용
    optionRepository.deleteBySvno(svno);
    questionRepository.deleteBySvno(svno);

    survey.setTitle(form.getTitle().trim());
    survey.setDescription(form.getDescription());
    survey.setAiyn(mergeAiyn(survey.getAiyn(), form.getAiyn()));
    survey.setUdate(Tool.getDate());
    ShopSurvey saved = surveyRepository.save(survey);

    insertQuestions(saved, form.getQuestions());
  }

  // =====================================================================
  // [점주] 상태 변경 / 삭제
  // =====================================================================

  /**
   * 상태 변경 (OPEN ↔ CLOSED)
   */
  @Transactional
  public void changeStatus(Long mno, Long svno, String status) {
    ShopSurvey survey = getOwnedSurvey(svno, mno);
    String current = survey.getStatus();

    boolean allowed = ("OPEN".equals(current) && "CLOSED".equals(status))
        || ("CLOSED".equals(current) && "OPEN".equals(status));
    if (!allowed) {
      throw new IllegalStateException(current + " 상태에서 " + status + "(으)로 변경할 수 없습니다.");
    }

    survey.setStatus(status);
    survey.setUdate(Tool.getDate());
  }

  /**
   * 삭제
   * - 응답 0건: 보기 → 문항 → 설문 순서로 실제 삭제
   * - 응답 있음: STATUS = DELETE (응답 데이터 보존)
   *
   * @return true: 실제 삭제, false: DELETE 상태로 숨김
   */
  @Transactional
  public boolean delete(Long mno, Long svno) {
    ShopSurvey survey = getOwnedSurvey(svno, mno);

    if (responseRepository.countBySvno(svno) == 0) {
      optionRepository.deleteBySvno(svno);
      questionRepository.deleteBySvno(svno);
      surveyRepository.deleteById(svno);
      return true;
    }

    survey.setStatus("DELETE");
    survey.setUdate(Tool.getDate());
    return false;
  }

  // =====================================================================
  // [점주] 응답 목록 / 집계
  // =====================================================================

  /**
   * 응답 목록 (최신순, 응답별 문항 답 + 선택 보기 + 첨부 사진 포함)
   * 현재 페이지 응답 번호로 답/보기/사진을 각각 한 번씩만 조회합니다.
   */
  public PageResponse<ShopSurveyAnswerDTO.Response> responses(Long mno, Long svno, String fromDate, String toDate,int page, int size) {
    getOwnedSurvey(svno, mno);
    
    String from = startOfDay(fromDate);
    String to = endOfDay(toDate);

    Page<ShopSurveyResponse> responsePage =
        responseRepository.findPageBySvno(svno, from, to, PageRequest.of(page, size));
    List<Long> srnos = responsePage.getContent().stream().map(ShopSurveyResponse::getNo).toList();

    // 응답번호 → 응답 DTO
    Map<Long, ShopSurveyAnswerDTO.Response> responseMap = new LinkedHashMap<>();
    for (ShopSurveyResponse r : responsePage.getContent()) {
      responseMap.put(r.getNo(), ShopSurveyAnswerDTO.Response.builder()
          .no(r.getNo())
          .cdate(r.getCdate())
          .build());
    }

    if (!srnos.isEmpty()) {
      // 답변번호 → 답 DTO
      Map<Long, ShopSurveyAnswerDTO.Answer> answerMap = new HashMap<>();
      for (ShopSurveyAnswer a : answerRepository.findByResponseNos(srnos)) {
        ShopSurveyAnswerDTO.Answer dto = ShopSurveyAnswerDTO.Answer.builder()
            .no(a.getNo())
            .sqno(a.getQuestion().getNo())
            .questionTitle(a.getQuestion().getTitle())
            .atype(a.getQuestion().getAtype())
            .content(a.getContent())
            .scale(a.getScale())
            .build();
        answerMap.put(a.getNo(), dto);
        responseMap.get(a.getResponse().getNo()).getAnswers().add(dto);
      }

      // 선택한 보기 내용
      for (ShopSurveyAnswerOption ao : answerOptionRepository.findByResponseNos(srnos)) {
        ShopSurveyAnswerDTO.Answer dto = answerMap.get(ao.getId().getSano());
        if (dto != null) {
          dto.getOptions().add(ao.getOption().getLabel());
        }
      }

      // 첨부 사진
      for (Attach at : answerRepository.findAttachByResponseNos(srnos)) {
        ShopSurveyAnswerDTO.Answer dto = answerMap.get(at.getBno());
        if (dto != null) {
          dto.getFiles().add(AttachDTO.fromEntity(at));
        }
      }
    }

    return new PageResponse<>(
        new ArrayList<>(responseMap.values()),
        responsePage.getNumber(),
        responsePage.getSize(),
        responsePage.getTotalElements(),
        responsePage.getTotalPages());
  }

  /**
   * 문항별 집계 (답 수, 점수 평균, 보기별 선택 수)
   */
  public ShopSurveyAnswerDTO.Stats stats(Long mno, Long svno, String fromDate, String toDate) {
    getOwnedSurvey(svno, mno);
    
    String from = startOfDay(fromDate);
    String to = endOfDay(toDate);

    // 문항번호 → [답 수, 평균]
    Map<Long, Object[]> answerStat = new HashMap<>();
    for (Object[] row : answerRepository.countAndAvgBySvno(svno, from, to)) {
      answerStat.put((Long) row[0], row);
    }

    // 보기번호 → 선택 수
    Map<Long, Long> optionCount = new HashMap<>();
    for (Object[] row : answerOptionRepository.countBySvno(svno, from, to)) {
      optionCount.put((Long) row[0], (Long) row[1]);
    }

    // 문항별 보기 목록
    Map<Long, List<ShopSurveyOption>> optionsByQuestion = optionRepository.findBySvno(svno).stream()
        .collect(Collectors.groupingBy(o -> o.getQuestion().getNo(), LinkedHashMap::new, Collectors.toList()));

    List<ShopSurveyAnswerDTO.QuestionStat> questionStats = new ArrayList<>();
    for (ShopSurveyQuestion q : questionRepository.findBySvno(svno)) {
      Object[] row = answerStat.get(q.getNo());
      Long count = row != null ? (Long) row[1] : 0L;
      Double avg = null;
      if ("SCALE".equals(q.getAtype()) && row != null && row[2] != null) {
        avg = Math.round(((Number) row[2]).doubleValue() * 10) / 10.0;
      }

      List<ShopSurveyAnswerDTO.OptionStat> optionStats = new ArrayList<>();
      for (ShopSurveyOption o : optionsByQuestion.getOrDefault(q.getNo(), List.of())) {
        optionStats.add(ShopSurveyAnswerDTO.OptionStat.builder()
            .sono(o.getNo())
            .label(o.getLabel())
            .count(optionCount.getOrDefault(o.getNo(), 0L))
            .build());
      }

      questionStats.add(ShopSurveyAnswerDTO.QuestionStat.builder()
          .sqno(q.getNo())
          .title(q.getTitle())
          .atype(q.getAtype())
          .answerCount(count)
          .scaleAvg(avg)
          .options(optionStats)
          .build());
    }

    return ShopSurveyAnswerDTO.Stats.builder()
        .svno(svno)
        .totalResponses(responseRepository.countBySvnoInRange(svno, from, to))
        .questions(questionStats)
        .build();
  }
  
  /**
   * AI 요약 (요약 + 긍정/부정 점수, LLM 1회 호출)
   *
   * 기간 내 응답을 모아 FastAPI로 보냅니다.
   * - 점수형/객관식: 문항별 집계(평균, 보기별 선택 수)만 전달 → 응답이 많아도 프롬프트가 짧음
   * - 서술형: 최신 응답부터 SUMMARY_MAX_TEXTS개까지 원문 전달 (길면 잘라서)
   *
   * LLM 응답을 기다리는 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션 밖에서 실행합니다.
   * 결과(요약, 점수, 약한 항목)는 SHOP_SURVEY_SUMMARY에 설문당 1개로 덮어써서 저장합니다.
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public ShopSurveyAnswerDTO.Summary summarize(Long mno, Long svno, String fromDate, String toDate) {
    ShopSurvey survey = getOwnedSurvey(svno, mno);
    String from = startOfDay(fromDate);
    String to = endOfDay(toDate);

    long total = responseRepository.countBySvnoInRange(svno, from, to);
    if (total == 0) {
      throw new IllegalStateException("요약할 응답이 없습니다.");
    }

    // 1. 문항별 집계 (기간 필터 적용된 stats 재사용)
    ShopSurveyAnswerDTO.Stats stats = stats(mno, svno, fromDate, toDate);
    List<Map<String, Object>> questionStats = new ArrayList<>();
    for (ShopSurveyAnswerDTO.QuestionStat q : stats.getQuestions()) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("question", q.getTitle());
      row.put("type", q.getAtype());
      row.put("answerCount", q.getAnswerCount());
      if ("SCALE".equals(q.getAtype())) {
        row.put("scaleAvg", q.getScaleAvg());
        row.put("scaleMax", SCALE_MAX.intValue());
      }
      if (!q.getOptions().isEmpty()) {
        List<Map<String, Object>> opts = new ArrayList<>();
        for (ShopSurveyAnswerDTO.OptionStat o : q.getOptions()) {
          opts.add(Map.of("label", o.getLabel(), "count", o.getCount()));
        }
        row.put("options", opts);
      }
      questionStats.add(row);
    }

    // 2. 서술형 답변 (최신 응답 SUMMARY_MAX_RESPONSES건 안에서)
    List<Long> srnos = responseRepository
        .findPageBySvno(svno, from, to, PageRequest.of(0, SUMMARY_MAX_RESPONSES))
        .getContent().stream().map(ShopSurveyResponse::getNo).toList();

    List<Map<String, Object>> texts = new ArrayList<>();
    for (ShopSurveyAnswer a : answerRepository.findByResponseNos(srnos)) {
      if (texts.size() >= SUMMARY_MAX_TEXTS) break;
      String content = a.getContent();
      if (content == null || content.isBlank()) continue;
      if (content.length() > SUMMARY_TEXT_LENGTH) content = content.substring(0, SUMMARY_TEXT_LENGTH) + "…";
      texts.add(Map.of("question", a.getQuestion().getTitle(), "answer", content));
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("shopTitle", survey.getShop().getTitle());
    payload.put("surveyTitle", survey.getTitle());
    payload.put("totalResponses", total);
    payload.put("questions", questionStats);
    payload.put("textAnswers", texts);

    ShopSurveyAnswerDTO.Summary result = aiClient.summarize(payload);
    result.setResponseCount(total);
    
 // 3. 저장 (같은 설문이면 덮어쓰기) - AI 자동작성 참고용
    saveSummary(svno, result);
    
    return result;
  }
  
  /**
   * AI 요약 결과 저장 (SVNO가 PK라 save()가 INSERT 또는 UPDATE)
   * 저장에 실패해도 점주에게 보여줄 요약은 이미 나왔으므로 화면 응답은 그대로 돌려줍니다.
   */
  private void saveSummary(Long svno, ShopSurveyAnswerDTO.Summary result) {
    try {
      List<String> weak = result.getWeakPoints() != null ? result.getWeakPoints() : List.of();
      summaryRepository.save(ShopSurveySummary.builder()
          .svno(svno)
          .summary(result.getSummary())
          .score(BigDecimal.valueOf(result.getScore()))
          .weakpoints(objectMapper.writeValueAsString(weak))
          .build());
    } catch (Exception e) {
      log.warn("매장 설문 AI 요약 저장 실패 svno={}", svno, e);
    }
  }
  
  //=====================================================================
  // [내부] 날짜 필터
  // =====================================================================

   /** 'yyyy-MM-dd' → 'yyyy-MM-dd 00:00:00' (빈 값이면 null = 제한 없음) */
   private String startOfDay(String date) {
     return isBlank(date) ? null : checkDate(date) + " 00:00:00";
   }
  
   /** 'yyyy-MM-dd' → 'yyyy-MM-dd 23:59:59' (빈 값이면 null = 제한 없음) */
   private String endOfDay(String date) {
     return isBlank(date) ? null : checkDate(date) + " 23:59:59";
   }
  
   private String checkDate(String date) {
     String d = date.trim();
     if (!d.matches("\\d{4}-\\d{2}-\\d{2}")) {
       throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");
     }
     return d;
   }
   
// =====================================================================
  // [점주] AI 설문 자동작성
  // =====================================================================

  /** 참고 설문 최대 개수 / 요청 문장 최대 길이 */
  private static final int AI_MAX_REFERENCES = 5;
  private static final int AI_MAX_REQUEST_LENGTH = 500;
  private static final Set<String> AI_MODES = Set.of("create", "revise", "trend");

  /**
   * AI 설문 자동작성 (FastAPI LangGraph 에이전트 중계)
   *
   * Spring은 권한만 확인하고 번호를 넘깁니다.
   * - 매장이 로그인 점주 소유인지
   * - 참고 설문(refSvnos)이 모두 이 점주의 같은 매장 설문인지
   * FastAPI는 확인된 설문번호로 문항/약한 항목을 DB에서 직접 읽습니다.
   *
   * LLM 응답을 기다리는 동안 DB 커넥션을 잡지 않도록 트랜잭션 밖에서 실행합니다.
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public Map<String, Object> generateWithAi(Long mno, ShopSurveyAiDTO.Request req) {
    if (req == null || req.getMode() == null || !AI_MODES.contains(req.getMode())) {
      throw new IllegalArgumentException("AI 요청 종류가 올바르지 않습니다.");
    }
    Shop shop = getOwnedShop(req.getSno(), mno);

    String mode = req.getMode();
    String request = req.getRequest() == null ? "" : req.getRequest().trim();
    if (!"trend".equals(mode) && request.isEmpty()) {
      throw new IllegalArgumentException("AI에게 요청할 내용을 입력해주세요.");
    }
    if (request.length() > AI_MAX_REQUEST_LENGTH) {
      throw new IllegalArgumentException("요청 내용은 " + AI_MAX_REQUEST_LENGTH + "자까지 입력할 수 있습니다.");
    }

    // 참고 설문: 중복 제거 후 최대 5개, 전부 이 점주의 같은 매장 설문인지 확인
    List<Long> refSvnos = new ArrayList<>();
    if ("create".equals(mode) && req.getRefSvnos() != null) {
      for (Long svno : new LinkedHashSet<>(req.getRefSvnos())) {
        if (svno == null) continue;
        ShopSurvey ref = getOwnedSurvey(svno, mno);
        if (ref.getShop().getNo() != shop.getNo()) { // Shop.no는 long(기본형)
          throw new SecurityException("다른 매장의 설문은 참고할 수 없습니다.");
        }
        refSvnos.add(svno);
        if (refSvnos.size() >= AI_MAX_REFERENCES) break;
      }
    }

    if (!"create".equals(mode)) {
      ShopSurveyDTO form = req.getCurrentForm();
      if (form == null || form.getQuestions() == null || form.getQuestions().isEmpty()) {
        throw new IllegalArgumentException("수정할 문항이 없습니다. 먼저 문항을 만들어주세요.");
      }
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("mode", mode);
    payload.put("request", request);
    payload.put("shopTitle", shop.getTitle());
    payload.put("industry", req.getIndustry());
    payload.put("refSvnos", refSvnos);
    payload.put("currentForm", "create".equals(mode) ? null : toAiForm(req.getCurrentForm()));

    return aiClient.generate(payload);
  }

  /** 폼 → AI 입력 형식 (번호/정렬순서 같은 화면용 값은 빼고 내용만) */
  private Map<String, Object> toAiForm(ShopSurveyDTO form) {
    List<Map<String, Object>> questions = new ArrayList<>();
    for (ShopSurveyDTO.Question q : form.getQuestions()) {
      if (q == null) continue;
      List<Map<String, Object>> options = new ArrayList<>();
      if (q.getOptions() != null && isChoice(q.getAtype())) {
        for (ShopSurveyDTO.Option o : q.getOptions()) {
          if (o != null && !isBlank(o.getLabel())) options.add(Map.of("label", o.getLabel().trim()));
        }
      }
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("title", q.getTitle() == null ? "" : q.getTitle().trim());
      row.put("atype", ATYPES.contains(q.getAtype()) ? q.getAtype() : "SHORT");
      row.put("requiredyn", isZeroOrOne(q.getRequiredyn()) ? q.getRequiredyn() : 0);
      row.put("fileyn", isZeroOrOne(q.getFileyn()) ? q.getFileyn() : 0);
      row.put("options", options);
      questions.add(row);
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("title", form.getTitle() == null ? "" : form.getTitle().trim());
    result.put("description", form.getDescription() == null ? "" : form.getDescription().trim());
    result.put("questions", questions);
    return result;
  }

  // =====================================================================
  // [고객] 설문 조회 / 제출 (비로그인)
  // =====================================================================

  /**
   * QR 토큰으로 설문 조회 (OPEN만)
   */
  public ShopSurveyDTO getPublic(String qrid) {
    ShopSurvey survey = getOpenSurvey(qrid);

    ShopSurveyDTO dto = new ShopSurveyDTO();
    dto.setQuestions(loadQuestions(survey.getNo()));
    fillMeta(dto, survey);
    dto.setShopTitle(survey.getShop().getTitle());
    dto.setResponseCount(null); // 고객에게는 응답 수 비공개
    return dto;
  }

  /**
   * 응답 제출
   * 검증을 모두 통과한 뒤 저장하고, 사진은 마지막에 ATTACH로 저장합니다.
   *
   * @param filesBySqno 문항번호 → 첨부 사진 목록
   * @param ip          접속 IP
   * @return 응답번호
   */
  @Transactional
  public Long submit(String qrid, ShopSurveyAnswerDTO.Submit submit,
                     Map<Long, List<MultipartFile>> filesBySqno, String ip) {
    ShopSurvey survey = getOpenSurvey(qrid);
    Long svno = survey.getNo();

    // 1. 도배 방지
    String from = Tool.getLocalDateTime(Tool.getDate()).minusMinutes(WINDOW_MINUTES)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    if (responseRepository.countRecentByIp(svno, ip, from) >= RATE_LIMIT) {
      throw new IllegalStateException("잠시 후 다시 제출해주세요.");
    }

    // 2. 설문 문항/보기 로딩
    List<ShopSurveyQuestion> questions = questionRepository.findBySvno(svno);
    Map<Long, ShopSurveyQuestion> questionMap = new LinkedHashMap<>();
    questions.forEach(q -> questionMap.put(q.getNo(), q));

    Map<Long, Map<Long, ShopSurveyOption>> optionsByQuestion = new HashMap<>();
    for (ShopSurveyOption o : optionRepository.findBySvno(svno)) {
      optionsByQuestion.computeIfAbsent(o.getQuestion().getNo(), k -> new HashMap<>()).put(o.getNo(), o);
    }

    // 3. 제출 답 정리 (다른 설문 문항 / 중복 문항 차단)
    Map<Long, ShopSurveyAnswerDTO.SubmitAnswer> answerBySqno = new HashMap<>();
    List<ShopSurveyAnswerDTO.SubmitAnswer> submitted =
        submit != null && submit.getAnswers() != null ? submit.getAnswers() : List.of();
    for (ShopSurveyAnswerDTO.SubmitAnswer a : submitted) {
      if (a == null || a.getSqno() == null || !questionMap.containsKey(a.getSqno())) {
        throw new IllegalArgumentException("설문에 없는 문항입니다.");
      }
      if (answerBySqno.put(a.getSqno(), a) != null) {
        throw new IllegalArgumentException("같은 문항에 답이 중복되었습니다.");
      }
    }
    Map<Long, List<MultipartFile>> files = filesBySqno != null ? filesBySqno : Map.of();
    for (Long sqno : files.keySet()) {
      if (!questionMap.containsKey(sqno)) {
        throw new IllegalArgumentException("설문에 없는 문항에 사진이 첨부되었습니다.");
      }
    }

    // 4. 문항별 검증
    for (ShopSurveyQuestion q : questions) {
      validateAnswer(q, answerBySqno.get(q.getNo()), files.getOrDefault(q.getNo(), List.of()),
          optionsByQuestion.getOrDefault(q.getNo(), Map.of()));
    }

    // 5. 저장: 응답 → 문항별 답 → 선택 보기
    ShopSurveyResponse response = responseRepository.save(ShopSurveyResponse.builder()
        .survey(survey)
        .ipAddr(ip)
        .cdate(Tool.getDate())
        .build());

    Map<Long, List<MultipartFile>> filesBySano = new LinkedHashMap<>();
    for (ShopSurveyQuestion q : questions) {
      ShopSurveyAnswerDTO.SubmitAnswer a = answerBySqno.get(q.getNo());
      List<MultipartFile> qFiles = files.getOrDefault(q.getNo(), List.of());
      if (!hasValue(q, a) && qFiles.isEmpty()) {
        continue; // 답하지 않은 선택 문항
      }

      ShopSurveyAnswer answer = ShopSurveyAnswer.builder()
          .response(response)
          .question(q)
          .build();
      if (a != null) {
        if ("SHORT".equals(q.getAtype()) || "LONG".equals(q.getAtype())) {
          answer.setContent(isBlank(a.getContent()) ? null : a.getContent().trim());
        } else if ("SCALE".equals(q.getAtype())) {
          answer.setScale(a.getScale());
        }
      }
      answerRepository.save(answer);

      if (a != null && ("SINGLE".equals(q.getAtype()) || "MULTI".equals(q.getAtype()))) {
        Map<Long, ShopSurveyOption> options = optionsByQuestion.get(q.getNo());
        for (Long sono : new HashSet<>(a.getSonos())) {
          answerOptionRepository.save(ShopSurveyAnswerOption.builder()
              .id(new ShopSurveyAnswerOption.Pk(answer.getNo(), sono))
              .answer(answer)
              .option(options.get(sono))
              .build());
        }
      }

      if (!qFiles.isEmpty()) {
        filesBySano.put(answer.getNo(), qFiles);
      }
    }

    // 6. 사진 저장 (ATTACH: TNAME = SHOP_SURVEY_ANSWER, BNO = 답변번호)
    filesBySano.forEach((sano, list) -> attachService.saveAttachFiles(ATTACH_TNAME, sano, list));

    return response.getNo();
  }

  // =====================================================================
  // [내부] 조회 / 권한
  // =====================================================================

  /** 로그인 점주 소유 매장 확인 */
  private Shop getOwnedShop(Long sno, Long mno) {
    if (sno == null) {
      throw new IllegalArgumentException("매장번호가 없습니다.");
    }
    return surveyRepository.findOwnedShop(sno, mno)
        .orElseThrow(() -> new SecurityException("본인 매장의 설문만 관리할 수 있습니다."));
  }

  /** 로그인 점주 소유 설문 확인 (삭제 제외) */
  private ShopSurvey getOwnedSurvey(Long svno, Long mno) {
    return surveyRepository.findOwned(svno, mno)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 접근할 수 없는 설문입니다."));
  }

  /** 고객 접속 가능한(OPEN) 설문 확인 */
  private ShopSurvey getOpenSurvey(String qrid) {
    ShopSurvey survey = surveyRepository.findByQridWithShop(qrid)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다."));
    if (!"OPEN".equals(survey.getStatus())) {
      throw new IllegalStateException("현재 응답을 받지 않는 설문입니다.");
    }
    return survey;
  }

  /** 문항 + 보기를 폼 형식으로 조회 (문항 1번, 보기 1번 조회 후 묶기) */
  private List<ShopSurveyDTO.Question> loadQuestions(Long svno) {
    Map<Long, ShopSurveyDTO.Question> map = new LinkedHashMap<>();
    for (ShopSurveyQuestion q : questionRepository.findBySvno(svno)) {
      map.put(q.getNo(), ShopSurveyDTO.Question.fromEntity(q));
    }
    for (ShopSurveyOption o : optionRepository.findBySvno(svno)) {
      ShopSurveyDTO.Question q = map.get(o.getQuestion().getNo());
      if (q != null) {
        q.getOptions().add(ShopSurveyDTO.Option.fromEntity(o));
      }
    }
    return new ArrayList<>(map.values());
  }

  /** 설문 기본 정보를 DTO에 채움 */
  private void fillMeta(ShopSurveyDTO dto, ShopSurvey survey) {
    dto.setNo(survey.getNo());
    dto.setSno(survey.getShop().getNo());
    dto.setTitle(survey.getTitle());
    dto.setDescription(survey.getDescription());
    dto.setStatus(survey.getStatus());
    dto.setQrid(survey.getQrid());
    dto.setCdate(survey.getCdate());
    dto.setUdate(survey.getUdate());
    dto.setAiyn(survey.getAiyn() != null ? survey.getAiyn() : 0);
    }
  
  /**
   * AI생성여부 합치기: 기존 값이나 이번 요청 중 하나라도 1이면 1 (한 번 AI가 관여하면 계속 1)
   */
  private int mergeAiyn(Integer current, Integer requested) {    boolean ai = (current != null && current == 1) || (requested != null && requested == 1);
    return ai ? 1 : 0;
  }

  /** 문항/보기 저장 (정렬순서는 배열 순서로 다시 매김) */
  private void insertQuestions(ShopSurvey survey, List<ShopSurveyDTO.Question> questions) {
    int qSort = 1;
    for (ShopSurveyDTO.Question qf : questions) {
      ShopSurveyQuestion q = questionRepository.save(ShopSurveyQuestion.builder()
          .survey(survey)
          .title(qf.getTitle().trim())
          .atype(qf.getAtype())
          .requiredyn(qf.getRequiredyn())
          .fileyn(qf.getFileyn())
          .sort(qSort++)
          .build());

      if (isChoice(qf.getAtype())) {
        int oSort = 1;
        for (ShopSurveyDTO.Option of : qf.getOptions()) {
          optionRepository.save(ShopSurveyOption.builder()
              .question(q)
              .label(of.getLabel().trim())
              .sort(oSort++)
              .build());
        }
      }
    }
  }

  /** 중복 없는 QR 토큰 생성 */
  private String createQrid() {
    for (int i = 0; i < 5; i++) {
      String qrid = UUID.randomUUID().toString();
      if (!surveyRepository.existsByQrid(qrid)) {
        return qrid;
      }
      log.warn("QR 토큰 중복 발생, 재시도합니다: {}", qrid);
    }
    throw new IllegalStateException("QR 토큰 생성 실패. 다시 시도해주세요.");
  }

  // =====================================================================
  // [내부] 검증
  // =====================================================================

  /**
   * 게시/수정용 폼 검증
   * 임시저장은 이 검증을 하지 않습니다.
   */
  private void validateForm(ShopSurveyDTO form) {
    if (form == null) {
      throw new IllegalArgumentException("설문 내용이 없습니다.");
    }
    if (isBlank(form.getTitle())) {
      throw new IllegalArgumentException("설문제목을 입력해주세요.");
    }
    checkBytes(form.getTitle().trim(), TITLE_BYTES, "설문제목");
    checkBytes(form.getDescription(), DESCRIPTION_BYTES, "설문설명");

    List<ShopSurveyDTO.Question> questions = form.getQuestions();
    if (questions == null || questions.isEmpty()) {
      throw new IllegalArgumentException("문항을 1개 이상 추가해주세요.");
    }
    if (questions.size() > 999) {
      throw new IllegalArgumentException("문항은 999개까지 추가할 수 있습니다.");
    }

    int idx = 1;
    for (ShopSurveyDTO.Question q : questions) {
      String label = idx++ + "번 문항";
      if (q == null || isBlank(q.getTitle())) {
        throw new IllegalArgumentException(label + "의 제목을 입력해주세요.");
      }
      checkBytes(q.getTitle().trim(), TITLE_BYTES, label + " 제목");

      if (q.getAtype() == null || !ATYPES.contains(q.getAtype())) {
        throw new IllegalArgumentException(label + "의 답변타입이 올바르지 않습니다.");
      }
      if (q.getRequiredyn() == null) q.setRequiredyn(0);
      if (q.getFileyn() == null) q.setFileyn(0);
      if (!isZeroOrOne(q.getRequiredyn()) || !isZeroOrOne(q.getFileyn())) {
        throw new IllegalArgumentException(label + "의 필수/첨부 여부는 0 또는 1이어야 합니다.");
      }

      if (isChoice(q.getAtype())) {
        List<ShopSurveyDTO.Option> options = q.getOptions();
        if (options == null || options.size() < 2) {
          throw new IllegalArgumentException(label + "에 보기를 2개 이상 추가해주세요.");
        }
        if (options.size() > 99) {
          throw new IllegalArgumentException(label + "의 보기는 99개까지 추가할 수 있습니다.");
        }
        for (ShopSurveyDTO.Option o : options) {
          if (o == null || isBlank(o.getLabel())) {
            throw new IllegalArgumentException(label + "에 비어 있는 보기가 있습니다.");
          }
          checkBytes(o.getLabel().trim(), LABEL_BYTES, label + " 보기");
        }
      }
    }
  }

  /**
   * 제출 답 검증 (문항 1개)
   */
  private void validateAnswer(ShopSurveyQuestion q, ShopSurveyAnswerDTO.SubmitAnswer a,
                              List<MultipartFile> files, Map<Long, ShopSurveyOption> options) {
    String label = "'" + q.getTitle() + "'";

    // 필수 문항
    if (q.getRequiredyn() == 1 && !hasValue(q, a)) {
      throw new IllegalArgumentException(label + " 문항은 필수입니다.");
    }

    if (a != null) {
      switch (q.getAtype()) {
        case "SHORT", "LONG" -> checkBytes(a.getContent(), CONTENT_BYTES, label + " 답변");
        case "SCALE" -> {
          BigDecimal scale = a.getScale();
          if (scale != null) {
            if (scale.compareTo(BigDecimal.ZERO) < 0 || scale.compareTo(SCALE_MAX) > 0) {
              throw new IllegalArgumentException(label + " 점수는 0 ~ 10 사이여야 합니다.");
            }
            if (scale.stripTrailingZeros().scale() > 1) {
              throw new IllegalArgumentException(label + " 점수는 소수 첫째 자리까지 가능합니다.");
            }
          }
        }
        case "SINGLE", "MULTI" -> {
          List<Long> sonos = a.getSonos() != null ? a.getSonos() : List.of();
          if ("SINGLE".equals(q.getAtype()) && sonos.size() > 1) {
            throw new IllegalArgumentException(label + " 문항은 하나만 선택할 수 있습니다.");
          }
          if (new HashSet<>(sonos).size() != sonos.size()) {
            throw new IllegalArgumentException(label + " 문항에 같은 보기가 중복 선택되었습니다.");
          }
          for (Long sono : sonos) {
            if (!options.containsKey(sono)) {
              throw new IllegalArgumentException(label + " 문항에 없는 보기입니다.");
            }
          }
        }
        default -> { }
      }
    }

    // 사진 첨부
    if (!files.isEmpty()) {
      if (q.getFileyn() != 1) {
        throw new IllegalArgumentException(label + " 문항은 사진을 첨부할 수 없습니다.");
      }
      if (files.size() > MAX_FILES) {
        throw new IllegalArgumentException(label + " 문항은 사진을 " + MAX_FILES + "장까지 첨부할 수 있습니다.");
      }
      for (MultipartFile f : files) {
        String contentType = f.getContentType();
        if (!Tool.isImage(f.getOriginalFilename()) || contentType == null || !contentType.startsWith("image/")) {
          throw new IllegalArgumentException(label + " 문항에는 이미지 파일만 첨부할 수 있습니다.");
        }
      }
    }
  }

  /** 답 값이 있는지 (사진 제외) */
  private boolean hasValue(ShopSurveyQuestion q, ShopSurveyAnswerDTO.SubmitAnswer a) {
    if (a == null) return false;
    return switch (q.getAtype()) {
      case "SHORT", "LONG" -> !isBlank(a.getContent());
      case "SCALE" -> a.getScale() != null;
      case "SINGLE", "MULTI" -> a.getSonos() != null && !a.getSonos().isEmpty();
      default -> false;
    };
  }

  private boolean isChoice(String atype) {
    return "SINGLE".equals(atype) || "MULTI".equals(atype);
  }

  private boolean isZeroOrOne(Integer v) {
    return v != null && (v == 0 || v == 1);
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  /** VARCHAR2 바이트 길이 검사 (한글 3바이트) */
  private void checkBytes(String value, int maxBytes, String name) {
    if (value != null && value.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
      throw new IllegalArgumentException(name + "이(가) 너무 깁니다. (최대 한글 약 " + (maxBytes / 3) + "자)");
    }
  }

  // =====================================================================
  // [내부] 임시저장 JSON
  // =====================================================================

  private String writeDraft(ShopSurveyDTO form) {
    try {
      // 메타 정보는 테이블 컬럼에 있으므로 폼 내용만 저장
      ShopSurveyDTO draft = ShopSurveyDTO.builder()
          .title(form.getTitle())
          .description(form.getDescription())
          .questions(form.getQuestions() != null ? form.getQuestions() : new ArrayList<>())
          .build();
      return objectMapper.writeValueAsString(draft);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("임시저장 데이터 변환에 실패했습니다.");
    }
  }

  private ShopSurveyDTO readDraft(String json) {
    try {
      return objectMapper.readValue(json, ShopSurveyDTO.class);
    } catch (JsonProcessingException e) {
      log.warn("임시저장 JSON 파싱 실패", e);
      return new ShopSurveyDTO();
    }
  }
}
