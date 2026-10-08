package dev.jpa.allimio.qa;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import dev.jpa.allimio.tool.Tool;
import jakarta.annotation.PreDestroy;

/* ---------------------------------------------------------------------
   1:1 문의 AI 자동 답변

   문의가 등록(커밋)되면 백그라운드에서 FastAPI(POST /api/qa/ai-answer)에 제목·내용을 보내
   챗봇 AI 상담과 같은 매뉴얼 검색·답변 생성으로 답을 받습니다.
   - 매뉴얼로 답할 수 있으면(answered=true) QA.AI_ANSWER / AI_ADATE에만 저장
     관리자 답변(ANSWER/ANO/ADATE)과 답변 상태(STATUS)는 건드리지 않음 → 관리자는 지금처럼 따로 답변
   - 모르는 내용이면 아무것도 하지 않음 → 지금처럼 관리자 답변 대기
   - 그 사이 관리자가 먼저 답변했거나 글이 삭제됐으면 저장하지 않음
   - AI 서버가 꺼져 있거나 오류가 나도 문의 등록에는 영향 없음 (로그만 남김)

   LLM 응답이 수십 초 걸려서 문의 등록 요청을 기다리게 하지 않도록 별도 스레드에서 처리하고,
   AI 서버에 한꺼번에 몰리지 않게 한 번에 하나씩 순서대로 처리합니다.
--------------------------------------------------------------------- */
@Service
public class QaAiAnswerService {

  private final QaRepository qaRepository;
  private final TransactionTemplate transactionTemplate;

  /** FastAPI 주소 (application.properties의 ai.server.url) — 없으면 자동 답변 생략 */
  private final String aiServerUrl;
  private final RestClient restClient;

  /** 한 번에 하나씩 처리 (LLM 서버 과부하 방지) */
  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
    Thread t = new Thread(r, "qa-ai-answer");
    t.setDaemon(true);
    return t;
  });

  public QaAiAnswerService(QaRepository qaRepository, PlatformTransactionManager transactionManager,
      @Value("${ai.server.url:}") String aiServerUrl) {
    this.qaRepository = qaRepository;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.aiServerUrl = aiServerUrl == null ? "" : aiServerUrl.replaceAll("/+$", "");

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);   // AI 서버가 꺼져 있으면 빨리 포기
    factory.setReadTimeout(180_000);   // 매뉴얼 검색 + LLM 답변 생성 (CPU 환경은 1분 이상 걸리기도 함)
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  /**
   * 문의 등록 트랜잭션이 커밋된 뒤 자동 답변 요청 (커밋 전에 보내면 다른 스레드에서 글을 못 찾음)
   */
  public void requestAfterCommit(Long qno) {
    if (qno == null || aiServerUrl.isBlank()) {
      return;
    }
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          executor.submit(() -> answer(qno));
        }
      });
    } else {
      executor.submit(() -> answer(qno));
    }
  }

  /** FastAPI에 답변 요청 → 답할 수 있을 때만 저장 */
  private void answer(Long qno) {
    try {
      Qa qa = qaRepository.findById(qno).orElse(null);
      if (!isWaiting(qa)) {
        return;
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> res = restClient.post()
          .uri(aiServerUrl + "/api/qa/ai-answer")
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("title", nvl(qa.getTitle()), "content", nvl(qa.getContent())))
          .retrieve()
          .body(Map.class);

      boolean answered = res != null && Boolean.TRUE.equals(res.get("answered"));
      String answer = res == null ? "" : nvl((String) res.get("answer")).trim();
      if (!answered || answer.isEmpty()) {
        System.out.println("-> QA AI 자동 답변 없음(no=" + qno + ") — 관리자 답변 대기");
        return;
      }

      // 저장 직전에 다시 확인 — AI가 답을 만드는 동안 관리자가 답변했거나 글이 삭제됐을 수 있음
      Boolean saved = transactionTemplate.execute(status -> {
        Qa current = qaRepository.findById(qno).orElse(null);
        if (!isWaiting(current)) {
          return false;
        }
        current.updateAiAnswer(answer, Tool.getDate()); // 관리자 답변·상태와 별도 컬럼
        return true;
      });
      System.out.println("-> QA AI 자동 답변 " + (Boolean.TRUE.equals(saved) ? "등록" : "생략(이미 답변됨/삭제됨)") + "(no=" + qno + ")");
    } catch (Exception e) {
      System.out.println("⚠ QA AI 자동 답변 실패(no=" + qno + "): " + e.getMessage());
    }
  }

  /** 관리자 답변·AI 답변이 아직 없는 살아 있는 1:1 문의인지 */
  private boolean isWaiting(Qa qa) {
    return qa != null
        && "N".equals(qa.getIsdel())
        && "N".equals(qa.getIsfaq())
        && qa.getStatus() != 2
        && (qa.getAnswer() == null || qa.getAnswer().isBlank())
        && (qa.getAiAnswer() == null || qa.getAiAnswer().isBlank());
  }

  private String nvl(String s) {
    return s == null ? "" : s;
  }

  @PreDestroy
  public void shutdown() {
    executor.shutdownNow();
  }
}
