package dev.jpa.allimio.chatstats;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.allimio.chatstats.ChatStatsDTO.AiRequest;
import dev.jpa.allimio.chatstats.ChatStatsDTO.AiSummary;
import dev.jpa.allimio.chatstats.ChatStatsDTO.DailyPoint;
import dev.jpa.allimio.chatstats.ChatStatsDTO.GroupCount;
import dev.jpa.allimio.chatstats.ChatStatsDTO.GroupSatisfy;
import dev.jpa.allimio.chatstats.ChatStatsDTO.RecentOpinion;
import dev.jpa.allimio.chatstats.ChatStatsDTO.Stats;
import dev.jpa.allimio.chatstats.ChatStatsDTO.Summary;

/* ---------------------------------------------------------------------
   /dbms/chatstats 챗봇 상담 만족도 통계 서비스.
   기간 계산 / 빈 날짜 0 채우기는 PayStatsService(결제 통계)와 같은 규칙입니다.
--------------------------------------------------------------------- */
@Service
@Transactional(readOnly = true) // 조회 전용
public class ChatStatsService {

  /** 허용 조회기간(일). 그 외 값이 오면 30일로 보정 */
  private static final List<Integer> ALLOWED_DAYS = List.of(7, 30, 90);

  /** 불만족 사유 이름 (CHAT_SESSION.SREASON) */
  private static final Map<Integer, String> SREASON_LABELS =
      Map.of(0, "답변이 부정확함", 1, "응답이 느림", 2, "원하는 답을 못 찾음", 9, "기타 (직접 입력)");

  /** 종료 사유 이름 (CHAT_SESSION.CREASON) */
  private static final Map<Integer, String> CREASON_LABELS =
      Map.of(0, "사용자가 종료", 1, "관리자 문의로 연결", 2, "자동 종료 (30분 방치)");

  /** 최근 불만족 의견 표시 개수 */
  private static final int RECENT_LIMIT = 10;

  /** AI 분석에 넘길 직접 입력 의견 / 고객 질문 최대 개수 */
  private static final int AI_MEMO_LIMIT = 30;
  private static final int AI_MESSAGE_LIMIT = 60;

  /** 고객 메시지 중 질문이 아닌 화면 버튼 문구 (분석에서 제외) */
  private static final Set<String> FLOW_LABELS = Set.of(
      "상담 종료", "만족", "불만족", "AI 상담", "다른 질문하기", "관리자에게 문의하기", "예", "아니오", "네", "사유 선택");

  @Autowired
  private ChatStatsRepository chatStatsRepository;

  @Autowired
  private ChatStatsAiRepository chatStatsAiRepository;

  @Autowired
  private ObjectMapper objectMapper;

  /** 조회기간 보정 — 7 | 30 | 90 외에는 30 */
  public int normalizeDays(int days) {
    return ALLOWED_DAYS.contains(days) ? days : 30;
  }

  /**
   * AI 분석 결과 저장 (CHAT_STATS_AI) — 기간(7/30/90일)마다 한 줄, 다시 분석하면 덮어씀
   * @param ano 분석을 실행한 관리자 번호
   */
  @Transactional
  public void saveAiSummary(int days, AiSummary result, Long ano) {
    String weakPoints;
    try {
      weakPoints = objectMapper.writeValueAsString(result.weakPoints() == null ? List.of() : result.weakPoints());
    } catch (JsonProcessingException e) {
      weakPoints = "[]";
    }
    String reason = result.reason() == null ? "" : result.reason();
    reason = reason.length() > 600 ? reason.substring(0, 600) : reason; // VARCHAR2(2000) — 한글 3바이트
    double score = Math.round(Math.max(0, Math.min(10, result.score())) * 10) / 10.0; // NUMBER(3,1)
    int period = normalizeDays(days);

    ChatStatsAi saved = chatStatsAiRepository.findFirstByDaysOrderByNoDesc(period).orElse(null);
    if (saved != null) {
      saved.overwrite(result.from(), result.to(), result.answered(), score, result.summary(), reason,
          weakPoints, ano, result.analyzedAt());
      return;
    }
    chatStatsAiRepository.save(ChatStatsAi.builder()
        .days(period)
        .fromDate(result.from())
        .toDate(result.to())
        .answered(result.answered())
        .score(score)
        .summary(result.summary())
        .reason(reason)
        .weakPoints(weakPoints)
        .ano(ano)
        .rdate(result.analyzedAt())
        .build());
  }

