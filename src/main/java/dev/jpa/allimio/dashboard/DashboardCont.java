package dev.jpa.allimio.dashboard;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.dashboard.DashboardDTO.ShopOption;
import dev.jpa.allimio.dashboard.DashboardDTO.Stats;
import dev.jpa.allimio.dashboard.DashboardService.ShopAccessException;

/* ---------------------------------------------------------------------
   /user/dashboard(매장 통계) API.

   [로그인 회원 식별 - HttpOnly Cookie JWT]
   로그인이 쿠키 방식으로 바뀌면서 React는 토큰을 직접 읽을 수 없습니다(HttpOnly).
   그래서 mno를 쿼리 파라미터로 받지 않고, JwtAuthenticationFilter가 access_token 쿠키를
   검증한 뒤 SecurityContext에 넣어둔 Authentication에서 꺼냅니다.
     - principal   : 회원번호(Long no)
     - authorities : ROLE_MEMBER | ROLE_MANAGER, GRADE_{n}
   → 클라이언트가 mno를 조작해서 남의 매장 통계를 보는 것을 원천 차단.

   /dashboard/** 는 SecurityConfig의 anyRequest().authenticated()에 걸려서
   쿠키가 없거나 만료되면 401 → React 인터셉터가 /auth/reissue 후 재시도합니다.
   (SecurityConfig는 담당자 파일이라 수정하지 않고, 회원 전용 체크는 여기서 합니다)

   GET /dashboard/shops                 → 내가 볼 수 있는 매장 목록 (소유 + 소속)
   GET /dashboard/stats?sno=&days=7     → 매장 통계 (days: 7 | 30 | 90)
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/dashboard")
public class DashboardCont {

  @Autowired
  private DashboardService dashboardService;

  public DashboardCont() {
    System.out.println("-> DashboardCont created.");
  }

  /**
   * 내 매장 목록 (대시보드 매장 선택용)
   * http://localhost:9102/dashboard/shops
   */
  @GetMapping(path = "/shops")
  public ResponseEntity<List<ShopOption>> shops(Authentication authentication) {
    Long mno = memberNo(authentication);
    return ResponseEntity.ok(dashboardService.findMyShops(mno));
  }

  /**
   * 매장 통계
   * http://localhost:9102/dashboard/stats?sno=1&days=7
   * @param sno  매장번호 (로그인 회원의 소유/소속 매장이어야 함, 아니면 403)
   * @param days 조회기간 7 | 30 | 90 (그 외 값은 7로 보정)
   */
  @GetMapping(path = "/stats")
  public ResponseEntity<Stats> stats(
      Authentication authentication,
      @RequestParam("sno") long sno,
      @RequestParam(value = "days", defaultValue = "7") int days) {
    Long mno = memberNo(authentication);
    return ResponseEntity.ok(dashboardService.getStats(mno, sno, days));
  }

  /**
   * SecurityContext의 인증정보에서 회원번호 추출.
   * 관리자(ROLE_MANAGER) 토큰의 principal은 MANAGER.NO라 MEMBER.NO와 번호가 겹칠 수 있으므로
   * 회원(ROLE_MEMBER)이 아니면 403으로 막습니다.
   */
  private Long memberNo(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Long no)) {
      throw new ShopAccessException(401, "로그인이 필요합니다.");
    }

    boolean isMember = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_MEMBER"::equals);

    if (!isMember) {
      throw new ShopAccessException(403, "회원만 이용할 수 있습니다.");
    }
    return no;
  }

  /** 권한/매장 오류 → 상태코드 + {success:false, message} (다른 API 응답 형식과 동일) */
  @ExceptionHandler(ShopAccessException.class)
  public ResponseEntity<Map<String, Object>> handleAccess(ShopAccessException e) {
    return ResponseEntity.status(e.getStatus())
        .body(Map.of("success", false, "message", e.getMessage()));
  }
}
