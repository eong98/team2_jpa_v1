package dev.jpa.allimio.shoporder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
import dev.jpa.allimio.shoprefund.ShopRefundDTO;
import dev.jpa.allimio.shoprefund.ShopRefundService;
import dev.jpa.allimio.tool.Tool;
import jakarta.persistence.Tuple;

@Service
public class ShopOrderService {
  @Autowired
  ShopOrderRepository shopOrderRepository;

  @Autowired
  CctvRepository cctvRepository;

  @Autowired
  ShopOrderLogService shopOrderLogService;

  @Autowired
  ShopPaymentService shopPaymentService;

  @Autowired
  ShopRefundService shopRefundService;

  /**
   * 신규 구독 결제 등록. 구독권(pno)·대수(ccnt)·기간(pmonth)은 여기서만 정해지고
   * 이후 변경 불가합니다 (변경하려면 취소 후 재구독). 매장(SNO)은 아직 비워두고,
   * BPRICE는 결제 시점 단가를 스냅샷으로 저장합니다.
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
        .status(0)
        .sdate(null)
        .edate(null)
        .cdate(Tool.getDate())
        .build();

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 0, null,
        null, null, saved.getTotalprice(), "신규 구독 결제");

    // TODO: 실제 결제수단 선택 UI 붙으면 request에서 pmethod 받아서 전달. 지금은 카드(0)로 고정.
    shopPaymentService.pay(saved.getNo(), saved.getMno(), saved.getTotalprice(), 0);

    return ShopOrderDTO.Response.from(saved);
  }

  /**
   * 랜덤 주문번호 발급 (형식: ORD-yyyyMMdd-6자리 랜덤숫자). 충돌 시 재발급합니다.
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

  /** 단일 조회 */
  @Transactional(readOnly = true)
  public ShopOrderDTO.Response findById(String no) {
    return shopOrderRepository.findWithJoinById(no)
        .map((Tuple tuple) -> {
          ShopOrder order = tuple.get("order", ShopOrder.class);
          String pname = tuple.get("pname", String.class);
          String sname = tuple.get("sname", String.class);

          return ShopOrderDTO.Response.from(order, pname, sname);
        })
        .orElse(null);
  }
  
  
  /** 회원 기준 구독·결제 내역 (마이페이지 요약용, 페이징 없는 단순 목록) */
  public List<ShopOrderDTO.Response> findByMno(long mno) {
    return shopOrderRepository.findByMnoOrderByCdateDesc(mno)
        .stream().map(ShopOrderDTO.Response::from).collect(Collectors.toList());
  }

  /** 매장 기준 구독·결제 내역 */
  public List<ShopOrderDTO.Response> findBySno(long sno) {
    return shopOrderRepository.findBySnoOrderByCdateDesc(sno)
        .stream().map(ShopOrderDTO.Response::from).collect(Collectors.toList());
  }

  /** 회원 기준 구독 내역 검색 + 페이징 조회 (마이 구독 목록 화면) */
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
   * 매장 선택 확정 — 결제된 구독 내역(SNO=null)을 특정 매장에 연결합니다.
   * 실패 사유를 구분해서 예외 메시지로 전달합니다:
   *  - 이미 활성 구독이 있는 매장: "이미 다른 구독권이 연결된 매장입니다."
   *  - CCTV 대수 불일치: "결제된 CCTV 대수와 매장에 등록된 CCTV 대수가 달라 해당 매장에
   *    구독권을 연결할 수 없습니다."
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

    if (shopOrder.getStatus() == 3) {
      throw new IllegalStateException("취소된 구독은 매장에 연결할 수 없습니다.");
    }

    if (shopOrderRepository.findBySnoAndStatus(request.getSno(), 1).isPresent()) {
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
    shopOrder.setStatus(1);

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 1, saved.getSno(),
        null, saved.getEdate(), null, "매장 연결 확정");

