package dev.jpa.allimio.dashboard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.dashboard.DashboardDTO.CodeCount;
import dev.jpa.allimio.dashboard.DashboardDTO.Point;
import dev.jpa.allimio.dashboard.DashboardDTO.RecentIssue;
import dev.jpa.allimio.dashboard.DashboardDTO.ShopOption;
import dev.jpa.allimio.dashboard.DashboardDTO.StateCount;
import dev.jpa.allimio.dashboard.DashboardDTO.Stats;
import dev.jpa.allimio.dashboard.DashboardDTO.Summary;
import dev.jpa.allimio.shop.Shop;
import dev.jpa.allimio.shop.ShopRepository;
import dev.jpa.allimio.shopmember.ShopMemberRepository;

/* ---------------------------------------------------------------------
   /user/dashboard 매장 통계 서비스.

   [소속매장 판별 규칙] - ShopService.searchForUser()와 같은 기준
   - 점주(grade 10): SHOP.MNO == 로그인 회원번호 인 매장
   - 직원(grade 6~9): SHOP_MEMBER(SNO, MNO)에 배정된 매장
   통계 조회 시에는 grade와 상관없이 "소유 OR 배정" 둘 중 하나면 접근 허용합니다.
   (점주가 다른 매장에 직원으로 배정되는 경우까지 자연스럽게 커버)
--------------------------------------------------------------------- */
@Service
@Transactional(readOnly = true) // 조회 전용 - 더티체킹/플러시 생략으로 가볍게
public class DashboardService {

  /** 허용 조회기간(일). 그 외 값이 오면 7일로 보정 */
  private static final List<Integer> ALLOWED_DAYS = List.of(7, 30, 90);

  @Autowired
  private DashboardRepository dashboardRepository;

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private ShopMemberRepository shopMemberRepository;

  /** 접근 거부/매장 없음을 컨트롤러에서 403/404로 바꾸기 위한 예외 */
  public static class ShopAccessException extends RuntimeException {
    private final int status;

    public ShopAccessException(int status, String message) {
      super(message);
      this.status = status;
    }

    public int getStatus() {
      return status;
    }
  }

  /* =========================================================
     내가 볼 수 있는 매장 목록 (소유 + 소속)
  ========================================================= */
  public List<ShopOption> findMyShops(long mno) {
    // LinkedHashMap: 매장번호로 중복 제거 + 넣은 순서(소유 매장 먼저) 유지
    Map<Long, ShopOption> map = new LinkedHashMap<>();

    for (Object[] row : dashboardRepository.findOwnedShops(mno)) {
      Long no = ((Number) row[0]).longValue();
      map.put(no, new ShopOption(no, (String) row[1], "OWNER"));
    }
    for (Object[] row : dashboardRepository.findStaffShops(mno)) {
      Long no = ((Number) row[0]).longValue();
      map.putIfAbsent(no, new ShopOption(no, (String) row[1], "STAFF")); // 이미 소유면 OWNER 유지
    }
    return new ArrayList<>(map.values());
  }

  /* =========================================================
     매장 접근 권한 확인 → 매장 엔티티 반환
  ========================================================= */
  public Shop checkAccess(long mno, long sno) {
    Optional<Shop> optional = shopRepository.findById(sno);
    if (optional.isEmpty()) {
      throw new ShopAccessException(404, "존재하지 않는 매장입니다.");
    }

    Shop shop = optional.get();
    boolean isOwner = shop.getMno() == mno;
    boolean isStaff = shopMemberRepository.existsBySnoAndMno(sno, mno);

    if (!isOwner && !isStaff) {
      throw new ShopAccessException(403, "소속된 매장이 아닙니다.");
    }
    return shop;
  }

