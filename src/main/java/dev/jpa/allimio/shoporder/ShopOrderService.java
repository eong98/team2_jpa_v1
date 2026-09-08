package dev.jpa.allimio.shoporder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.cctv.CctvRepository;
import dev.jpa.allimio.shop.Shop;
import dev.jpa.allimio.shop.ShopWithCctvCount;
import dev.jpa.allimio.shoporderlog.ShopOrderLogService;
import dev.jpa.allimio.shoppayment.ShopPaymentDTO;
import dev.jpa.allimio.shoppayment.ShopPaymentService;
import dev.jpa.allimio.shopplan.ShopPlan;
import dev.jpa.allimio.shopplan.ShopPlanRepository;
import dev.jpa.allimio.shoprefund.ShopRefundDTO;
import dev.jpa.allimio.shoprefund.ShopRefundService;
import dev.jpa.allimio.tool.Tool;
import jakarta.persistence.Tuple;

@Service
public class ShopOrderService {
  @Autowired
  ShopOrderRepository shopOrderRepository;
  
  @Autowired
  ShopPlanRepository shopPlanRepository;

  @Autowired
  CctvRepository cctvRepository;

  @Autowired
  ShopOrderLogService shopOrderLogService;

  @Autowired
  ShopPaymentService shopPaymentService;

  @Autowired
  ShopRefundService shopRefundService;

  // ══════════════════════════════════════════════
  // STATUS 상수 (가독성용) — 0:매장연결대기 / 1:정상 / 2:취소
  // ══════════════════════════════════════════════
  private static final int STATUS_WAIT_SHOP = 0;
  private static final int STATUS_NORMAL = 1;
  private static final int STATUS_CANCELLED = 2;
  
  
  // --------------------------------------------------
  // 등록
  // --------------------------------------------------
  
  /**
   * 랜덤 주문번호 발급 (형식: ORD-yyyyMMdd6자리 랜덤숫자). 충돌 시 재발급합니다.
   * @return
   */
  private String generateno() {
    String datePart = Tool.getDate().substring(0, 10).replace("-", "");
    String no;
    do {
      String randomPart = String.format("%06d", new Random().nextInt(1000000));
      no = "ORD-" + datePart + randomPart;
    } while (shopOrderRepository.existsById(no));
    return no;
  }


  /**
   * 구독권 등록 (매장 NULL, 상태 0)
   *  
   * @param request
   * @return
   */
  public ShopOrderDTO.Response save(ShopOrderDTO.Request request) {
    ShopOrder shopOrder = ShopOrder.builder()
        .no(generateno())
        .pno(request.getPno())
        .mno(request.getMno())
        .sno(null)
        .pmonth(request.getPmonth())
        .ccnt(request.getCcnt())
        .bprice(request.getBprice())
        .totalprice(request.getTotalprice())
        .status(STATUS_WAIT_SHOP)
        .sdate(null)
        .edate(null)
        .cdate(Tool.getDate())
        .build();

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    // 주문번호, 회원번호, 이벤트 종류(결제), 매장번호(NULL), 
    // 변경 전 종료일(NULL), 변경 후 종료일(NULL), 총결제액, 설명
    shopOrderLogService.log(saved.getNo(), saved.getMno(), 0, null,
        null, null, saved.getTotalprice(), "신규 구독 결제");

    // 주문번호, 회원번호, 결제금액, 결제수단
    shopPaymentService.pay(saved.getNo(), saved.getMno(), saved.getTotalprice(), request.getPmethod());

    return ShopOrderDTO.Response.from(saved);
  }

  // --------------------------------------------------
  // 목록
  // --------------------------------------------------
  
  /**
   * 전체 구독내역 목록 
   *  
   * @param req
   * @param pageable
   * @return /user/shoporder/ShopOrderList.tsx
   */
  @Transactional(readOnly = true)
  public Page<ShopOrderDTO.Response> searchOrders(ShopOrderDTO.SearchRequest req, Pageable pageable) {
    String today = LocalDate.now().toString();
    Page<Object[]> result = shopOrderRepository.searchAllWithJoin(
        req.getMno(),
        req.getWord(),
        req.getStatus(),
        req.getPmonth(),
        req.getDateFrom(),
        req.getDateTo(),
        today,
        pageable
    );

    // Page.map을 통해 DTO의 from() 메서드로 가공
    return result.map(row -> {
      ShopOrder order = (ShopOrder) row[0];
      String pname = (String) row[1];
      String sname = (String) row[2];
      Integer pstatus = row[3] != null ? ((Number) row[3]).intValue() : null;
      return ShopOrderDTO.Response.from(order, pname, sname, pstatus);
    });
  }
  
