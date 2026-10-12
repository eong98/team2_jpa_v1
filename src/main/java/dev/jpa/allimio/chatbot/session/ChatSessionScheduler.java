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

   자동 종료 5분 전(IDLE_WARN_BEFORE_MINUTES)에는 경고 문구와 함께 만족도 질문을 먼저 남깁니다.
   챗봇 창이 열려 있으면 화면이 제때 요청하고(ChatRoom), 창을 닫아 둔 상담은 이 배치가 남기면서
   안 읽음으로 표시 → 챗봇 버튼에 N 표시. 종료를 먼저 돌리고 경고를 남기므로, 배치가 경고한 상담은
   다음 실행(5분 뒤)에 종료됩니다. 그때까지 답이 없으면 만족도 없이(NULL) 종료.

   종료 후에는 사용자가 [상담 종료]를 눌렀을 때(React ChatRoom.markConsultEnded)와 같이
   FastAPI에 채팅 목록 제목 AI 요약을 요청합니다 (POST /api/chatbot/{sno}/summarize-title).
   FastAPI는 바로 응답하고 요약은 백그라운드로 처리 → 끝나면 웹소켓으로 목록 갱신 알림.

   ※ Team2Application의 @EnableScheduling이 있어야 실행됩니다.
--------------------------------------------------------------------- */
@Component
public class ChatSessionScheduler {

  @Autowired
  private ChatSessionService chatSessionService;

  /** FastAPI 호출에 붙일 서버 티켓 (비밀키가 없으면 붙이지 않음) */
  @Autowired
  private ChatTicketProvider chatTicketProvider;

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
    if (!nos.isEmpty()) {
      System.out.println("-> 챗봇 방치 세션 자동 종료: " + closed + "건");
    }
    warnIdleSessions();
  }

  /** 곧 자동 종료될 상담에 경고 + 만족도 질문을 남김 (화면이 이미 남긴 상담은 건너뜀) */
  private void warnIdleSessions() {
    for (String no : chatSessionService.findIdleWarnSessionNos()) {
      try {
        chatSessionService.idleWarn(no, null, true);
      } catch (Exception e) {
        System.out.println("⚠ 챗봇 자동 종료 경고 실패(no=" + no + "): " + e.getMessage());
      }
    }
  }

  /** 채팅 목록 제목 AI 요약 요청 — 실패해도(AI 서버 꺼짐 등) 종료 처리에는 영향 없음 */
  private void requestTitleSummary(String no) {
    if (aiServerUrl.isBlank()) {
      return;
    }
    try {
      String ticket = chatTicketProvider.issueForServer();
      restClient.post()
          .uri(aiServerUrl + "/api/chatbot/{sno}/summarize-title", no)
          .headers(h -> { if (ticket != null) h.set(ChatTicketProvider.HEADER, ticket); })
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      System.out.println("⚠ 자동 종료 상담 제목 요약 요청 실패(no=" + no + "): " + e.getMessage());
    }
  }
}
