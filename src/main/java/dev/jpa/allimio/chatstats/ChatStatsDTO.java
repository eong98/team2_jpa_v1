package dev.jpa.allimio.chatstats;

import java.util.List;

/* ---------------------------------------------------------------------
   /dbms/chatstats(챗봇 상담 만족도 통계) 응답 DTO 모음.
   paystats.PayStatsDTO와 같은 방식으로 record를 한 파일에 모았습니다.
--------------------------------------------------------------------- */
public class ChatStatsDTO {

  private ChatStatsDTO() {
  }

  /** 날짜(yyyy-MM-dd) 한 칸 — 종료된 상담 수, 만족/불만족 응답 수 */
  public record DailyPoint(String label, long closed, long satisfied, long dissatisfied) {
  }

  /** 항목별 건수 (불만족 사유 / 종료 사유) */
  public record GroupCount(String key, String label, long count) {
  }

  /** 항목별 만족/불만족 (회원·비회원) */
  public record GroupSatisfy(String key, String label, long closed, long satisfied, long dissatisfied) {
  }

  /** 최근 불만족 의견 1건 */
  public record RecentOpinion(String no, String closedat, String stitle, Integer sreason, String reasonLabel,
      String smemo, boolean member) {
  }

  /** 상단 KPI 카드 값 (현재 기간 + 직전 동일 기간 비교값) */
  public record Summary(
      long closed,            // 기간 내 종료된 상담
      long answered,          // 만족도 응답 수 (만족 + 불만족)
      long satisfied,         // 만족
      long dissatisfied,      // 불만족
      long answeredPrev,      // 직전 동일 기간 응답 수
      long satisfiedPrev,     // 직전 동일 기간 만족 수
      long userClosed,        // 사용자가 직접 종료 (CREASON=0)
      long adminConnect,      // 관리자 문의로 연결 (CREASON=1)
      long autoClosed,        // 30분 방치로 자동 종료 (CREASON=2)
      long inProgress         // 현재 진행 중인 상담 (기간 무관)
  ) {
  }

  /**
   * AI 감정분석 + 요약 결과 (관리자가 [AI 분석] 버튼을 눌렀을 때만 생성)
   * score: 0 = 매우 부정, 5 = 중립, 10 = 매우 긍정 — 매장 설문 AI 요약과 같은 기준
   */
  public record AiSummary(String summary, double score, String reason, List<String> weakPoints,
      long answered, String from, String to, String analyzedAt) {
  }

  /** AI 서버에 보낼 분석 자료 (DB 조회 결과) — AI 호출은 DB 연결을 놓은 뒤 컨트롤러에서 */
  public record AiRequest(java.util.Map<String, Object> payload, String from, String to, long answered) {
  }

  /** 만족도 통계 전체 응답 */
  public record Stats(
      int days,
      String from,
      String to,
      Summary summary,
      List<DailyPoint> daily,
      List<GroupCount> byReason,
      List<GroupCount> byCloseReason,
      List<GroupSatisfy> byUserType,
      List<RecentOpinion> recent) {
  }
}