  /** 해당 기간의 저장된 AI 분석 결과 — 없으면 null */
  public AiSummary getLatestAiSummary(int days) {
    return chatStatsAiRepository.findFirstByDaysOrderByNoDesc(normalizeDays(days))
        .map(row -> {
          List<String> weakPoints;
          try {
            weakPoints = row.getWeakPoints() == null || row.getWeakPoints().isBlank()
                ? List.of()
                : objectMapper.readValue(row.getWeakPoints(), new TypeReference<List<String>>() {});
          } catch (JsonProcessingException e) {
            weakPoints = List.of();
          }
          return new AiSummary(row.getSummary(), row.getScore(), row.getReason() == null ? "" : row.getReason(),
              weakPoints, row.getAnswered(), row.getFromDate(), row.getToDate(), row.getRdate());
        })
        .orElse(null);
  }

  public Stats getStats(int days) {
    days = normalizeDays(days);

    // 기간: 오늘 포함 최근 N일 [from, to], 쿼리는 반열림 구간 [from, toNext)
    LocalDate today = LocalDate.now();
    LocalDate fromDate = today.minusDays(days - 1L);
    String from = fromDate.toString();
    String to = today.toString();
    String toNext = today.plusDays(1).toString();
    // 직전 동일 기간 (만족률 증감 비교용)
    String prevFrom = fromDate.minusDays(days).toString();

    // ---- KPI ----
    Object[] cur = chatStatsRepository.summary(from, toNext);
    Object[] prev = chatStatsRepository.summary(prevFrom, from);
    Summary summary = new Summary(
        num(cur[0]),
        num(cur[1]),
        num(cur[2]),
        num(cur[3]),
        num(prev[1]),
        num(prev[2]),
        num(cur[4]),
        num(cur[5]),
        num(cur[6]),
        chatStatsRepository.countInProgress());

    // ---- 일별 (빈 날은 0으로 채워서 x축이 끊기지 않게) ----
    Map<String, long[]> dailyMap = new HashMap<>();
    for (Object[] row : chatStatsRepository.daily(from, toNext)) {
      dailyMap.put((String) row[0], new long[] { num(row[1]), num(row[2]), num(row[3]) });
    }
    List<DailyPoint> daily = new ArrayList<>(days);
    for (int i = 0; i < days; i++) {
      String d = fromDate.plusDays(i).toString();
      long[] v = dailyMap.getOrDefault(d, new long[] { 0, 0, 0 });
      daily.add(new DailyPoint(d, v[0], v[1], v[2]));
    }

    // ---- 불만족 사유별 ----
    List<GroupCount> byReason = new ArrayList<>();
    for (Object[] row : chatStatsRepository.byReason(from, toNext)) {
      Integer code = row[0] == null ? null : (int) num(row[0]);
      byReason.add(new GroupCount(String.valueOf(code), reasonLabel(code), num(row[1])));
    }

    // ---- 종료 사유별 ----
    List<GroupCount> byCloseReason = new ArrayList<>();
    for (Object[] row : chatStatsRepository.byCloseReason(from, toNext)) {
      Integer code = row[0] == null ? null : (int) num(row[0]);
      String label = code == null ? "사유 없음" : CREASON_LABELS.getOrDefault(code, "기타(" + code + ")");
      byCloseReason.add(new GroupCount(String.valueOf(code), label, num(row[1])));
    }

    // ---- 회원 / 비회원 (둘 다 항상 표시) ----
    Map<Integer, long[]> typeMap = new HashMap<>();
    for (Object[] row : chatStatsRepository.byUserType(from, toNext)) {
      typeMap.put((int) num(row[0]), new long[] { num(row[1]), num(row[2]), num(row[3]) });
    }
    List<GroupSatisfy> byUserType = new ArrayList<>();
    long[] member = typeMap.getOrDefault(1, new long[] { 0, 0, 0 });
    long[] guest = typeMap.getOrDefault(0, new long[] { 0, 0, 0 });
    byUserType.add(new GroupSatisfy("member", "회원", member[0], member[1], member[2]));
    byUserType.add(new GroupSatisfy("guest", "비회원", guest[0], guest[1], guest[2]));

    // ---- 최근 불만족 의견 ----
    List<RecentOpinion> recent = new ArrayList<>();
    for (Object[] row : chatStatsRepository.recentDissatisfied(from, toNext, RECENT_LIMIT)) {
      Integer code = row[3] == null ? null : (int) num(row[3]);
      recent.add(new RecentOpinion(
          (String) row[0],
          (String) row[1],
          sessionTitle(str(row[2]), str(row[6])),
          code,
          reasonLabel(code),
          str(row[4]),
          row[5] != null));
    }

    return new Stats(days, from, to, summary, daily, byReason, byCloseReason, byUserType, recent);
  }