  /**
   * 매장별 전체 구독내역 목록
   * 
   * @param req
   * @param pageable
   * @return /user/shop/ShopOrderList.tsx
   */
  @Transactional(readOnly = true)
  public Page<ShopOrderDTO.Response> searchShopOrders(ShopOrderDTO.SearchRequest req, Pageable pageable) {
    String today = LocalDate.now().toString();
    
    Page<Object[]> result = shopOrderRepository.searchSnoAndMno(
        req.getMno(),
        req.getSno(),
        req.getWord(),
        req.getStatus(),
        req.getPmonth(),
        req.getDateFrom(),
        req.getDateTo(),
        today,
        pageable
        );
    
    // Page.map을 통해 DTO의 from() 메서드로 가공
    return result.map(row -> {
      ShopOrder order = (ShopOrder) row[0];
      String pname = (String) row[1];
      Integer pstatus = row[2] != null ? ((Number) row[2]).intValue() : null;
      return ShopOrderDTO.Response.from(order, pname, null, pstatus);
    });
  }
  
  /**
   * 매장별 구독내역 상단 노출(상태: 정상 | 만료)
   * @param sno
   * @param status
   * @return order | null
   */
  @Transactional(readOnly = true)
  public ShopOrderDTO.Response findByShopTop(Long mno, Long sno, Integer status) {
    List<Object[]> res = shopOrderRepository.findByShopTop(mno, sno, status);
    
    Object[] row = res.get(0);
    ShopOrder order = (ShopOrder) row[0];
    String pname = (String) row[1];
    Integer pstatus = row[2] != null ? ((Number) row[2]).intValue() : null;

    return ShopOrderDTO.Response.from(order, pname, null, pstatus);
  }

  /** 주문내역 상세조회 */
  @Transactional(readOnly = true)
  public ShopOrderDTO.Response findById(String no) {
    // 구독권 이름, 매장이름 JOIN 포함된 목록
    List<Object[]> res = shopOrderRepository.findByIdWithJoin(no);

    Object[] row = res.get(0);
    ShopOrder order = (ShopOrder) row[0];
    String pname = (String) row[1];
    String sname = (String) row[2];
      
    return ShopOrderDTO.Response.from(order, pname, sname, null);
  }
  
  /**
   * 구독 결제 완료 후 
   * 로그인된 점주의 연결 가능한 매장 목록 조회
   * 
   * 1. 연결된 구독권이 없는 매장
   * 2. 연결된 구독권이 있지만 취소된 매장
   * 3. 연결된 구독권의 종료일자가 지난 매장
   * 
   * @param mno 회원번호
   * @return 연결 가능한 매장 목록 (CCTV 등록 대수 포함) : ShopMatch.tsx
   */
  public List<ShopWithCctvCount> findLinkableShops(long mno) {
    String today = LocalDate.now().toString();
    
    List<Shop> shops = shopOrderRepository.findLinkableShops(mno, today);
    
    return shops.stream()
        .map(shop -> new ShopWithCctvCount(shop, cctvRepository.countBySno(shop.getNo())))
        .collect(Collectors.toList());
  }
  
  /**
   * 로그인된 점주의 매장1에 연결 가능한 구독권 목록 조회
   * 
   * 1. 연결되지 않은 구독권(대기상태)
   * 2. 연결된(정상,만료), 취소된 구독권은 대상아님
   * 
   * @param mno 회원번호
   * @param sno 접속한 매장의 번호
   * @return 연결 가능한 구독권 목록 : ShopOrderMatch.tsx
   */
  public List<ShopOrderDTO.Response> findLinkableOrders(Long mno, Long sno) {
    return shopOrderRepository.findLinkableOrders(mno).stream()
        .filter(row -> {
          ShopOrder order = (ShopOrder) row[0];
          return order.getCcnt() != null && cctvRepository.countBySno(sno) == order.getCcnt();
        })
        .map(row -> {
          ShopOrder order = (ShopOrder) row[0];
          String pname = (String) row[1];
          return ShopOrderDTO.Response.from(order, pname, null, null);
        })
        .collect(Collectors.toList());
  }
  

