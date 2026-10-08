package dev.jpa.allimio.chatbot.session;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/* ---------------------------------------------------------------------
   방치된 챗봇 상담 자동 종료 배치.

   사용자가 상담 중에 창을 닫고 떠나면 세션이 계속 "상담 중"으로 남으므로,
   5분마다 마지막 활동(CHAT_SESSION.UDATE) 후 30분(ChatSessionService.SESSION_IDLE_MINUTES)이
   지난 열린 세션을 찾아 자동 종료합니다(종료 사유 CREASON=2, 안내 메시지 남김).
   UDATE는 Spring·FastAPI 모두 대화가 오갈 때마다 갱신합니다.

   종료 후에는 사용자가 [상담 종료]를 눌렀을 때(React ChatRoom.markConsultEnded)와 같이
   FastAPI에 채팅 목록 제목 AI 요약을 요청합니다 (POST /api/chatbot/{sno}/summarize-title).
   FastAPI는 바로 응답하고 요약은 백그라운드로 처리 → 끝나면 웹소켓으로 목록 갱신 알림.

   ※ Team2Application의 @EnableScheduling이 있어야 실행됩니다.
--------------------------------------------------------------------- */
@Component
public class ChatSessionScheduler {

  @Autowired
  private ChatSessionService chatSessionService;

  /** FastAPI 주소 (application.properties의 ai.server.url, 예: http://139.150.91.194:11200) — 없으면 요약 생략 */
  private final String aiServerUrl;
  private final RestClient restClient;

  public ChatSessionScheduler(@Value("${ai.server.url:}") String aiServerUrl) {
    this.aiServerUrl = aiServerUrl == null ? "" : aiServerUrl.replaceAll("/+$", "");

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000); // AI 서버가 꺼져 있어도 배치가 오래 멈추지 않게
    factory.setReadTimeout(5000);    // 요약 요청은 접수만 하고 바로 응답함
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  /** 서버 시작 1분 뒤부터, 이전 실행이 끝나고 5분마다 */
  @Scheduled(initialDelay = 60_000, fixedDelay = 5 * 60_000)
  public void closeIdleSessions() {
    List<String> nos = chatSessionService.findIdleSessionNos();
    if (nos.isEmpty()) {
      return;
    }
    int closed = 0;
    for (String no : nos) {
      try {
        chatSessionService.autoClose(no); // 세션마다 따로 커밋 — 하나가 실패해도 나머지는 진행
        closed++;
      } catch (Exception e) {
        System.out.println("⚠ 챗봇 세션 자동 종료 실패(no=" + no + "): " + e.getMessage());
        continue;
      }
      requestTitleSummary(no);
    }
    System.out.println("-> 챗봇 방치 세션 자동 종료: " + closed + "건");
  }

  /** 채팅 목록 제목 AI 요약 요청 — 실패해도(AI 서버 꺼짐 등) 종료 처리에는 영향 없음 */
  private void requestTitleSummary(String no) {
    if (aiServerUrl.isBlank()) {
      return;
    }
    try {
      restClient.post()
          .uri(aiServerUrl + "/api/chatbot/{sno}/summarize-title", no)
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      System.out.println("⚠ 자동 종료 상담 제목 요약 요청 실패(no=" + no + "): " + e.getMessage());
    }
  }
}
