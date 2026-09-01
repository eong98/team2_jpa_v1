package dev.jpa.allimio.shoporder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 구독 관련 날짜/개월수/금액 계산을 모아둔 유틸 클래스.
 *
 * <p>ShopOrderService(취소/갱신/변경 신청/승인 로직)가 "언제, 무엇을 바꿀지"
 * 같은 흐름 제어에만 집중할 수 있도록, 날짜 계산과 금액 산정 공식은
 * 전부 이 클래스로 분리했습니다. 모든 메서드는 상태를 갖지 않는 순수 계산
 * 함수(static)입니다.</p>
 *
 * <p><b>사용 개월수 계산 방식</b>: 반올림 없이, 실제 달력 기준(각 달의 실제
 * 일수)으로 순수 일할 계산합니다. 대신 각 액션(취소/갱신/변경)마다 "최소
 * 이만큼은 기간이 남아있어야 가능하다"는 조건을 ShopOrderService에서
 * 별도로 검사합니다(28일/275일/7일 등).</p>
 */
public class ShopOrderCaculator {

  private ShopOrderCaculator() {
  }

  // ────────────────────────────────────────────
  // 날짜/개월수 계산
  // ────────────────────────────────────────────

  /**
   * 두 날짜 사이의 개월수를 실제 달력 기준으로 순수 일할 계산합니다(반올림 없음).
   * 완전히 지난 달은 1개월씩 그대로 카운트하고, 마지막 자투리 기간은
   * "그 달의 실제 일수 대비 며칠을 썼는지" 비율로 소수 계산합니다.
   * @param from 시작일
   * @param to 종료일 (from보다 미래여야 함)
   * @return 개월수 (소수). to가 from 이전/같으면 0
   */
  public static double calcUseMonths(LocalDate from, LocalDate to) {
    if (!to.isAfter(from)) return 0;

    double months = 0;
    LocalDate cursor = from;

    while (!cursor.plusMonths(1).isAfter(to)) {
      months += 1;
      cursor = cursor.plusMonths(1);
    }

    long remainderDays = ChronoUnit.DAYS.between(cursor, to);
    if (remainderDays > 0) {
      months += remainderDays / (double) cursor.lengthOfMonth();
    }

    return months;
  }

  /**
   * 기준일부터 종료일(edate)까지 남은 일수를 계산합니다.
   * 취소(28일)/기간축소변경(275일)/대수변경(28일) 가능 여부 판단에 사용합니다.
   */
  public static long daysUntil(LocalDate from, String edate) {
    return ChronoUnit.DAYS.between(from, LocalDate.parse(edate));
  }

  /**
   * 갱신 시 새 종료일을 계산합니다. 기존 종료일(EDATE)에 연장 개월수를 더합니다.
   */
  public static String calcRenewEdate(String currentEdate, int extendMonths) {
    return LocalDate.parse(currentEdate).plusMonths(extendMonths).toString();
  }

  /**
   * 구독권 "기간 변경" 시 새 종료일을 계산합니다. 갱신과 달리 구독
   * 시작일(SDATE) 기준으로 새 이용기간 전체를 다시 계산합니다.
   */
  public static String calcChangedEdate(String sdate, int newPmonth) {
    return LocalDate.parse(sdate).plusMonths(newPmonth).toString();
  }

  // ────────────────────────────────────────────
  // 금액 계산 — 취소
  // ────────────────────────────────────────────

  /**
   * 구독 취소 시 환불 금액을 계산합니다. 결제 시점 단가(스냅샷, BPRICE) 기준입니다.
   */
  public static long calcCancelRefund(LocalDate sdate, LocalDate today, double bprice, int ccnt, int totalMonths) {
    double usedMonths = calcUseMonths(sdate, today);
    double refundMonths = Math.max(0, totalMonths - usedMonths);
    return Math.round(bprice * ccnt * refundMonths);
  }

  // ────────────────────────────────────────────
  // 금액 계산 — 갱신
  // ────────────────────────────────────────────

