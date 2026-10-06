package dev.jpa.allimio.chatbot.session;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/* ---------------------------------------------------------------------
   방치된 챗봇 상담 자동 종료 배치.

   사용자가 상담 중에 창을 닫고 떠나면 세션이 계속 "상담 중"으로 남으므로,
   5분마다 마지막 활동(CHAT_SESSION.UDATE) 후 30분(ChatSessionService.SESSION_IDLE_MINUTES)이
   지난 열린 세션을 찾아 자동 종료합니다(종료 사유 CREASON=2, 안내 메시지 남김).
   UDATE는 Spring·FastAPI 모두 대화가 오갈 때마다 갱신합니다.

   ※ Team2Application의 @EnableScheduling이 있어야 실행됩니다.
--------------------------------------------------------------------- */
@Component
public class ChatSessionScheduler {

  @Autowired
  private ChatSessionService chatSessionService;

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
      }
    }
    System.out.println("-> 챗봇 방치 세션 자동 종료: " + closed + "건");
  }
}
