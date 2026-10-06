package dev.jpa.allimio.paystats;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.paystats.PayStatsDTO.Stats;

/* ---------------------------------------------------------------------
   /dbms/paystats(구독권 결제 통계) API - 관리자 전용.

   /pay_stats/** 는 SecurityConfig의 anyRequest().authenticated()에 걸려 로그인이 필요하고,
   관리자(ROLE_MANAGER)인지는 여기서 확인합니다.
   (SecurityConfig는 담당자 파일이라 수정하지 않음 - DashboardCont와 같은 방식)

   GET /pay_stats/stats?days=30   → 결제 통계 (days: 7 | 30 | 90)
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/pay_stats")
public class PayStatsCont {

  @Autowired
  private PayStatsService payStatsService;

  public PayStatsCont() {
    System.out.println("-> PayStatsCont created.");
  }

  /**
   * 구독권 결제 통계
   * http://localhost:9102/pay_stats/stats?days=30
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

    Stats stats = payStatsService.getStats(days);
    return ResponseEntity.ok(stats);
  }
}