    return ShopOrderDTO.Response.from(saved);
  }

  /**
   * 구독 결제 완료 후 "연결 가능한 매장" 목록을 반환합니다.
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

  /**
   * 구독 갱신 — 순수 기간 연장 전용. 구독권/CCTV 대수는 결제 시점에 고정되어
   * 회원(mno) 소유 매장 중, 활성(STATUS=1) 구독이 걸려있지 않은 매장만 포함됩니다
   * 이 API로는 변경할 수 없습니다 (대수/플랜을 바꾸고 싶으면 취소 후 재구독 안내).
   * 시작일 기준은 "오늘"과 "기존 EDATE" 중 늦은 날짜라, 남은 기간이 있으면 자동으로
   * 이어붙습니다. 취소(status=3)된 주문, 매장 미연결(edate 없음) 주문은 갱신 대상이 아닙니다.
   * @param orderno
   * @return 갱신 결과, 대상 아니면 null
   */
  public ShopOrderDTO.RenewResult renew(String no) {

    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() == 3) return null;
    if (shopOrder.getEdate() == null) return null;

    String beforeEdate = shopOrder.getEdate();

    LocalDate today = LocalDate.now();
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    LocalDate startDate = today.isAfter(oldEdate) ? today : oldEdate;
    shopOrder.setEdate(startDate.plusMonths(shopOrder.getPmonth()).toString());

    if (shopOrder.getStatus() == 2) {
      shopOrder.setStatus(1);
    }
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    shopOrderLogService.log(saved.getNo(), saved.getMno(), 2, saved.getSno(),
        beforeEdate, saved.getEdate(), null, "구독 갱신(기간 연장)");

    return ShopOrderDTO.RenewResult.builder()
        .no(saved.getNo())
        .ccnt(saved.getCcnt())
        .totalprice(saved.getTotalprice())
        .edate(saved.getEdate())
        .build();
  }

  /**
   * 구독 취소 — 사용한 개월수(1개월 미만 올림)를 뺀 나머지 개월수만큼 환불액을 계산합니다.
   * 환불액이 0보다 크면 환불계좌 정보(request)가 필수입니다(비어있으면 취소 거부).
   * 매장 미연결(SDATE 없음) 상태면 사용한 기간이 없으므로 전액 환불 처리합니다.
   * 매장 연결(SNO)은 그대로 두고 STATUS만 취소(2) 처리합니다 — 이 매장은 이후
   * "구독권이 연결되어 있지만 만료/취소된 매장" 목록에 다시 노출되어, 재구독 시
   * 같은 매장으로 바로 연결할 수 있습니다.
   * @param orderno
   * @param request 환불계좌 정보 (환불 대상일 때만 필수)
   * @return 환불 계산 결과, 대상 없거나 계좌정보 누락 시 null
   */
  public ShopOrderDTO.CancelResult cancel(String no, ShopOrderDTO.CancelRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    long refundAmount;
    int usedMonths;
    int refundMonths;

    if (shopOrder.getSdate() == null) {
      usedMonths = 0;
      refundMonths = shopOrder.getPmonth();
      refundAmount = (long) (shopOrder.getBprice() * shopOrder.getCcnt() * shopOrder.getPmonth());
    } else {
      LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
      LocalDate today = LocalDate.now();
      long usedDays = ChronoUnit.DAYS.between(sdate, today);
      usedMonths = (int) Math.max(1, Math.ceil(usedDays / 30.0));
      refundMonths = Math.max(0, shopOrder.getPmonth() - usedMonths);
      refundAmount = (long) (shopOrder.getBprice() * shopOrder.getCcnt() * refundMonths);
    }

    // 환불 대상인데 계좌 정보가 없으면 취소 자체를 거부
    if (refundAmount > 0 && (request == null || request.getAccountNo() == null || request.getAccountNo().isBlank())) {
      return null;
    }

    shopOrder.setStatus(3);
    shopOrder.setUdate(Tool.getDate());
    shopOrderRepository.save(shopOrder);

    String memo = usedMonths == 0
        ? "매장 연결 전 취소 · 전액 환불"
        : "사용 " + usedMonths + "개월 · 환불 " + refundMonths + "개월";
    shopOrderLogService.log(no, shopOrder.getMno(), 3, shopOrder.getSno(),
        null, null, refundAmount, memo);

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
        .refundMonths(refundMonths)
        .refundAmount(refundAmount)
        .build();
  }
}