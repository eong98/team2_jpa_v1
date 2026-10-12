package dev.jpa.allimio.qa.faq;

import java.time.Duration;
import java.util.ArrayList;
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
import dev.jpa.allimio.qa.faq.QaFaqSuggestDTO.Candidate;
import dev.jpa.allimio.qa.faq.QaFaqSuggestDTO.Result;
import dev.jpa.allimio.qa.faq.QaFaqSuggestDTO.Source;
import lombok.extern.slf4j.Slf4j;

/**
 * FAQ AI 추천(FastAPI) 호출 클라이언트 — ChatStatsAiClient와 같은 방식
 *
 * POST {ai.server.url}/api/faq/suggest
 *   요청: { period, questions[{no,title,content,answer}], existing[{no,title,content}] }
 *   응답: { candidates[{title,content,answer,count,sourceNos,reason}], analyzed, groups, skipped }
 *
 * 묶음마다 LLM을 한 번씩 호출해서(최대 5번) 몇 분 걸릴 수 있어 읽기 타임아웃을 넉넉히 둡니다.
 */
@Slf4j
@Component
public class QaFaqSuggestClient {

  private final String aiServerUrl;
  private final RestClient restClient;
  private final ChatTicketProvider chatTicketProvider;

  public QaFaqSuggestClient(@Value("${ai.server.url:}") String aiServerUrl, ChatTicketProvider chatTicketProvider) {
    this.aiServerUrl = aiServerUrl == null ? "" : aiServerUrl.replaceAll("/+$", "");
    this.chatTicketProvider = chatTicketProvider;

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(420));
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  /**
   * @throws IllegalStateException 관리자에게 그대로 보여줄 안내 문구
   */
  @SuppressWarnings("unchecked")
  public Result suggest(QaFaqSuggestDTO.Request request) {
    if (aiServerUrl.isBlank()) {
      throw new IllegalStateException("AI 서버 주소가 설정되지 않았습니다.");
    }
    try {
      String ticket = chatTicketProvider.issueForServer(); // 티켓 검사가 켜져 있으면 서버 티켓 필요
      Map<String, Object> res = restClient.post()
          .uri(aiServerUrl + "/api/faq/suggest")
          .contentType(MediaType.APPLICATION_JSON)
          .headers(h -> { if (ticket != null) h.set(ChatTicketProvider.HEADER, ticket); })
          .body(request.payload())
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {});

      if (res == null) {
        throw new IllegalStateException("AI 추천 결과가 비어 있습니다. 다시 시도해 주세요.");
      }

      List<Candidate> candidates = new ArrayList<>();
      if (res.get("candidates") instanceof List<?> list) {
        for (Object item : list) {
          if (!(item instanceof Map<?, ?> raw)) {
            continue;
          }
          Map<String, Object> c = (Map<String, Object>) raw;
          List<Source> sources = new ArrayList<>();
          if (c.get("sourceNos") instanceof List<?> nos) {
            for (Object n : nos) {
              if (n instanceof Number number) {
                long no = number.longValue();
                sources.add(new Source(no, request.titles().getOrDefault(no, "")));
              }
            }
          }
          candidates.add(new Candidate(
              str(c.get("title")), str(c.get("content")), str(c.get("answer")),
              pickType(c.get("type"), sources, request.types()),
              c.get("count") instanceof Number count ? count.intValue() : sources.size(),
              sources, str(c.get("reason"))));
        }
      }
      return new Result(candidates, num(res.get("analyzed"), request.analyzed()), num(res.get("groups"), 0),
          num(res.get("skipped"), 0), request.from(), request.to());

    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new IllegalStateException("AI 서버에 FAQ 추천 기능이 아직 없습니다. AI 서버를 최신 코드로 올려 주세요.");
      }
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw new IllegalStateException("분석할 문의가 부족합니다. (관리자 답변이 달린 문의가 2건 이상 필요)");
      }
      log.warn("FAQ AI 추천 호출 거절: {}", e.getStatusCode());
      throw new IllegalStateException("AI 추천 요청이 거절되었습니다. (" + e.getStatusCode().value() + ")");
    } catch (RestClientException e) {
      log.warn("FAQ AI 추천 호출 실패", e);
      throw new IllegalStateException("AI 서버 응답이 없습니다. 잠시 후 다시 시도해 주세요.");
    }
  }

  /** QA.TYPE 범위 (0 기타 ~ 8 직원초대) */
  private static final int MAX_TYPE = 8;

  /**
   * 후보의 문의 유형 — AI가 내용으로 고른 값을 우선 사용.
   * AI가 고르지 못했거나 기타(0)면 근거가 된 문의들의 유형을 따름 (문의를 쓸 때 고객이 유형을 '기타'로 두는 경우가 많아 AI 판단을 먼저 봄)
   */
  private static int pickType(Object aiType, List<Source> sources, Map<Long, Integer> types) {
    if (aiType instanceof Number n && n.intValue() > 0 && n.intValue() <= MAX_TYPE) {
      return n.intValue();
    }
    return majorityType(sources, types);
  }

  /** 근거가 된 문의들의 유형 중 가장 많은 것 — 같은 수면 먼저 나온 것, 없으면 0(기타) */
  private static int majorityType(List<Source> sources, Map<Long, Integer> types) {
    Map<Integer, Integer> counts = new java.util.LinkedHashMap<>();
    for (Source s : sources) {
      Integer type = types.get(s.no());
      if (type != null) {
        counts.merge(type, 1, Integer::sum);
      }
    }
    int best = 0;
    int bestCount = 0;
    for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
      if (e.getValue() > bestCount) {
        best = e.getKey();
        bestCount = e.getValue();
      }
    }
    return best;
  }

  private static String str(Object o) {
    return o == null ? "" : String.valueOf(o).trim();
  }

  private static int num(Object o, int fallback) {
    return o instanceof Number n ? n.intValue() : fallback;
  }
}
