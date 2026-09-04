package dev.jpa.allimio.shoporderpending;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.cctv.CctvRepository;
import dev.jpa.allimio.shoporder.ShopOrder;
import dev.jpa.allimio.shoporder.ShopOrderCaculator;
import dev.jpa.allimio.shoporder.ShopOrderRepository;
import dev.jpa.allimio.shoporderlog.ShopOrderLogService;
import dev.jpa.allimio.shoppayment.ShopPaymentDTO;
import dev.jpa.allimio.shoppayment.ShopPaymentService;
import dev.jpa.allimio.shopplan.ShopPlan;
import dev.jpa.allimio.shopplan.ShopPlanRepository;
import dev.jpa.allimio.shoprefund.ShopRefundDTO;
import dev.jpa.allimio.shoprefund.ShopRefundService;
import dev.jpa.allimio.tool.Tool;

@Service
public class ShopOrderPendingService {
  @Autowired
  ShopOrderPendingRepository shopOrderPendingRepository;

  @Autowired
  ShopOrderRepository shopOrderRepository; // 조회 전용으로만 사용 (조건 검증)

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
  // STATUS 상수 (가독성용) — 0:승인대기 / 1:승인반려 / 2:처리완료
  // ══════════════════════════════════════════════
  private static final int STATUS_WAIT_SHOP = 0;
  private static final int STATUS_REJECTED = 1;
  private static final int STATUS_DONE = 2;
  
  
  /**
   * 구독권 변경 신청. 대수 변경이 있으면 SHOP_ORDER_PENDING에 승인대기(0)
   * 기간만 변경하는 경우는 승인 절차가 필요 없으므로, 
   * 등록과 동시에 applyChange()를 호출해서 SHOP_ORDER_PENDING을 완료(2) 상태로 만들고 
   * SHOP_ORDER도 그 자리에서 즉시 확정 반영합니다.
   * 
   * 신청일~EDATE가 28일 미만이면 불가. 이미 승인대기 중인 신청이 있으면 거부.
   */
  public ShopOrderPendingDTO.ChangeResult save(ShopOrderPendingDTO.Request request) {
    ShopOrder shopOrder = shopOrderRepository.findById(request.getOno())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독 내역입니다."));

    if (shopOrder.getStatus() != 1) throw new IllegalStateException("정상 상태의 구독만 변경 신청할 수 있습니다.");
    if (shopOrder.getEdate() == null) throw new IllegalStateException("매장 미연결 구독은 변경할 수 없습니다.");

    if (shopOrderPendingRepository.existsByOnoAndStatus(request.getOno(), 0)) {
      throw new IllegalStateException("이미 승인 대기 중인 변경 신청이 있습니다.");
    }

    Integer newCcnt = request.getCcnt() != null ? request.getCcnt() : shopOrder.getCcnt();
    Integer newPmonth = request.getPmonth() != null ? request.getPmonth() : shopOrder.getPmonth();
    boolean ccntChanged = !newCcnt.equals(shopOrder.getCcnt());
    boolean pmonthChanged = !newPmonth.equals(shopOrder.getPmonth());
    if (!ccntChanged && !pmonthChanged) throw new IllegalStateException("변경 사항이 없습니다."); // 리액트에서 버튼 disabled 처리

    LocalDate today = LocalDate.now();
    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());

    String newEdate = shopOrder.getEdate();
    long periodExtraCharge = 0;
    long periodRefund = 0;

    if (pmonthChanged) {
      if (newPmonth > shopOrder.getPmonth()) {
        newEdate = ShopOrderCaculator.calcChangedEdate(shopOrder.getSdate(), newPmonth);
      } else {
        if (daysLeft < 275) throw new IllegalStateException("남은 구독기간이 275일 미만이면 기간을 줄일 수 없습니다.");
        newEdate = ShopOrderCaculator.calcChangedEdate(shopOrder.getSdate(), newPmonth);
      }
    }

    if (ccntChanged && daysLeft < 28) {
      throw new IllegalStateException("구독 종료까지 28일 미만이면 대수를 변경할 수 없습니다.");
    }

    ShopPlan targetPlan = shopPlanRepository.findByPmonthAndCcntInRange(newPmonth, newCcnt)
        .orElseThrow(() -> new IllegalStateException("해당 조합에 맞는 구독권이 없습니다."));

    // 기간변경 금액은 SDATE 기준 전체금액 재계산, 대수변경 금액은 남은기간 기준
    if (pmonthChanged) {
      long newFullValue = Math.round(targetPlan.getBprice() * shopOrder.getCcnt()
          * ShopOrderCaculator.calcUseMonths(sdate, LocalDate.parse(newEdate)));
      long diff = newFullValue - shopOrder.getTotalprice();
      if (diff > 0) periodExtraCharge = diff; else periodRefund = -diff;
    }

    // 변경 신청 데이터 저장
    ShopOrderPending pending = ShopOrderPending.builder()
        .ono(shopOrder.getNo())
        .mno(shopOrder.getMno())
        .pno(targetPlan.getNo())
        .ccnt(newCcnt)
        .bprice(targetPlan.getBprice())
        .pmonth(newPmonth)
        .edate(newEdate)
        .totalprice(shopOrder.getTotalprice() + periodExtraCharge - periodRefund)
        .status(STATUS_WAIT_SHOP)
        .cdate(Tool.getDate())
        .build();