  /* =========================================================
     통계 조회
  ========================================================= */
  public Stats getStats(long mno, long sno, int days) {
    Shop shop = checkAccess(mno, sno);

    if (!ALLOWED_DAYS.contains(days)) {
      days = 7;
    }

    // 기간: 오늘 포함 최근 N일 [from, to], 쿼리는 반열림 구간 [from, toNext)
    LocalDate today = LocalDate.now();
    LocalDate fromDate = today.minusDays(days - 1L);
    String from = fromDate.toString();              // LocalDate.toString() = yyyy-MM-dd
    String to = today.toString();
    String toNext = today.plusDays(1).toString();

    // 직전 동일 기간 (증감률 비교용): [fromDate - days, fromDate)
    String prevFrom = fromDate.minusDays(days).toString();
    String prevToNext = from;

    // ---- 방문객 ----
    long visitorCount = dashboardRepository.countVisitors(sno, from, toNext);
    long visitorCountPrev = dashboardRepository.countVisitors(sno, prevFrom, prevToNext);
    Double avgStay = dashboardRepository.avgStayMinutes(sno, from, toNext);
    long currentVisitors = dashboardRepository.countVisitorsByState(sno, 0, null, null); // 입장중
    long longStayCount = dashboardRepository.countVisitorsByState(sno, 2, from, toNext); // 장시간체류

    // ---- 이슈 ----
    long issueCount = dashboardRepository.countIssues(sno, from, toNext);
    long issueCountPrev = dashboardRepository.countIssues(sno, prevFrom, prevToNext);

    Map<Integer, Long> issueStateMap = new HashMap<>();
    for (Object[] row : dashboardRepository.issueByState(sno, from, toNext)) {
      issueStateMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
    }

    // ---- CCTV ----
    List<StateCount> cctvByState = new ArrayList<>();
    long cctvTotal = 0;
    long cctvNormal = 0;
    for (Object[] row : dashboardRepository.cctvByState(sno)) {
      int state = ((Number) row[0]).intValue();
      long cnt = ((Number) row[1]).longValue();
      cctvByState.add(new StateCount(state, cnt));
      cctvTotal += cnt;
      if (state == 0) cctvNormal = cnt;
    }

    Summary summary = new Summary(
        visitorCount,
        visitorCountPrev,
        avgStay == null ? null : Math.round(avgStay * 10) / 10.0, // 소수 첫째 자리
        currentVisitors,
        longStayCount,
        issueCount,
        issueCountPrev,
        issueStateMap.getOrDefault(0, 0L),
        issueStateMap.getOrDefault(1, 0L),
        issueStateMap.getOrDefault(2, 0L),
        cctvTotal,
        cctvNormal);

    // ---- 차트 데이터 (빈 칸은 0으로 채워서 x축이 끊기지 않게) ----
    List<Point> visitorDaily = fillDays(fromDate, days, dashboardRepository.visitorDaily(sno, from, toNext));
    List<Point> issueDaily = fillDays(fromDate, days, dashboardRepository.issueDaily(sno, from, toNext));
    List<Point> visitorHourly = fillHours(dashboardRepository.visitorHourly(sno, from, toNext));

    List<CodeCount> issueByCode = new ArrayList<>();
    for (Object[] row : dashboardRepository.issueByCode(sno, from, toNext)) {
      String code = (String) row[0];
      String codeName = row[1] != null ? (String) row[1] : code; // 코드 테이블에 없으면 코드값 그대로
      issueByCode.add(new CodeCount(code, codeName, ((Number) row[2]).intValue(), ((Number) row[3]).longValue()));
    }

    List<RecentIssue> recentIssues = new ArrayList<>();
    for (Object[] row : dashboardRepository.recentIssues(sno, 5)) {
      String code = (String) row[2];
      recentIssues.add(new RecentIssue(
          ((Number) row[0]).longValue(),
          ((Number) row[1]).longValue(),
          code,
          row[3] != null ? (String) row[3] : code,
          ((Number) row[4]).intValue(),
          (String) row[5],
          (String) row[6]));
    }

    return new Stats(sno, shop.getTitle(), days, from, to, summary,
        visitorDaily, visitorHourly, issueDaily, issueByCode, cctvByState, recentIssues);
  }

  /** GROUP BY 결과(데이터 있는 날만 옴)를 from부터 days일 연속 배열로 펼침. 없는 날은 0 */
  private List<Point> fillDays(LocalDate fromDate, int days, List<Object[]> rows) {
    Map<String, Long> map = toMap(rows);
    List<Point> list = new ArrayList<>(days);
    for (int i = 0; i < days; i++) {
      String d = fromDate.plusDays(i).toString();
      list.add(new Point(d, map.getOrDefault(d, 0L)));
    }
    return list;
  }

  /** 00~23시 24칸으로 펼침. 없는 시간은 0 */
  private List<Point> fillHours(List<Object[]> rows) {
    Map<String, Long> map = toMap(rows);
    List<Point> list = new ArrayList<>(24);
    for (int h = 0; h < 24; h++) {
      String hh = String.format("%02d", h);
      list.add(new Point(hh, map.getOrDefault(hh, 0L)));
    }
    return list;
  }

  private Map<String, Long> toMap(List<Object[]> rows) {
    Map<String, Long> map = new HashMap<>();
    for (Object[] row : rows) {
      if (row[0] != null) {
        map.put((String) row[0], ((Number) row[1]).longValue());
      }
    }
    return map;
  }
}