  /**
   * AI 감정분석 + 요약에 쓸 자료 모으기 (DB 조회만)
   * 만족/불만족 집계, 불만족 사유, 직접 쓴 의견, 불만족 상담의 고객 질문.
   * AI 호출(수 초~수 분)은 여기서 하지 않습니다 — 이 메서드의 트랜잭션이 끝나 DB 연결을 돌려준 뒤
   * 컨트롤러가 ChatStatsAiClient를 호출합니다. (AI 응답을 기다리는 동안 DB 연결을 붙잡지 않게)
   *
   * @throws IllegalStateException 관리자에게 그대로 보여줄 안내 문구 (만족도 응답 없음)
   */
  public AiRequest buildAiRequest(int days) {
    Stats stats = getStats(days);
    Summary s = stats.summary();
    if (s.answered() <= 0) {
      throw new IllegalStateException("기간 내 만족도 응답이 없어 분석할 수 없습니다.");
    }
    String toNext = LocalDate.parse(stats.to()).plusDays(1).toString();

    List<Map<String, Object>> reasons = new ArrayList<>();
    for (GroupCount g : stats.byReason()) {
      reasons.add(Map.of("label", g.label(), "count", g.count()));
    }

    List<String> opinions = new ArrayList<>();
    for (Object memo : chatStatsRepository.dissatisfiedMemos(stats.from(), toNext, AI_MEMO_LIMIT)) {
      String text = str(memo);
      if (text != null && !text.isBlank()) {
        opinions.add(text.trim());
      }
    }

    // 고객이 고른 메뉴·입력한 질문 — 버튼 문구, 불만족 사유 이름, 직접 쓴 의견(위에서 따로 보냄)은 제외
    List<String> questions = new ArrayList<>();
    for (Object message : chatStatsRepository.dissatisfiedUserMessages(stats.from(), toNext, AI_MESSAGE_LIMIT)) {
      String text = str(message);
      if (text == null || text.isBlank()) {
        continue;
      }
      text = text.trim();
      if (FLOW_LABELS.contains(text) || SREASON_LABELS.containsValue(text) || opinions.contains(text)
          || questions.contains(text)) {
        continue;
      }
      questions.add(text);
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("period", stats.from() + " ~ " + stats.to());
    payload.put("closed", s.closed());
    payload.put("answered", s.answered());
    payload.put("satisfied", s.satisfied());
    payload.put("dissatisfied", s.dissatisfied());
    payload.put("adminConnect", s.adminConnect());
    payload.put("autoClosed", s.autoClosed());
    payload.put("reasons", reasons);
    payload.put("opinions", opinions);
    payload.put("questions", questions);

    return new AiRequest(payload, stats.from(), stats.to(), s.answered());
  }

  /**
   * 상담 표시 이름 — 챗봇 상담 목록(ChatSessionService.toSummary)과 같은 순서
   * 1) AI가 요약한 상담 제목  2) 마지막으로 고른 메뉴 이름  3) "AI 상담"
   * (제목은 상담 종료 후 AI가 붙이므로, 요약 전이거나 AI 서버가 꺼져 있었으면 비어 있음)
   */
  private String sessionTitle(String stitle, String menuLabel) {
    if (stitle != null && !stitle.isBlank()) {
      return stitle;
    }
    if (menuLabel != null && !menuLabel.isBlank()) {
      return menuLabel;
    }
    return "AI 상담";
  }

  private String reasonLabel(Integer code) {
    return code == null ? "사유 미선택" : SREASON_LABELS.getOrDefault(code, "기타(" + code + ")");
  }

  /** Oracle NUMBER(BigDecimal 등) → long, null은 0 */
  private long num(Object value) {
    return value == null ? 0L : ((Number) value).longValue();
  }

  /** VARCHAR/CLOB → String (null 유지) */
  private String str(Object value) {
    return value == null ? null : value.toString();
  }
}
