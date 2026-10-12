package dev.jpa.allimio.chatstats;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.chatstats.ChatStatsDTO.AiRequest;
import dev.jpa.allimio.chatstats.ChatStatsDTO.Stats;
import dev.jpa.allimio.tool.Tool;

/* ---------------------------------------------------------------------
   /dbms/chatstats(챗봇 상담 만족도 통계) API - 관리자 전용.

   /chat_stats/** 는 SecurityConfig의 anyRequest().authenticated()에 걸려 로그인이 필요하고,
   관리자(ROLE_MANAGER)인지는 로그인 토큰으로 여기서 확인합니다. (PayStatsCont와 같은 방식)

   GET  /chat_stats/stats?days=30       → 만족도 통계 (days: 7 | 30 | 90)
   POST /chat_stats/ai-summary?days=30  → AI 감정분석 + 요약 (관리자가 버튼을 눌렀을 때만, LLM 호출)
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/chat_stats")
public class ChatStatsCont {

  @Autowired
  private ChatStatsService chatStatsService;

  @Autowired
  private ChatStatsAiClient chatStatsAiClient;

  public ChatStatsCont() {
    System.out.println("-> ChatStatsCont created.");
  }

  /**
   * 챗봇 상담 만족도 통계
   * http://localhost:9102/chat_stats/stats?days=30
   * @param days 조회기간 7 | 30 | 90 (그 외 값은 30으로 보정)
   */
  @GetMapping(path = "/stats")
  public ResponseEntity<?> stats(
      Authentication authentication,
      @RequestParam(value = "days", defaultValue = "30") int days) {

    boolean isManager = authentication != null && authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_MANAGER"::equals);
    if (!isManager) {
      return ResponseEntity.status(403)
          .body(Map.of("success", false, "message", "관리자만 이용할 수 있습니다."));
    }

    Stats stats = chatStatsService.getStats(days);
    return ResponseEntity.ok(stats);
  }

  /**
   * AI 감정분석 + 요약 — 화면의 [AI 분석] 버튼
   * http://localhost:9102/chat_stats/ai-summary?days=30
   * 응답 { summary, score(0~10), reason, weakPoints, answered, from, to, analyzedAt }
   * 분석할 수 없으면(응답 없음·AI 서버 오류) 409 + {success:false, message}
   */
  @PostMapping(path = "/ai-summary")
  public ResponseEntity<?> aiSummary(
      Authentication authentication,
      @RequestParam(value = "days", defaultValue = "30") int days) {

    boolean isManager = authentication != null && authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_MANAGER"::equals);
    if (!isManager) {
      return ResponseEntity.status(403)
          .body(Map.of("success", false, "message", "관리자만 이용할 수 있습니다."));
    }

    try {
      // 1) 분석 자료는 DB에서 모으고(짧은 트랜잭션) 2) AI 호출은 DB 연결을 놓은 뒤에
      AiRequest request = chatStatsService.buildAiRequest(days);
      return ResponseEntity.ok(chatStatsAiClient.summarize(
          request.payload(), request.from(), request.to(), request.answered(), Tool.getDate()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(Map.of("success", false, "message", e.getMessage()));
    }
  }
}