  /**
   * 구독 갱신 시 추가 결제 금액을 계산합니다. 갱신 당일 관리자 지정 단가
   * 기준으로, 기존 종료일부터 새 종료일까지(연장 구간)를 일할 계산합니다.
   */
  public static long calcRenewCharge(LocalDate oldEdate, LocalDate newEdate, double currentBprice, int ccnt) {
    double extendedMonths = calcUseMonths(oldEdate, newEdate);
    return Math.round(currentBprice * ccnt * extendedMonths);
  }

  // ────────────────────────────────────────────
  // 금액 계산 — 변경(기간)
  // ────────────────────────────────────────────

  /**
   * 기간을 늘리는 변경(예: 6→12개월) 시 추가 결제 금액을 계산합니다.
   * 구독 시작일부터 새 종료일까지 전체 기간을 당일 관리자 단가로 계산한
   * 값에서, 이미 낸 금액(기존 totalprice)을 뺀 차액입니다.
   */
  public static long calcPeriodIncreaseCharge(LocalDate sdate, LocalDate newEdate, double currentBprice, int ccnt, long alreadyPaid) {
    double totalNewMonths = calcUseMonths(sdate, newEdate);
    long newFullValue = Math.round(currentBprice * ccnt * totalNewMonths);
    return Math.max(0, newFullValue - alreadyPaid);
  }

  /**
   * 기간을 줄이는 변경(예: 12→6개월) 시 환불 금액을 계산합니다.
   * 구독 시작일부터 새 종료일까지 전체 기간을 결제 시점 단가로 계산한 값을,
   * 이미 낸 금액(기존 totalprice)에서 뺀 만큼 환불합니다.
   */
  public static long calcPeriodDecreaseRefund(LocalDate sdate, LocalDate newEdate, double paidBprice, int ccnt, long alreadyPaid) {
    double totalNewMonths = calcUseMonths(sdate, newEdate);
    long newFullValue = Math.round(paidBprice * ccnt * totalNewMonths);
    return Math.max(0, alreadyPaid - newFullValue);
  }

  // ────────────────────────────────────────────
  // 금액 계산 — 변경(대수)
  // ────────────────────────────────────────────

  /**
   * 대수를 늘리는 변경 신청 시, 신청 시점에 즉시 청구할 금액을 계산합니다.
   * 신청일부터 기존 종료일까지 남은 기간에 대해, 늘어난 대수분만 당일
   * 관리자 단가로 일할 계산합니다(원래 있던 대수분은 추가 청구 없음).
   */
  public static long calcCcntIncreaseCharge(LocalDate requestDate, LocalDate oldEdate, double currentBprice, int diffCcnt) {
    double remainMonths = calcUseMonths(requestDate, oldEdate);
    return Math.round(currentBprice * diffCcnt * remainMonths);
  }

  /**
   * 대수 증가 승인 시, "신청일~승인일" 기간만큼 과다청구된 금액을 정산(환불)합니다.
   * 이 기간 동안은 아직 기존 대수로 서비스를 이용했으므로, 이미 결제받은
   * 늘어난 대수분 중 이 기간에 해당하는 만큼을 돌려줍니다.
   */
  public static long calcCcntIncreaseSettlement(LocalDate requestDate, LocalDate approveDate, double pendingBprice, int diffCcnt) {
    double gapMonths = calcUseMonths(requestDate, approveDate);
    return Math.round(pendingBprice * diffCcnt * gapMonths);
  }

  /**
   * 대수를 줄이는 변경 승인 시 환불 금액을 계산합니다. 승인일부터 종료일까지
   * 남은 기간에 대해, 줄어든 대수분을 결제 시점 단가 기준으로 일할 계산합니다.
   */
  public static long calcCcntDecreaseRefund(LocalDate approveDate, LocalDate edate, double paidBprice, int diffCcnt) {
    double remainMonths = calcUseMonths(approveDate, edate);
    return Math.round(paidBprice * diffCcnt * remainMonths);
  }
}