  /**
   * 특정 매장의 정상 상태인 구독권 개수 (무조건 0, 1 반환됨)
   * 
   * @param sno
   * @return 목록 개수 반환 / 0: 갱신가능 | 1: 갱신불가 (리액트에서 처리)
   */
  public int setCount(Long sno) {
    LocalDate today = LocalDate.now();
    
    List<ShopOrder> list = shopOrderRepository.findAllBySnoAndStatus(sno, 1).stream()
        .filter(order -> {
          if (order.getEdate() == null || order.getEdate().length() < 10) return false;
          
          LocalDate oldEdate = LocalDate.parse(order.getEdate().substring(0, 10));
          return !today.isAfter(oldEdate);
        }).collect(Collectors.toList());
    
    return  list.size();
  }  
  

  /**
   * 변경가능한 CCTV 대수 호출
   * @param no
   * @return
   */
  @Transactional(readOnly = true)
  public ShopOrderDTO.Response setChangeInfo(String no) {
    List<Object[]> res = shopOrderRepository.findByNoWithChange(no);

    Object[] row = res.get(0);
    ShopOrder order = (ShopOrder) row[0];
    Integer minCcnt = row[1] != null ? ((Number) row[1]).intValue() : null;
    Integer maxCcnt = row[2] != null ? ((Number) row[2]).intValue() : null;

    return ShopOrderDTO.Response.from(order, null, null, null, minCcnt, maxCcnt);
  }
  
  
  
  
  
  /**
   * 관리자용 전체(또는 특정 회원) 구독 내역 검색 + 페이징 조회
   * @param c 검색조건 (mno 선택사항 — null이면 전체 회원 대상)
   */
//  public Page<ShopOrderDTO.Response> searchAllAdmin(ShopOrderDTO.SearchRequest c, Pageable pageable) {
//    Page<ShopOrder> result = shopOrderRepository.searchAllAdmin(
//        c.getMno(), c.getWord(), c.getStatus(), c.getPno(), c.getSno(),
//        c.getDateType(), c.getDateFrom(), c.getDateTo(),
//        pageable);
//
//    return result.map(ShopOrderDTO.Response::from);
//  }
  

  
  

  // --------------------------------------------------
  // 수정
  // --------------------------------------------------
  

  /**
   * 매장 선택 확정 — 결제된 구독 내역(SNO=null)을 특정 매장에 연결합니다.
   * 연결 성공 시 STATUS를 정상(1)으로 전환합니다.
   * @param no 연결할 구독 내역 번호
   * @param request 연결할 매장 번호
   * @return 연결된 구독 내역
   * @throws IllegalStateException 검증 실패 시 사유가 담긴 메시지와 함께 발생
   */
  public ShopOrderDTO.Response linkShop(String no, ShopOrderDTO.LinkShopRequest request) {
    LocalDate today = LocalDate.now();
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    
    if (optional.isEmpty()) {
      throw new IllegalArgumentException("존재하지 않는 구독 내역입니다.");
    }

    ShopOrder shopOrder = optional.get();

    if (shopOrder.getStatus() == STATUS_CANCELLED) {
      throw new IllegalStateException("취소된 구독은 매장에 연결할 수 없습니다.");
    }

    // 정상, 만료 구분
    Optional<ShopOrder> shop_normal = shopOrderRepository.findBySnoAndStatus(request.getSno(), STATUS_NORMAL);
    if (shop_normal.isPresent()) {
      ShopOrder normal = shop_normal.get();
      LocalDate oldEdate = LocalDate.parse(normal.getEdate());
      
      if (!today.isAfter(oldEdate)) {
        throw new IllegalStateException("이미 다른 구독권이 연결된 매장입니다.");
      }      
    }

    long actualCctvCount = cctvRepository.countBySno(request.getSno());
    if (shopOrder.getCcnt() != actualCctvCount) {
      throw new IllegalStateException("결제된 CCTV 대수와 매장에 등록된 CCTV 대수가 달라 해당 매장에 구독권을 연결할 수 없습니다.");
    }
    
    LocalDate edate = today.plusMonths(shopOrder.getPmonth());
    shopOrder.setSdate(today.toString());
    shopOrder.setEdate(edate.toString());

    shopOrder.setSno(request.getSno());
    // 구독기간 시작 전 업데이트라 udate 처리 안함
    // shopOrder.setUdate(Tool.getDate());
    shopOrder.setStatus(STATUS_NORMAL); // 정상(2)으로 확정

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    // 주문번호, 회원번호, 이벤트 종류(결제), 매장번호(NULL), 
    // 변경 전 종료일(NULL), 변경 후 종료일(NULL), 총결제액, 설명
    shopOrderLogService.log(saved.getNo(), saved.getMno(), 1, saved.getSno(),
        null, saved.getEdate(), null, "매장 연결 확정");

    return ShopOrderDTO.Response.from(saved);
  }