    ShopOrderPending saved = shopOrderPendingRepository.save(pending);

    if (!ccntChanged) {
      // 대수 변경이 없으면 승인 절차 불필요 — 등록과 동시에 바로 확정 처리
      applyChange(saved, shopOrder, request, periodExtraCharge, periodRefund);
      return ShopOrderPendingDTO.ChangeResult.builder().no(saved.getNo().toString()).pending(false).build();
    }

    // 대수 변경 포함 — 승인 대기, 환불계좌 미리 확보
    if (request.getBankName() != null) {
      shopRefundService.savePendingAccountOnly(
          shopOrder.getNo(), shopOrder.getMno(), request.getBankName(), request.getAccountNo(), request.getAccountHolder());
    }

    int diffCcnt = newCcnt - shopOrder.getCcnt();
    if (diffCcnt > 0) {
      long extraCharge = ShopOrderCaculator.calcCcntIncreaseCharge(today, oldEdate, targetPlan.getBprice(), diffCcnt);
      
      shopPaymentService.pay(shopOrder.getNo(), shopOrder.getMno(), extraCharge, request.getPmethod());
      
      // 주문번호, 회원번호, 이벤트 종류(변경), 매장번호, 
      // 변경 전 종료일(NULL), 변경 후 종료일(NULL), 총결제액, 설명
      shopOrderLogService.log(shopOrder.getNo(), shopOrder.getMno(), 4, shopOrder.getSno(),
          null, null, extraCharge, "구독권 대수 증가 신청(승인대기) · 신청시 즉시결제");
    } else {
      shopOrderLogService.log(shopOrder.getNo(), shopOrder.getMno(), 4, shopOrder.getSno(),
          null, null, null, "구독권 대수 감소 신청(승인대기) · 승인 시 환불 예정");
    }

    return ShopOrderPendingDTO.ChangeResult.builder().no(saved.getNo().toString()).pending(true).build();
  }
  

/**
 * 승인 절차가 필요 없는 변경(기간만 변경)을 즉시 확정합니다.
 * SHOP_ORDER_PENDING을 완료(2)로 바꾸고, SHOP_ORDER도 그 자리에서 업데이트합니다.
 */
