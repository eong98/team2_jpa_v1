package dev.jpa.allimio.paystats;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.paystats.PayStatsDTO.DailyPoint;
import dev.jpa.allimio.paystats.PayStatsDTO.GroupAmount;
import dev.jpa.allimio.paystats.PayStatsDTO.RecentPayment;
import dev.jpa.allimio.paystats.PayStatsDTO.Stats;
import dev.jpa.allimio.paystats.PayStatsDTO.Summary;

/* ---------------------------------------------------------------------
   /dbms/paystats 구독권 결제 통계 서비스.
   기간 계산 / 빈 날짜 0 채우기는 DashboardService(매장 통계)와 같은 규칙입니다.
--------------------------------------------------------------------- */
@Service
@Transactional(readOnly = true) // 조회 전용
public class PayStatsService {

  /** 허용 조회기간(일). 그 외 값이 오면 30일로 보정 */
  private static final List<Integer> ALLOWED_DAYS = List.of(7, 30, 90);

  /** 결제수단 이름 (SHOP_PAYMENT.PMETHOD, React ShopPayment.ts PMETHOD_MAP과 동일) */
  private static final Map<Integer, String> PMETHOD_LABELS = Map.of(0, "카드", 1, "계좌이체", 2, "토스페이");

  @Autowired
  private PayStatsRepository payStatsRepository;

  public Stats getStats(int days) {
    if (!ALLOWED_DAYS.contains(days)) {
      days = 30;
    }

    // 기간: 오늘 포함 최근 N일 [from, to], 쿼리는 반열림 구간 [from, toNext)
    LocalDate today = LocalDate.now();
    LocalDate fromDate = today.minusDays(days - 1L);
    String from = fromDate.toString();
    String to = today.toString();
    String toNext = today.plusDays(1).toString();

    // 직전 동일 기간 (증감률 비교용)
    String prevFrom = fromDate.minusDays(days).toString();

    // ---- KPI ----
    Object[] paid = payStatsRepository.paidSummary(from, toNext);
    Object[] paidPrev = payStatsRepository.paidSummary(prevFrom, from);
    Object[] refund = payStatsRepository.refundSummary(from, toNext);

    Map<Integer, Long> payStatus = toCountMap(payStatsRepository.countByStatus(from, toNext));
    Map<Integer, Long> orderStatus = toCountMap(payStatsRepository.orderByStatus());

    Summary summary = new Summary(
        num(paid[0]),
        num(paidPrev[0]),
        num(paid[1]),
        num(paidPrev[1]),
        payStatus.getOrDefault(1, 0L),
        payStatus.getOrDefault(2, 0L),
        num(refund[0]),
        num(refund[1]),
        payStatsRepository.countRefundPending(),
        orderStatus.getOrDefault(1, 0L),
        orderStatus.getOrDefault(0, 0L));

    // ---- 일별 (빈 날은 0으로 채워서 x축이 끊기지 않게) ----
    Map<String, long[]> dailyMap = new HashMap<>();
    for (Object[] row : payStatsRepository.daily(from, toNext)) {
      dailyMap.put((String) row[0], new long[] { num(row[1]), num(row[2]) });
    }
    List<DailyPoint> daily = new ArrayList<>(days);
    for (int i = 0; i < days; i++) {
      String d = fromDate.plusDays(i).toString();
      long[] v = dailyMap.getOrDefault(d, new long[] { 0, 0 });
      daily.add(new DailyPoint(d, v[0], v[1]));
    }

    // ---- 항목별 ----
    List<GroupAmount> byPlan = new ArrayList<>();
    for (Object[] row : payStatsRepository.byPlan(from, toNext)) {
      String pno = String.valueOf(num(row[0]));
      String pname = row[1] != null ? (String) row[1] : "삭제된 구독권 #" + pno;
      byPlan.add(new GroupAmount(pno, pname, num(row[2]), num(row[3])));
    }

    List<GroupAmount> byMethod = new ArrayList<>();
    for (Object[] row : payStatsRepository.byMethod(from, toNext)) {
      int method = (int) num(row[0]);
      byMethod.add(new GroupAmount(String.valueOf(method),
          PMETHOD_LABELS.getOrDefault(method, "기타(" + method + ")"), num(row[1]), num(row[2])));
    }

    List<GroupAmount> byMonth = new ArrayList<>();
    for (Object[] row : payStatsRepository.byMonth(from, toNext)) {
      long month = num(row[0]);
      byMonth.add(new GroupAmount(String.valueOf(month), month + "개월", num(row[1]), num(row[2])));
    }

    // ---- 최근 결제 ----
    List<RecentPayment> recent = new ArrayList<>();
    for (Object[] row : payStatsRepository.recent(8)) {
      recent.add(new RecentPayment(
          num(row[0]),
          (String) row[1],
          num(row[2]),
          (int) num(row[3]),
          (int) num(row[4]),
          (String) row[5],
          row[6] != null ? (int) num(row[6]) : null,
          row[7] != null ? (int) num(row[7]) : null,
          (String) row[8]));
    }

    return new Stats(days, from, to, summary, daily, byPlan, byMethod, byMonth, recent);
  }

  /** [상태값, 건수] 목록 → Map */
  private Map<Integer, Long> toCountMap(List<Object[]> rows) {
    Map<Integer, Long> map = new HashMap<>();
    for (Object[] row : rows) {
      if (row[0] != null) {
        map.put((int) num(row[0]), num(row[1]));
      }
    }
    return map;
  }

  /** Oracle NUMBER(BigDecimal 등) → long, null은 0 */
  private long num(Object value) {
    return value == null ? 0L : ((Number) value).longValue();
  }
}