  // ══════════════════════════════════════════════
  // 갱신 — 만료(edate) 7일 전부터만 가능, 만료 후 불가
  // ══════════════════════════════════════════════

  /**
   * 구독 갱신. 만료 7일 전부터 만료 시점까지만 가능하고, 만료 후에는
   * 불가합니다. newPmonth를 안 주면 동일조건 갱신, 주면 그 기간으로 연장
   * 갱신입니다(대수는 항상 그대로). 갱신 당일 관리자 지정 단가로 일할
   * 계산해서 즉시 추가 결제합니다.
   * @param no
   * @param newPmonth 갱신 시 적용할 기간(null이면 동일조건)
   * @param pmethod 추가결제 수단
   * @return 갱신 결과, 대상 아니거나 조건 미충족 시 null
   */
  public ShopOrderDTO.RenewResult renew(String no, Integer newPmonth, Integer pmethod) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) throw new IllegalStateException("갱신 대상을 찾을수 없습니다.");

    ShopOrder shopOrder = optional.get();
    // 취소 상태는 갱신대상 아님.
    if (shopOrder.getStatus() == STATUS_CANCELLED) throw new IllegalStateException("취소된 구독권은 갱신할 수 없습니다.");
    // 매장대기상태 (edate 없는 경우) 갱신 대상 아님
    if (shopOrder.getEdate() == null && shopOrder.getStatus() == STATUS_WAIT_SHOP)  throw new IllegalStateException("매장 대기 상태는 갱신 대상이 아닙니다.");

    LocalDate today = LocalDate.now();
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());

    if (today.isAfter(oldEdate)) throw new IllegalStateException("만료일이 지났습니다.");; // 만료 후 갱신 불가 (리액트에서 버튼 안나오게 처리)
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());
    if (daysLeft > 7)  throw new IllegalStateException("갱신 가능 기간이 아닙니다.");; // 만료 7일 전부터만 가능

    int extendMonths = newPmonth != null ? newPmonth : shopOrder.getPmonth();

    // 연장기간+동일대수 조합에 맞는 등급/단가를 다시 조회 (관리자가 단가를 올렸을 수 있음)
    ShopPlan currentPlan = shopPlanRepository.findByPmonthAndCcntInRange(extendMonths, shopOrder.getCcnt())
        .orElseThrow(() -> new IllegalStateException("구독권 정보를 찾을 수 없습니다."));

    String beforeEdate = shopOrder.getEdate();
    String newEdate = ShopOrderCaculator.calcRenewEdate(shopOrder.getEdate(), extendMonths);
    LocalDate newEdateDate = LocalDate.parse(newEdate);

    long extraCharge = ShopOrderCaculator.calcRenewCharge(oldEdate, newEdateDate, currentPlan.getBprice(), shopOrder.getCcnt());

    shopOrder.setPno(currentPlan.getNo());
    shopOrder.setPmonth(extendMonths);
    shopOrder.setBprice(currentPlan.getBprice());
    shopOrder.setTotalprice(shopOrder.getTotalprice() + extraCharge);
    shopOrder.setEdate(newEdate);
    
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);
    
    // 주문번호, 회원번호, 결제금액, 결제수단
    shopPaymentService.pay(saved.getNo(), saved.getMno(), extraCharge, pmethod);
    
    // 주문번호, 회원번호, 이벤트 종류, 매장번호, 
    // 변경 전 종료일, 변경 후 종료일, 총결제액, 설명
    shopOrderLogService.log(saved.getNo(), saved.getMno(), 2, saved.getSno(),
        beforeEdate, newEdate, extraCharge,
        "구독 갱신" + (newPmonth != null ? "(기간 변경)" : "(동일조건)"));

    return ShopOrderDTO.RenewResult.builder()
        .no(saved.getNo())
        .ccnt(saved.getCcnt())
        .totalprice(saved.getTotalprice())
        .edate(saved.getEdate())
        .build();
  }

  // ══════════════════════════════════════════════
  // 취소 — 신청일~EDATE 28일 이상 남아야 가능
  // ══════════════════════════════════════════════

  /**
   * 구독 취소. 매장 미연결(SDATE 없음)이면 기간 제약 없이 전액 환불합니다.
   * 매장 연결된 경우 신청일~EDATE가 28일 미만이면 취소 불가하고, 환불액은
   * 결제 시점 단가(스냅샷) 기준으로 일할 계산합니다.
   */

  public ShopOrderDTO.CancelResult cancel(String no, ShopOrderDTO.CancelRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    // 매장 미연결 — 기간 제약 없이 전액 환불
    if (shopOrder.getSdate() == null) {
      shopOrder.setStatus(STATUS_CANCELLED);
      shopOrder.setUdate(Tool.getDate());
      shopOrderRepository.save(shopOrder);

      long fullRefund = Math.round(shopOrder.getBprice() * shopOrder.getCcnt() * shopOrder.getPmonth());
      
      // 주문번호, 회원번호, 이벤트 종류(취소), 매장번호, 
      // 변경 전 종료일(NULL), 변경 후 종료일(NULL), 총결제액, 설명
      shopOrderLogService.log(no, shopOrder.getMno(), 3, shopOrder.getSno(),
          null, null, fullRefund, "매장 연결 전 취소 · 전액 환불");

      // 환불 데이터 처리
      if (fullRefund > 0) {
        ShopPaymentDTO.Response payment = shopPaymentService.refund(no, shopOrder.getMno(), fullRefund);
        ShopRefundDTO.Request refundRequest = ShopRefundDTO.Request.builder()
            .bankName(request.getBankName())
            .accountNo(request.getAccountNo())
            .accountHolder(request.getAccountHolder())
            .build();
        shopRefundService.save(no, payment.getNo(), shopOrder.getMno(), refundRequest, fullRefund);
      }

      return ShopOrderDTO.CancelResult.builder()
          .no(no).usedMonths(0).refundMonths(shopOrder.getPmonth()).refundAmount(fullRefund)
          .build();
    }

    LocalDate today = LocalDate.now();
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());
    if (daysLeft < 28) {
      throw new IllegalStateException("구독 종료까지 28일 미만이면 취소할 수 없습니다.");
    }

    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    long refundAmount = ShopOrderCaculator.calcCancelRefund(
        sdate, today, shopOrder.getBprice(), shopOrder.getCcnt(), shopOrder.getPmonth());

    // 환불 대상인데 계좌 정보 없으면 거부(리액트에서도 처리)
    if (refundAmount > 0 && (request == null || request.getAccountNo() == null || request.getAccountNo().isBlank())) {
      throw new IllegalStateException("환불 계좌를 입력해 주세요.");
    }

    shopOrder.setStatus(STATUS_CANCELLED);
    shopOrder.setUdate(Tool.getDate());
    shopOrderRepository.save(shopOrder);

    double usedMonths = ShopOrderCaculator.calcUseMonths(sdate, today);
    
    // 주문번호, 회원번호, 이벤트 종류(결제), 매장번호(NULL), 
    // 변경 전 종료일(NULL), 변경 후 종료일(NULL), 총결제액, 설명
    shopOrderLogService.log(no, shopOrder.getMno(), 3, shopOrder.getSno(),
        null, null, refundAmount, "구독 취소");

    if (refundAmount > 0) {
      ShopPaymentDTO.Response payment = shopPaymentService.refund(no, shopOrder.getMno(), refundAmount);
      ShopRefundDTO.Request refundRequest = ShopRefundDTO.Request.builder()
          .bankName(request.getBankName())
          .accountNo(request.getAccountNo())
          .accountHolder(request.getAccountHolder())
          .build();
      shopRefundService.save(no, payment.getNo(), shopOrder.getMno(), refundRequest, refundAmount);
    }

    return ShopOrderDTO.CancelResult.builder()
        .no(no)
        .usedMonths(usedMonths)
        .refundMonths(Math.max(0, shopOrder.getPmonth() - usedMonths))
        .refundAmount(refundAmount)
        .build();
  }

}