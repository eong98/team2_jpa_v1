package dev.jpa.allimio.paystats;

import java.util.List;

/* ---------------------------------------------------------------------
   /dbms/paystats(구독권 결제 통계) 응답 DTO 모음.
   dashboard.DashboardDTO와 같은 방식으로 record를 한 파일에 모았습니다.
--------------------------------------------------------------------- */
public class PayStatsDTO {

  private PayStatsDTO() {
  }

  /** 날짜(yyyy-MM-dd) 한 칸의 결제 금액 + 건수 */
  public record DailyPoint(String label, long amount, long count) {
  }

  /** 항목별(구독권 / 결제수단 / 구독기간) 결제 금액 + 건수 */
  public record GroupAmount(String key, String label, long amount, long count) {
  }

  /** 최근 결제 1건 */
  public record RecentPayment(long no, String cdate, long price, int pmethod, int pstatus,
      String pname, Integer pmonth, Integer ccnt, String mname) {
  }

  /** 상단 KPI 카드 값 (현재 기간 + 직전 동일 기간 비교값) */
  public record Summary(
      long payAmount,         // 기간 내 결제완료 금액
      long payAmountPrev,     // 직전 동일 기간 결제완료 금액
      long payCount,          // 기간 내 결제완료 건수
      long payCountPrev,      // 직전 동일 기간 결제완료 건수
      long failCount,         // 기간 내 결제실패 건수
      long cancelCount,       // 기간 내 결제취소 건수
      long refundAmount,      // 기간 내 환불 완료 금액
      long refundCount,       // 기간 내 환불 완료 건수
      long refundPending,     // 처리 대기 중인 환불 (기간 무관, 현재)
      long activeOrders,      // 현재 정상 구독 (SHOP_ORDER.STATUS=1)
      long waitingOrders      // 현재 매장연결 대기 (SHOP_ORDER.STATUS=0)
  ) {
  }

  /** 결제 통계 전체 응답 */
  public record Stats(
      int days,
      String from,
      String to,
      Summary summary,
      List<DailyPoint> daily,
      List<GroupAmount> byPlan,
      List<GroupAmount> byMethod,
      List<GroupAmount> byMonth,
      List<RecentPayment> recent) {
  }
}
