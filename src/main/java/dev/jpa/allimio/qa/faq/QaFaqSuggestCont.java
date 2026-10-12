package dev.jpa.allimio.qa.faq;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   FAQ AI 추천 API - 관리자 전용 (SecurityConfig에서 POST /qa/faq/suggest 는 ROLE_MANAGER만)

   POST /qa/faq/suggest?days=90  → FAQ 후보 목록 (관리자가 [AI 추천]을 눌렀을 때만, LLM 호출)
   분석할 수 없으면(문의 부족·AI 서버 오류) 409 + {success:false, message}

   후보를 만들기만 하고 저장·등록하지 않습니다. 등록은 관리자가 내용을 확인한 뒤 기존 FAQ 등록(POST /qa/faq)으로 합니다.
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/qa/faq")
@RequiredArgsConstructor
public class QaFaqSuggestCont {

  private final QaFaqSuggestService suggestService;
  private final QaFaqSuggestClient suggestClient;

  /**
   * FAQ AI 추천
   * http://localhost:9102/qa/faq/suggest?days=90
   * @param days 조회기간 30 | 90 | 180 | 365 (그 외 값은 90으로 보정)
   */
  @PostMapping("/suggest")
  public ResponseEntity<?> suggest(@RequestParam(value = "days", defaultValue = "90") int days) {
    try {
      // 1) 문의·기존 FAQ는 요청 스레드 밖에서 조회 — AI 응답을 몇 분 기다리는 동안 DB 연결을 잡고 있지 않게
      //    (요청 스레드에서 조회하면 응답을 보낼 때까지 연결을 놓지 않아, 연결 풀 5개가 금방 참)
      QaFaqSuggestDTO.Request request = runOffRequest(days);
      // 2) AI 호출 (DB 연결 없음)
      return ResponseEntity.ok(suggestClient.suggest(request));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  private QaFaqSuggestDTO.Request runOffRequest(int days) {
    try {
      return CompletableFuture.supplyAsync(() -> suggestService.buildRequest(days)).join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof RuntimeException cause) {
        throw cause; // IllegalStateException(안내 문구)는 그대로 전달
      }
      throw e;
    }
  }
}
