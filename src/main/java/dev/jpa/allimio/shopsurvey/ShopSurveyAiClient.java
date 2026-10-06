package dev.jpa.allimio.shopsurvey;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.allimio.shopsurveyanswer.ShopSurveyAnswerDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * 매장 설문 AI(FastAPI) 호출 클라이언트
 *
 * POST {ai.server.url}/api/shop-survey/summary
 *   요청: Spring이 권한/기간 필터를 거쳐 모은 설문 응답 데이터
 *   응답: { summary, score, reason }
 *
 * FastAPI는 DB를 읽지 않고 받은 데이터로 LLM만 호출합니다.
 * H200 Gemma를 여러 팀이 같이 쓰기 때문에 읽기 타임아웃을 넉넉히 둡니다.
 */
@Slf4j
@Component
public class ShopSurveyAiClient {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public ShopSurveyAiClient(@Value("${ai.server.url}") String aiServerUrl, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(180));

    this.restClient = RestClient.builder()
        .baseUrl(aiServerUrl)
        .requestFactory(factory)
        .build();
  }

  /**
   * 설문 응답 요약 + 긍정/부정 점수 요청 (LLM 1회 호출)
   *
   * @param payload 설문 제목, 문항별 집계, 서술형 답변 목록
   */
  public ShopSurveyAnswerDTO.Summary summarize(Map<String, Object> payload) {
    try {
      ShopSurveyAnswerDTO.Summary result = restClient.post()
          .uri("/api/shop-survey/summary")
          .contentType(MediaType.APPLICATION_JSON)
          .body(payload)
          .retrieve()
          .body(ShopSurveyAnswerDTO.Summary.class);

      if (result == null || result.getSummary() == null || result.getScore() == null) {
        throw new IllegalStateException("AI 요약 결과가 비어 있습니다. 다시 시도해주세요.");
      }
      return result;
    } catch (RestClientException e) {
      log.warn("매장 설문 AI 요약 호출 실패", e);
      throw new IllegalStateException("AI 서버 응답이 없습니다. 잠시 후 다시 시도해주세요.");
    }
  }
  
  /**
   * 설문 AI 자동작성 (LangGraph 에이전트)
   * FastAPI 결과를 그대로 Map으로 돌려줍니다.
   * FastAPI가 400으로 거절하면 그 안내 문구(detail)를 점주에게 그대로 보여줍니다.
   */
  public Map<String, Object> generate(Map<String, Object> payload) {
    try {
      Map<String, Object> result = restClient.post()
          .uri("/api/shop-survey/generate")
          .contentType(MediaType.APPLICATION_JSON)
          .body(payload)
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {});

      if (result == null || result.get("form") == null) {
        throw new IllegalStateException("AI 결과가 비어 있습니다. 다시 시도해주세요.");
      }
      return result;
    } catch (HttpClientErrorException e) {
      throw new IllegalArgumentException(readDetail(e.getResponseBodyAsString(), "AI 요청이 올바르지 않습니다."));
    } catch (RestClientException e) {
      log.warn("매장 설문 AI 자동작성 호출 실패", e);
      throw new IllegalStateException("AI 서버 응답이 없습니다. 잠시 후 다시 시도해주세요.");
    }
  }

  /** FastAPI 오류 응답 { "detail": "..." }에서 안내 문구 꺼내기 */
  private String readDetail(String body, String fallback) {
    try {
      JsonNode detail = objectMapper.readTree(body).get("detail");
      return detail != null && detail.isTextual() ? detail.asText() : fallback;
    } catch (Exception e) {
      return fallback;
    }
  }
}