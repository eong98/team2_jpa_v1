package dev.jpa.allimio.dashboard;

import java.util.List;

/* ---------------------------------------------------------------------
   /user/dashboard(매장 통계) 응답 DTO 모음.

   화면 하나가 쓰는 응답이라 클래스를 여러 개로 흩어놓지 않고 record로 한 파일에 모았습니다.
   record는 Java 16+ 문법으로 getter/equals/toString이 자동 생성되고 불변(immutable)이라
   "조회 결과를 담아서 JSON으로 내려주기만 하는" 통계 응답에 딱 맞습니다.
   (Jackson 2.12+는 record를 그대로 JSON 직렬화합니다 - Spring Boot 3.x 기본 포함)
--------------------------------------------------------------------- */
public class DashboardDTO {

  private DashboardDTO() {
  }

  /**
   * 로그인 회원이 볼 수 있는 매장 1건 (대시보드 매장 선택 드롭다운용).
   * @param no    매장번호 (SHOP.NO)
   * @param title 매장명
   * @param role  OWNER(SHOP.MNO 소유 점주) | STAFF(SHOP_MEMBER 배정 직원)
   */
  public record ShopOption(Long no, String title, String role) {
  }

  /** 날짜(yyyy-MM-dd) 또는 시간대(00~23) 한 칸의 건수 */
  public record Point(String label, long value) {
  }

  /** 이상행동 유형별 건수 (CCTV_ISSUE_CODE.CODE_NAME 조인) */
  public record CodeCount(String code, String codeName, int severity, long value) {
  }

  /** 상태값별 건수 (CCTV_ISSUE.STATE / CCTV.STATE / CCTV_VISITOR.STATE) */
  public record StateCount(int state, long value) {
  }

  /** 최근 이슈 1건 (대시보드 하단 목록) */
  public record RecentIssue(long no, long cno, String code, String codeName, int state,
      String reliability, String cdate) {
  }

  /** 상단 KPI 카드 값 (현재 기간 + 직전 동일 기간 비교값) */
  public record Summary(
      long visitorCount,        // 기간 내 방문객(입장) 수
      long visitorCountPrev,    // 직전 동일 기간 방문객 수
      Double avgStayMinutes,    // 기간 내 평균 체류시간(분), 데이터 없으면 null
      long currentVisitors,     // 지금 매장 안에 있는 인원 (STATE=0 입장중)
      long longStayCount,       // 기간 내 장시간체류(STATE=2) 건수
      long issueCount,          // 기간 내 이슈 발생 건수
      long issueCountPrev,      // 직전 동일 기간 이슈 건수
      long unconfirmedIssues,   // 기간 내 미확인(STATE=0) 이슈
      long confirmedIssues,     // 기간 내 정탐(STATE=1) 이슈
      long falseIssues,         // 기간 내 오탐(STATE=2) 이슈
      long cctvTotal,           // 매장 CCTV 등록 대수
      long cctvNormal           // 정상(STATE=0) CCTV 대수
  ) {
  }

  /** GET /dashboard/stats 응답 전체 */
  public record Stats(
      long sno,
      String shopTitle,
      int days,
      String from,              // 조회 시작일 yyyy-MM-dd (포함)
      String to,                // 조회 종료일 yyyy-MM-dd (포함, 오늘)
      Summary summary,
      List<Point> visitorDaily, // 일별 방문객 (빈 날짜 0으로 채움)
      List<Point> visitorHourly,// 시간대별 방문객 00~23 (빈 시간 0으로 채움)
      List<Point> issueDaily,   // 일별 이슈 (빈 날짜 0으로 채움)
      List<CodeCount> issueByCode,
      List<StateCount> cctvByState,
      List<RecentIssue> recentIssues
  ) {
  }
}
