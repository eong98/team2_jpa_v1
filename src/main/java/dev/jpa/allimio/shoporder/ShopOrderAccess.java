package dev.jpa.allimio.shoporder;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import dev.jpa.allimio.shoppayment.ShopPayment;
import dev.jpa.allimio.shoppayment.ShopPaymentRepository;
import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   구독·결제 API 접근 확인 (구독 주문 / 변경 신청 / 주문 로그 / 결제 / 환불 공용)

   예전에는 화면이 주소에 회원번호를 넣어 보냈고(/shop_order/list/23) 서버가 그대로 믿어서,
   번호만 바꾸면 다른 회원의 구독·결제 내역이 조회됐습니다. 주문번호·결제번호로 조회·변경하는 API도
   그 주문이 누구 것인지 확인하지 않았습니다.

   - 회원번호는 로그인 토큰에서 꺼냅니다 (JwtAuthenticationFilter가 principal에 번호, 권한에 ROLE_MEMBER/ROLE_MANAGER를 넣어 둠)
   - 주문·결제는 구매한 회원 본인과 관리자만 볼 수 있습니다 (직원 계정은 구독 내역을 보지 않음)
   - 없는 번호는 여기서 막지 않습니다 → 각 컨트롤러가 지금처럼 404 등으로 응답
--------------------------------------------------------------------- */
@Component
@RequiredArgsConstructor
public class ShopOrderAccess {

  private final ShopOrderRepository shopOrderRepository;
  private final ShopPaymentRepository shopPaymentRepository;

  public boolean isManager(Authentication auth) {
    return hasRole(auth, "ROLE_MANAGER");
  }

  /** 회원 로그인이면 회원번호, 아니면 null (관리자 번호는 회원번호와 겹칠 수 있어 제외) */
  public Long memberNoOrNull(Authentication auth) {
    if (hasRole(auth, "ROLE_MEMBER") && auth.getPrincipal() instanceof Long no) {
      return no;
    }
    return null;
  }

  /** 회원 로그인 필수 — 로그인한 회원번호 */
  public Long requireMember(Authentication auth) {
    Long mno = memberNoOrNull(auth);
    if (mno == null) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "회원만 이용할 수 있습니다.");
    }
    return mno;
  }

  /** 이 주문(구독)을 구매한 회원 본인이거나 관리자인지 */
  public void assertOrderOwner(Authentication auth, String orderNo) {
    if (isManager(auth) || orderNo == null) {
      return;
    }
    Long mno = requireMember(auth);
    ShopOrder order = shopOrderRepository.findById(orderNo).orElse(null);
    if (order != null && !mno.equals(order.getMno())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 구독 내역만 이용할 수 있습니다.");
    }
  }

  /** 이 결제를 한 회원 본인이거나 관리자인지 */
  public void assertPaymentOwner(Authentication auth, Long paymentNo) {
    if (isManager(auth) || paymentNo == null) {
      return;
    }
    Long mno = requireMember(auth);
    ShopPayment payment = shopPaymentRepository.findById(paymentNo).orElse(null);
    if (payment != null && !mno.equals(payment.getMno())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 결제 내역만 이용할 수 있습니다.");
    }
  }

  private boolean hasRole(Authentication auth, String role) {
    return auth != null && auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
  }
}
