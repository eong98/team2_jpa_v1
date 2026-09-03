package dev.jpa.allimio.shoporder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
  // STATUS 상수 (가독성용) — 0:매장연결대기 / 1:정상 / 2:만료 / 3:취소
  // ══════════════════════════════════════════════
  private static final int STATUS_WAIT_SHOP = 0;
  private static final int STATUS_NORMAL = 1;
  private static final int STATUS_EXPIRED = 2;
  private static final int STATUS_CANCELLED = 3;
  
  
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
   * 신규 구독 결제 등록. 매장(SNO)은 아직 비워두고(매장연결대기, STATUS=0),
   * BPRICE는 결제 시점 단가를 스냅샷으로 저장합니다.
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

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 0, null,
        null, null, saved.getTotalprice(), "신규 구독 결제");

    shopPaymentService.pay(saved.getNo(), saved.getMno(), saved.getTotalprice(), request.getPmethod());

    return ShopOrderDTO.Response.from(saved);
  }

  // --------------------------------------------------
  // 목록
  // --------------------------------------------------
  

  /** 주문번호 기준 단일 조회 */
  @Transactional(readOnly = true)
  public ShopOrderDTO.Response findById(String no) {
    Optional<Tuple> optional = shopOrderRepository.findWithJoinById(no);
    if (optional.isEmpty()) return null;

    Tuple tuple = optional.get();
    ShopOrder shopOrder = tuple.get("order", ShopOrder.class);
    String pname = tuple.get("pname", String.class);
    String sname = tuple.get("sname", String.class);

    Number minCcntNum = tuple.get("minCcnt", Number.class);
    Number maxCcntNum = tuple.get("maxCcnt", Number.class);
    Integer minCcnt = minCcntNum != null ? minCcntNum.intValue() : null;
    Integer maxCcnt = maxCcntNum != null ? maxCcntNum.intValue() : null;

    return ShopOrderDTO.Response.from(shopOrder, pname, sname, minCcnt, maxCcnt);
  }
  

  /** 회원 기준 구독 내역 전체 검색 + 페이징 조회 (전체 구독내역 목록) */
  @Transactional(readOnly = true)
  public Page<ShopOrderDTO.Response> searchOrders(ShopOrderDTO.SearchRequest req, Pageable pageable) {
    Page<Object[]> result = shopOrderRepository.searchAllWithJoin(
        req.getMno(),
        req.getWord(),
        req.getStatus(),
        req.getPname(),
        req.getPmonth(),
        req.getDateFrom(),
        req.getDateTo(),
        pageable
    );

    // Page.map을 통해 DTO의 from() 메서드로 가공
    return result.map(row -> {
      ShopOrder order = (ShopOrder) row[0];
      String pname = (String) row[1];
      String sname = (String) row[2];
      return ShopOrderDTO.Response.from(order, pname, sname);
    });
  }
  
  /** 회원+매장 기준 구독 내역 검색 + 페이징 조회 (매장별 구독내역) */
  @Transactional(readOnly = true)
  public Page<ShopOrderDTO.Response> searchShopOrders(ShopOrderDTO.SearchRequest req, Pageable pageable) {
    Page<Object[]> result = shopOrderRepository.searchSnoAndMno(
        req.getMno(),
        req.getSno(),
        req.getWord(),
        req.getStatus(),
        req.getPname(),
        req.getPmonth(),
        req.getDateFrom(),
        req.getDateTo(),
        pageable
        );
    
    // Page.map을 통해 DTO의 from() 메서드로 가공
    return result.map(row -> {
      ShopOrder order = (ShopOrder) row[0];
      String pname = (String) row[1];
      String sname = (String) row[2];
      return ShopOrderDTO.Response.from(order, pname, sname);
    });
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
  

  /**
   * 회원기준 (특정 매장(sno)에 연결 가능한) 구독권 목록.
   * 매장 미연결이면서 매장연결대기(0) 또는 정상(2) 상태인 구독권 중,
   * 회원 소유 매장의 CCTV 등록 대수와 일치하는 것만 필터링합니다.
   * @param mno 회원번호
   * @return 연결 가능한 구독권 목록
   */
  public List<ShopOrderDTO.Response> findLinkableOrders(Long mno, Long sno) {
    // 3. 연결 가능한 구독권을 가져와서 CCTV 개수가 일치하는 구독권만 필터링 후 DTO 변환
    return shopOrderRepository.findLinkableOrders(mno).stream()
        .filter(row -> {
          ShopOrder order = (ShopOrder) row[0];
          // 구독권의 ccnt가 Null이 아니고, 매장들의 CCTV 개수 집합(Set)에 포함되는지 검증

          System.out.println(cctvRepository.countBySno(sno));
          System.out.println(order.getCcnt());
          return order.getCcnt() != null && cctvRepository.countBySno(sno) == order.getCcnt();
        })
        .map(row -> {
          ShopOrder order = (ShopOrder) row[0];
          String pname = (String) row[1];
          return ShopOrderDTO.Response.from(order, pname, null);
        })
        .collect(Collectors.toList());
  }
  
  

  /**
   * 구독 결제 완료 후 
   * 연결 가능 매장 조회 (매장연결 화면)
   * 
   * 회원(mno) 소유 매장 중, 활성(STATUS=1) 구독이 걸려있지 않은 매장만 포함됩니다
   * — 구독이 아예 없던 매장 + 이전 구독이 만료/취소된 매장 둘 다 여기 해당합니다.
   * @param mno 회원번호
   * @return 연결 가능한 매장 목록 (CCTV 등록 대수 포함)
   */
  public List<ShopWithCctvCount> findLinkableShops(long mno) {
    List<Shop> shops = shopOrderRepository.findLinkableShops(mno);
    
    return shops.stream()
        .map(shop -> new ShopWithCctvCount(shop, cctvRepository.countBySno(shop.getNo())))
        .collect(Collectors.toList());
  }
  
  
  /** 매장 기준 구독·결제 내역 */
  public List<ShopOrderDTO.Response> findBySno(long sno) {
    return shopOrderRepository.findBySnoOrderByCdateDesc(sno)
        .stream().map(ShopOrderDTO.Response::from).collect(Collectors.toList());
  }
  
  

  // --------------------------------------------------
  // 수정
  // --------------------------------------------------
  

  /**
   * 매장 선택 확정 — 결제된 구독 내역(SNO=null)을 특정 매장에 연결합니다.
   * 연결 성공 시 STATUS를 정상(2)으로 전환합니다.
   * @param orderno 연결할 구독 내역 번호
   * @param request 연결할 매장 번호
   * @return 연결된 구독 내역
   * @throws IllegalStateException 검증 실패 시 사유가 담긴 메시지와 함께 발생
   */
  public ShopOrderDTO.Response linkShop(String no, ShopOrderDTO.LinkShopRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) {
      throw new IllegalArgumentException("존재하지 않는 구독 내역입니다.");
    }

    ShopOrder shopOrder = optional.get();

    if (shopOrder.getStatus() == STATUS_CANCELLED) {
      throw new IllegalStateException("취소된 구독은 매장에 연결할 수 없습니다.");
    }

    if (shopOrderRepository.findBySnoAndStatus(request.getSno(), STATUS_NORMAL).isPresent()) {
      throw new IllegalStateException("이미 다른 구독권이 연결된 매장입니다.");
    }

    long actualCctvCount = cctvRepository.countBySno(request.getSno());
    if (shopOrder.getCcnt() != actualCctvCount) {
      throw new IllegalStateException("결제된 CCTV 대수와 매장에 등록된 CCTV 대수가 달라 해당 매장에 구독권을 연결할 수 없습니다.");
    }
    
    LocalDate today = LocalDate.now();
    LocalDate edate = today.plusMonths(shopOrder.getPmonth());
    shopOrder.setSdate(today.toString());
    shopOrder.setEdate(edate.toString());

    shopOrder.setSno(request.getSno());
    shopOrder.setUdate(Tool.getDate());
    shopOrder.setStatus(STATUS_NORMAL); // 정상(2)으로 확정

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 1, saved.getSno(),
        null, saved.getEdate(), null, "매장 연결 확정");

    return ShopOrderDTO.Response.from(saved);
  }



  // ══════════════════════════════════════════════
  // 갱신 — 만료 7일 전부터만 가능, 만료 후 불가
  // ══════════════════════════════════════════════

  /**
   * 구독 갱신. 만료 7일 전부터 만료 시점까지만 가능하고, 만료(3) 후에는
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
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() == STATUS_CANCELLED) return null;
    if (shopOrder.getEdate() == null) return null;

    LocalDate today = LocalDate.now();
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());

    if (today.isAfter(oldEdate)) return null; // 만료 후 갱신 불가
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());
    if (daysLeft > 7) return null; // 만료 7일 전부터만 가능

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
    if (shopOrder.getStatus() == STATUS_EXPIRED) shopOrder.setStatus(STATUS_NORMAL);
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    shopPaymentService.pay(saved.getNo(), saved.getMno(), extraCharge, pmethod);

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
  // 변경 — 기간/대수
  // ══════════════════════════════════════════════

  /**
   * 구독권 변경 예상 결과 미리보기. 실제 반영은 하지 않고 계산만 합니다.
   * requestChange()와 동일한 계산식을 그대로 사용합니다.
   */

  public ShopOrderDTO.ChangePreview previewChange(String no, ShopOrderDTO.ChangeRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() != STATUS_NORMAL) return null; // 정상 상태만 변경 가능
    if (shopOrder.getEdate() == null) return null;

    Integer newPmonth = request.getPmonth() != null ? request.getPmonth() : shopOrder.getPmonth();
    Integer newCcnt = request.getCcnt() != null ? request.getCcnt() : shopOrder.getCcnt();
    boolean pmonthChanged = !newPmonth.equals(shopOrder.getPmonth());
    boolean ccntChanged = !newCcnt.equals(shopOrder.getCcnt());
    if (!pmonthChanged && !ccntChanged) return null;

    LocalDate today = LocalDate.now();
    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());

    ShopPlan currentPlan = shopPlanRepository.findByPmonthAndCcntInRange(newPmonth, newCcnt)
        .orElseThrow(() -> new IllegalStateException("해당 조합에 맞는 구독권이 없습니다."));

    long extraCharge = 0;
    long refundAmount = 0;
    String newEdate = shopOrder.getEdate();
    boolean requiresApproval = ccntChanged;

    if (pmonthChanged) {
      if (newPmonth > shopOrder.getPmonth()) {
        newEdate = ShopOrderCaculator.calcChangedEdate(shopOrder.getSdate(), newPmonth);
        extraCharge += ShopOrderCaculator.calcPeriodIncreaseCharge(
            sdate, LocalDate.parse(newEdate), currentPlan.getBprice(), shopOrder.getCcnt(), shopOrder.getTotalprice());
      } else {
        if (daysLeft < 275) {
          throw new IllegalStateException("남은 구독기간이 275일 미만이면 기간을 줄일 수 없습니다.");
        }
        newEdate = ShopOrderCaculator.calcChangedEdate(shopOrder.getSdate(), newPmonth);
        refundAmount += ShopOrderCaculator.calcPeriodDecreaseRefund(
            sdate, LocalDate.parse(newEdate), shopOrder.getBprice(), shopOrder.getCcnt(), shopOrder.getTotalprice());
      }
    }

    if (ccntChanged) {
      if (daysLeft < 28) {
        throw new IllegalStateException("구독 종료까지 28일 미만이면 대수를 변경할 수 없습니다.");
      }
      int diffCcnt = newCcnt - shopOrder.getCcnt();
      if (diffCcnt > 0) {
        extraCharge += ShopOrderCaculator.calcCcntIncreaseCharge(today, oldEdate, currentPlan.getBprice(), diffCcnt);
      }
      // 대수 감소는 승인 시점에 확정되므로 미리보기에선 예상 문구로만 안내
    }

    return ShopOrderDTO.ChangePreview.builder()
        .pname(currentPlan.getPname())
        .bprice(currentPlan.getBprice())
        .extraCharge(extraCharge)
        .refundAmount(refundAmount)
        .totalprice(Math.max(0, shopOrder.getTotalprice() + extraCharge - refundAmount))
        .edate(newEdate)
        .requiresApproval(requiresApproval)
        .build();
  }

  /**
   * 구독권 변경 신청.
   *
   *
   * 기간 변경(6→12): 기간측정 없이 즉시 반영. SDATE 기준 새 종료일 전체
   * 금액(당일 단가)에서 이미 낸 금액을 뺀 차액을 즉시 결제.
   *
   * 기간 변경(12→6): 남은기간 275일 이상이어야 가능. SDATE 기준 새 종료일
   * 전체 금액(결제시점 단가)과 이미 낸 금액의 차액을 즉시 환불(계좌 필요).
   *
   * 대수 변경: 신청일~EDATE가 28일 미만이면 불가. 
   * 전환하고 기존 PNO/CCNT 등은 그대로 유지(PENDING_*에만 신청값 저장).
   * 증가는 신청 시점에 남은기간×늘어난대수를 당일 단가로 즉시 결제.
   * 증가/감소 둘 다 승인 시점에 정산(환불)이 예정되어 있으므로 신청 시
   * 환불계좌를 미리 받아둡니다(금액은 0으로 저장, 승인 시 확정).
   */
  public ShopOrderDTO.ChangeResult requestChange(String no, ShopOrderDTO.ChangeRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() != STATUS_NORMAL) return null;
    if (shopOrder.getEdate() == null) return null;

    Integer newPmonth = request.getPmonth() != null ? request.getPmonth() : shopOrder.getPmonth();
    Integer newCcnt = request.getCcnt() != null ? request.getCcnt() : shopOrder.getCcnt();
    boolean pmonthChanged = !newPmonth.equals(shopOrder.getPmonth());
    boolean ccntChanged = !newCcnt.equals(shopOrder.getCcnt());
    if (!pmonthChanged && !ccntChanged) return null;

    LocalDate today = LocalDate.now();
    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());

    ShopPlan currentPlan = shopPlanRepository.findByPmonthAndCcntInRange(newPmonth, newCcnt)
        .orElseThrow(() -> new IllegalStateException("해당 조합에 맞는 구독권이 없습니다."));

    long periodExtraCharge = 0;
    long periodRefund = 0;
    String newEdate = shopOrder.getEdate();

    // ── 1. 기간 변경 ──────────────────────────
    if (pmonthChanged) {
      if (newPmonth > shopOrder.getPmonth()) {
        newEdate = ShopOrderCaculator.calcChangedEdate(shopOrder.getSdate(), newPmonth);
        periodExtraCharge = ShopOrderCaculator.calcPeriodIncreaseCharge(
            sdate, LocalDate.parse(newEdate), currentPlan.getBprice(), shopOrder.getCcnt(), shopOrder.getTotalprice());
      } else {
        if (daysLeft < 275) {
          throw new IllegalStateException("남은 구독기간이 275일 미만이면 기간을 줄일 수 없습니다.");
        }
        newEdate = ShopOrderCaculator.calcChangedEdate(shopOrder.getSdate(), newPmonth);
        periodRefund = ShopOrderCaculator.calcPeriodDecreaseRefund(
            sdate, LocalDate.parse(newEdate), shopOrder.getBprice(), shopOrder.getCcnt(), shopOrder.getTotalprice());
      }
    }

    // ── 2. 대수 변경 (승인 대기) ─────────────────
    if (ccntChanged) {
      if (daysLeft < 28) {
        throw new IllegalStateException("구독 종료까지 28일 미만이면 대수를 변경할 수 없습니다.");
      }

      int diffCcnt = newCcnt - shopOrder.getCcnt();

      shopOrder.setPendingPno(currentPlan.getNo());
      shopOrder.setPendingPmonth(newPmonth);
      shopOrder.setPendingCcnt(newCcnt);
      shopOrder.setPendingBprice(currentPlan.getBprice());
      shopOrder.setPendingTotalprice(null); // 승인 시점에 재계산
      shopOrder.setPendingEdate(newEdate);
      shopOrder.setUdate(Tool.getDate());

      ShopOrder saved = shopOrderRepository.save(shopOrder);

      // 증가/감소 둘 다 승인 시점에 정산(환불)이 예정되어 있으므로 신청 시 계좌를 미리 확보
      if (request.getBankName() != null) {
        shopRefundService.savePendingAccountOnly(
            saved.getNo(), saved.getMno(), request.getBankName(), request.getAccountNo(), request.getAccountHolder());
      }

      if (diffCcnt > 0) {
        long extraCharge = ShopOrderCaculator.calcCcntIncreaseCharge(today, oldEdate, currentPlan.getBprice(), diffCcnt);
        shopPaymentService.pay(saved.getNo(), saved.getMno(), extraCharge, request.getPmethod());

        shopOrderLogService.log(saved.getNo(), saved.getMno(), 4, saved.getSno(),
            shopOrder.getEdate(), newEdate, extraCharge,
            "구독권 대수 증가 신청(승인대기) · " + shopOrder.getCcnt() + "대 → " + newCcnt + "대, 신청시 즉시결제");
      } else {
        shopOrderLogService.log(saved.getNo(), saved.getMno(), 4, saved.getSno(),
            shopOrder.getEdate(), newEdate, null,
            "구독권 대수 감소 신청(승인대기) · " + shopOrder.getCcnt() + "대 → " + newCcnt + "대, 승인 시 환불 예정");
      }

      return ShopOrderDTO.ChangeResult.builder().no(saved.getNo()).pending(true).applied(null).build();
    }

    // ── 3. 기간만 변경(대수 변경 없음) — 즉시 반영 ──
    shopOrder.setPno(currentPlan.getNo());
    shopOrder.setPmonth(newPmonth);
    shopOrder.setBprice(currentPlan.getBprice());
    shopOrder.setTotalprice(shopOrder.getTotalprice() + periodExtraCharge - periodRefund);
    shopOrder.setEdate(newEdate);
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    if (periodExtraCharge > 0) {
      shopPaymentService.pay(saved.getNo(), saved.getMno(), periodExtraCharge, request.getPmethod());
    } else if (periodRefund > 0) {
      ShopPaymentDTO.Response payment = shopPaymentService.refund(saved.getNo(), saved.getMno(), periodRefund);
      ShopRefundDTO.Request refundRequest = ShopRefundDTO.Request.builder()
          .bankName(request.getBankName())
          .accountNo(request.getAccountNo())
          .accountHolder(request.getAccountHolder())
          .build();
      shopRefundService.save(saved.getNo(), payment.getNo(), saved.getMno(), refundRequest, periodRefund);
    }

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 5, saved.getSno(),
        shopOrder.getEdate(), newEdate, periodExtraCharge > 0 ? periodExtraCharge : -periodRefund,
        "구독 기간 변경(즉시 반영)");

    return ShopOrderDTO.ChangeResult.builder()
        .no(saved.getNo())
        .pending(false)
        .applied(ShopOrderDTO.Response.from(saved))
        .build();
  }

  /**
   * 관리자용 — 구독권 대수 변경 승인/반려.
   * 
   * 승인 시 실제 매장 등록 CCTV 대수와 PENDING_CCNT가 일치해야 확정됩니다.
   * 증가였던 경우: 신청일~승인일 사이는 아직 기존 대수로 서비스했으므로,
   * 그 기간만큼 이미 받은 추가금을 정산(환불)합니다.
   * 감소였던 경우: 승인일~EDATE 남은기간을 결제시점 단가 기준으로 환불합니다.
   * 두 경우 다 신청 시점에 미리 저장해둔 환불계좌(amount=0)에 확정 금액만 채웁니다.
   */

  public ShopOrderDTO.Response approveChange(String no, ShopOrderDTO.ChangeApprovalRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() == STATUS_NORMAL || shopOrder.getPendingCcnt() == null) return null;

    if (!request.isApprove()) {
      clearPendingChange(shopOrder);
      shopOrder.setUdate(Tool.getDate());
      ShopOrder saved = shopOrderRepository.save(shopOrder);

      shopOrderLogService.log(saved.getNo(), saved.getMno(), 4, saved.getSno(),
          null, null, null, "구독권 변경 신청 반려");

      return ShopOrderDTO.Response.from(saved);
    }

    if (shopOrder.getSno() != null) {
      long actualCctvCount = cctvRepository.countBySno(shopOrder.getSno());
      if (actualCctvCount != shopOrder.getPendingCcnt()) return null; // 실제 설치대수 불일치 — 승인 거부
    }

    LocalDate today = LocalDate.now();
    LocalDate requestedDate = LocalDate.parse(shopOrder.getUdate().substring(0, 10));
    LocalDate edate = LocalDate.parse(shopOrder.getEdate());

    int diffCcnt = shopOrder.getPendingCcnt() - shopOrder.getCcnt();
    long settlementAmount = 0; // 양수면 추가청구, 음수면 환불

    if (diffCcnt > 0) {
      long overCharged = ShopOrderCaculator.calcCcntIncreaseSettlement(
          requestedDate, today, shopOrder.getPendingBprice(), diffCcnt);
      settlementAmount = -overCharged;
    } else if (diffCcnt < 0) {
      long refund = ShopOrderCaculator.calcCcntDecreaseRefund(
          today, edate, shopOrder.getBprice(), -diffCcnt);
      settlementAmount = -refund;
    }

    String beforeEdate = shopOrder.getEdate();
    shopOrder.setPno(shopOrder.getPendingPno());
    shopOrder.setPmonth(shopOrder.getPendingPmonth());
    shopOrder.setCcnt(shopOrder.getPendingCcnt());
    shopOrder.setBprice(shopOrder.getPendingBprice());
    shopOrder.setEdate(shopOrder.getPendingEdate());
    shopOrder.setTotalprice(Math.max(0, shopOrder.getTotalprice() + settlementAmount));
    clearPendingChange(shopOrder);
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    if (settlementAmount < 0) {
      long refundAmount = -settlementAmount;
      shopPaymentService.refund(saved.getNo(), saved.getMno(), refundAmount);
      shopRefundService.updateAmountForOrder(saved.getNo(), refundAmount);
    }

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 5, saved.getSno(),
        beforeEdate, saved.getEdate(), settlementAmount,
        "구독권 변경 승인 완료(대수 확정, 정산 " + settlementAmount + "원)");

    return ShopOrderDTO.Response.from(saved);
  }

  private void clearPendingChange(ShopOrder shopOrder) {
    shopOrder.setPendingPno(null);
    shopOrder.setPendingPmonth(null);
    shopOrder.setPendingCcnt(null);
    shopOrder.setPendingBprice(null);
    shopOrder.setPendingTotalprice(null);
    shopOrder.setPendingEdate(null);
  }

  /** 관리자용 — CCTV 대수 변경 승인대기(STATUS=1) 목록 */
  public List<ShopOrderDTO.Response> findPendingChangeList() {
    return shopOrderRepository.findByStatusAndPendingCcntIsNotNull(STATUS_NORMAL)
        .stream().map(ShopOrderDTO.Response::from).collect(Collectors.toList());
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

    if (shopOrder.getSdate() == null) {
      // 매장 미연결 — 기간 제약 없이 전액 환불
      shopOrder.setStatus(STATUS_CANCELLED);
      shopOrder.setUdate(Tool.getDate());
      shopOrderRepository.save(shopOrder);

      long fullRefund = Math.round(shopOrder.getBprice() * shopOrder.getCcnt() * shopOrder.getPmonth());

      shopOrderLogService.log(no, shopOrder.getMno(), 3, shopOrder.getSno(),
          null, null, fullRefund, "매장 연결 전 취소 · 전액 환불");

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

    if (refundAmount > 0 && (request == null || request.getAccountNo() == null || request.getAccountNo().isBlank())) {
      return null; // 환불 대상인데 계좌 정보 없으면 거부
    }

    shopOrder.setStatus(STATUS_CANCELLED);
    shopOrder.setUdate(Tool.getDate());
    shopOrderRepository.save(shopOrder);

    double usedMonths = ShopOrderCaculator.calcUseMonths(sdate, today);
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