package dev.jpa.allimio.chatstats;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import dev.jpa.allimio.chatbot.session.ChatTicketProvider;
import dev.jpa.allimio.chatstats.ChatStatsDTO.AiSummary;
import lombok.extern.slf4j.Slf4j;

/**
 * 챗봇 상담 만족도 AI 분석(FastAPI) 호출 클라이언트 — ShopSurveyAiClient와 같은 방식
 *
 * POST {ai.server.url}/api/chat-stats/summary
 *   요청: Spring이 관리자 확인·기간 필터를 거쳐 모은 만족도 집계와 고객 의견
 *   응답: { summary, score(0~10), reason, weakPoints }
 *
 * FastAPI는 DB를 읽지 않고 받은 데이터로 LLM만 호출합니다.
 * H200 모델을 여러 팀이 같이 쓰기 때문에 읽기 타임아웃을 넉넉히 둡니다.
 */
@Slf4j
@Component
public class ChatStatsAiClient {

  private final String aiServerUrl;
  private final RestClient restClient;
  private final ChatTicketProvider chatTicketProvider;

  public ChatStatsAiClient(@Value("${ai.server.url:}") String aiServerUrl, ChatTicketProvider chatTicketProvider) {
    this.aiServerUrl = aiServerUrl == null ? "" : aiServerUrl.replaceAll("/+$", "");
    this.chatTicketProvider = chatTicketProvider;

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(180));
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  /**
   * 만족도 요약 + 긍정/부정 점수 요청 (LLM 1회 호출)
   * @throws IllegalStateException 관리자에게 그대로 보여줄 안내 문구
   */
  @SuppressWarnings("unchecked")
  public AiSummary summarize(Map<String, Object> payload, String from, String to, long answered, String analyzedAt) {
    if (aiServerUrl.isBlank()) {
      throw new IllegalStateException("AI 서버 주소가 설정되지 않았습니다.");
    }
    try {
      String ticket = chatTicketProvider.issueForServer(); // 티켓 검사가 켜져 있으면 서버 티켓 필요
      Map<String, Object> res = restClient.post()
          .uri(aiServerUrl + "/api/chat-stats/summary")
          .contentType(MediaType.APPLICATION_JSON)
          .headers(h -> { if (ticket != null) h.set(ChatTicketProvider.HEADER, ticket); })
          .body(payload)
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {});

      if (res == null || res.get("summary") == null || res.get("score") == null) {
        throw new IllegalStateException("AI 분석 결과가 비어 있습니다. 다시 시도해 주세요.");
      }
      List<String> weakPoints = res.get("weakPoints") instanceof List<?> list
          ? list.stream().map(String::valueOf).toList() : List.of();
      return new AiSummary(
          String.valueOf(res.get("summary")),
          ((Number) res.get("score")).doubleValue(),
          res.get("reason") == null ? "" : String.valueOf(res.get("reason")),
          weakPoints, answered, from, to, analyzedAt);

    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new IllegalStateException("AI 서버에 만족도 분석 기능이 아직 없습니다. AI 서버를 최신 코드로 올려 주세요.");
      }
      log.warn("챗봇 만족도 AI 분석 호출 거절: {}", e.getStatusCode());
      throw new IllegalStateException("AI 분석 요청이 거절되었습니다. (" + e.getStatusCode().value() + ")");
    } catch (RestClientException e) {
      log.warn("챗봇 만족도 AI 분석 호출 실패", e);
      throw new IllegalStateException("AI 서버 응답이 없습니다. 잠시 후 다시 시도해 주세요.");
    }
  }
}