private void applyChange(ShopOrderPending pending, ShopOrder shopOrder, ShopOrderPendingDTO.Request request,
    long extraCharge, long refund) {

  String beforeEdate = shopOrder.getEdate();
  shopOrder.setPno(pending.getPno());
  shopOrder.setPmonth(pending.getPmonth());
  shopOrder.setBprice(pending.getBprice());
  shopOrder.setTotalprice(pending.getTotalprice());
  shopOrder.setEdate(pending.getEdate());
  shopOrder.setUdate(Tool.getDate());
  ShopOrder savedOrder = shopOrderRepository.save(shopOrder);

  pending.setStatus(STATUS_DONE);
  pending.setUdate(Tool.getDate());
  shopOrderPendingRepository.save(pending);

  if (extraCharge > 0) {
    shopPaymentService.pay(savedOrder.getNo(), savedOrder.getMno(), extraCharge, request.getPmethod());
  } else if (refund > 0) {
    ShopPaymentDTO.Response payment = shopPaymentService.refund(savedOrder.getNo(), savedOrder.getMno(), refund);
    ShopRefundDTO.Request refundRequest = ShopRefundDTO.Request.builder()
        .bankName(request.getBankName())
        .accountNo(request.getAccountNo())
        .accountHolder(request.getAccountHolder())
        .build();
    shopRefundService.save(savedOrder.getNo(), payment.getNo(), savedOrder.getMno(), refundRequest, refund);
  }

  shopOrderLogService.log(savedOrder.getNo(), savedOrder.getMno(), 5, savedOrder.getSno(),
      beforeEdate, savedOrder.getEdate(), extraCharge > 0 ? extraCharge : -refund,
      "구독 기간 변경(즉시 반영)");
}

  /** 변경 예상 결과 미리보기 (실제 반영 없음) */
  public ShopOrderPendingDTO.ChangePreview previewChange(ShopOrderPendingDTO.Request request) {
    ShopOrder shopOrder = shopOrderRepository.findById(request.getOno())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독 내역입니다."));

    Integer newCcnt = request.getCcnt();
    Integer newPmonth = request.getPmonth() != null ? request.getPmonth() : shopOrder.getPmonth();

    LocalDate today = LocalDate.now();
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    long daysLeft = ShopOrderCaculator.daysUntil(today, shopOrder.getEdate());
    if (daysLeft < 28) {
      throw new IllegalStateException("구독 종료까지 28일 미만이면 대수를 변경할 수 없습니다.");
    }

    ShopPlan targetPlan = shopPlanRepository.findByPmonthAndCcntInRange(newPmonth, newCcnt)
        .orElseThrow(() -> new IllegalStateException("해당 조합에 맞는 구독권이 없습니다."));

    int diffCcnt = newCcnt - shopOrder.getCcnt();
    long extraCharge = diffCcnt > 0
        ? ShopOrderCaculator.calcCcntIncreaseCharge(today, oldEdate, targetPlan.getBprice(), diffCcnt)
        : 0;

    return ShopOrderPendingDTO.ChangePreview.builder()
        .pname(targetPlan.getPname())
        .bprice(targetPlan.getBprice())
        .extraCharge(extraCharge)
        .refundAmount(0L) // 감소분은 승인 시점에 확정되므로 미리보기에선 0
        .edate(shopOrder.getEdate())
        .build();
  }

  /**
   * 관리자용 승인/반려. 
   * 승인 시에만 SHOP_ORDER를 확정 업데이트합니다.
   */
  public ShopOrderPendingDTO.Response approveChange(Long pendingNo, ShopOrderPendingDTO.ApprovalRequest request) {
    ShopOrderPending pending = shopOrderPendingRepository.findById(pendingNo)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 변경 신청입니다."));

    if (pending.getStatus() != STATUS_WAIT_SHOP) return null;

    ShopOrder shopOrder = shopOrderRepository.findById(pending.getOno())
        .orElseThrow(() -> new IllegalStateException("연결된 구독 내역을 찾을 수 없습니다."));

    if (!request.isApprove()) {
      pending.setStatus(STATUS_REJECTED);
      pending.setMemo(request.getMemo());
      pending.setUdate(Tool.getDate());
      ShopOrderPending saved = shopOrderPendingRepository.save(pending);

      shopOrderLogService.log(shopOrder.getNo(), shopOrder.getMno(), 4, shopOrder.getSno(),
          null, null, null, "구독권 변경 신청 반려");

      return ShopOrderPendingDTO.Response.from(saved);
    }

    if (shopOrder.getSno() != null) {
      long actualCctvCount = cctvRepository.countBySno(shopOrder.getSno());
      if (actualCctvCount != pending.getCcnt()) {
        throw new IllegalStateException("매장 CCTV 대수와 신청된 CCTV 대수가 일치하지 않습니다.");
      }
    }

    LocalDate today = LocalDate.now();
    LocalDate requestedDate = LocalDate.parse(pending.getCdate().substring(0, 10));
    LocalDate edate = LocalDate.parse(shopOrder.getEdate());

    int diffCcnt = pending.getCcnt() - shopOrder.getCcnt();
    long settlementAmount = 0;

    if (diffCcnt > 0) {
      long overCharged = ShopOrderCaculator.calcCcntIncreaseSettlement(requestedDate, today, pending.getBprice(), diffCcnt);
      settlementAmount = -overCharged;
    } else if (diffCcnt < 0) {
      long refund = ShopOrderCaculator.calcCcntDecreaseRefund(today, edate, shopOrder.getBprice(), -diffCcnt);
      settlementAmount = -refund;
    }

    String beforeEdate = shopOrder.getEdate();
    shopOrder.setPno(pending.getPno());
    shopOrder.setPmonth(pending.getPmonth());
    shopOrder.setCcnt(pending.getCcnt());
    shopOrder.setBprice(pending.getBprice());
    shopOrder.setTotalprice(Math.max(0, shopOrder.getTotalprice() + settlementAmount));
    shopOrder.setUdate(Tool.getDate());
    ShopOrder savedOrder = shopOrderRepository.save(shopOrder);

    pending.setStatus(STATUS_DONE);
    pending.setTotalprice(savedOrder.getTotalprice());
    pending.setUdate(Tool.getDate());
    ShopOrderPending savedPending = shopOrderPendingRepository.save(pending);

    if (settlementAmount < 0) {
      long refundAmount = -settlementAmount;
      shopPaymentService.refund(savedOrder.getNo(), savedOrder.getMno(), refundAmount);
      shopRefundService.updateAmountForOrder(savedOrder.getNo(), refundAmount);
    }

    shopOrderLogService.log(savedOrder.getNo(), savedOrder.getMno(), 5, savedOrder.getSno(),
        beforeEdate, savedOrder.getEdate(), settlementAmount,
        "구독권 변경 승인 완료(대수 확정, 정산 " + settlementAmount + "원)");

    return ShopOrderPendingDTO.Response.from(savedPending);
  }

  /**
   * 관리자 변경내역 조회
   * @param status
   * @return
   */
  @Transactional(readOnly = true)
  public Page<ShopOrderPendingDTO.Response> searchPending(ShopOrderPendingDTO.SearchRequest req, Pageable pageable) {
    Page<Object[]> res = shopOrderPendingRepository.findByStatusWithJoinSearch(req.getStatus(), req.getWord(), req.getDateFrom(), req.getDateTo(), pageable);
    
    return res.map(row -> {
          ShopOrderPending pending = (ShopOrderPending) row[0];
          String pname = (String) row[1];
          String sname = (String) row[2];
          Integer minCcnt = (Integer) row[3];
          Integer maxCcnt = (Integer) row[4];
          return ShopOrderPendingDTO.Response.from(pending, pname, sname, minCcnt, maxCcnt);
        });
  }
}