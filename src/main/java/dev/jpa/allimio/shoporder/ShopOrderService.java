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

import dev.jpa.allimio.cctv.CctvRepository;
import dev.jpa.allimio.shop.Shop;
import dev.jpa.allimio.shop.ShopWithCctvCount;
import dev.jpa.allimio.tool.Tool;

@Service
public class ShopOrderService {
  @Autowired
  ShopOrderRepository shopOrderRepository;

  @Autowired
  CctvRepository cctvRepository;

  /**
   * 신규 구독 결제 등록. 구독권(pno)·대수(ccnt)·기간(pmonth)은 여기서만 정해지고
   * 이후 변경 불가합니다 (변경하려면 취소 후 재구독). 매장(SNO)은 아직 비워두고,
   * BPRICE는 결제 시점 단가를 스냅샷으로 저장합니다.
   * @param request
   * @return
   */
  public ShopOrderDTO.Response save(ShopOrderDTO.Request request) {
    ShopOrder shopOrder = ShopOrder.builder()
        .orderno(generateOrderNo())
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
    return ShopOrderDTO.Response.from(saved);
  }

  /**
   * 랜덤 주문번호 발급 (형식: ORD-yyyyMMdd-6자리 랜덤숫자). 충돌 시 재발급합니다.
   * @return
   */
  private String generateOrderNo() {
    String datePart = Tool.getDate().substring(0, 10).replace("-", "");
    String orderno;
    do {
      String randomPart = String.format("%06d", new Random().nextInt(1000000));
      orderno = "ORD-" + datePart + randomPart;
    } while (shopOrderRepository.existsById(orderno));
    return orderno;
  }

  /** 단일 조회 */
  public ShopOrderDTO.Response findById(String orderno) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    return optional.map(ShopOrderDTO.Response::from).orElse(null);
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
  public Page<ShopOrderDTO.Response> search(ShopOrderDTO.SearchRequest c, Pageable pageable) {
    Page<ShopOrder> result = shopOrderRepository.searchByMno(
        c.getMno(), c.getWord(), c.getStatus(), c.getPno(), c.getSno(),
        c.getDateType(), c.getDateFrom(), c.getDateTo(),
        pageable);

    return result.map(ShopOrderDTO.Response::from);
  }

  /**
   * 관리자용 전체(또는 특정 회원) 구독 내역 검색 + 페이징 조회
   * @param c 검색조건 (mno 선택사항 — null이면 전체 회원 대상)
   */
  public Page<ShopOrderDTO.Response> searchAllAdmin(ShopOrderDTO.SearchRequest c, Pageable pageable) {
    Page<ShopOrder> result = shopOrderRepository.searchAllAdmin(
        c.getMno(), c.getWord(), c.getStatus(), c.getPno(), c.getSno(),
        c.getDateType(), c.getDateFrom(), c.getDateTo(),
        pageable);

    return result.map(ShopOrderDTO.Response::from);
  }

  /**
   * 매장 선택 확정 — 결제된 구독 내역(SNO=null)을 특정 매장에 연결합니다.
   * 실패 사유를 구분해서 예외 메시지로 전달합니다:
   *  - 이미 활성 구독이 있는 매장: "이미 구독권이 연결된 매장입니다."
   *  - CCTV 대수 불일치: "결제된 CCTV 대수와 매장에 등록된 CCTV 대수가 달라 해당 매장에
   *    구독권을 연결할 수 없습니다."
   * @param orderno 연결할 구독 내역 번호
   * @param request 연결할 매장 번호
   * @return 연결된 구독 내역
   * @throws IllegalStateException 검증 실패 시 사유가 담긴 메시지와 함께 발생
   */
  public ShopOrderDTO.Response linkShop(String orderno, ShopOrderDTO.LinkShopRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) {
      throw new IllegalArgumentException("존재하지 않는 구독 내역입니다.");
    }

    ShopOrder shopOrder = optional.get();

    if (shopOrder.getStatus() == 2) {
      throw new IllegalStateException("취소된 구독은 매장에 연결할 수 없습니다.");
    }

    if (shopOrderRepository.findBySnoAndStatus(request.getSno(), 0).isPresent()) {
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

    ShopOrder saved = shopOrderRepository.save(shopOrder);
    return ShopOrderDTO.Response.from(saved);
  }

  /**
   * 구독 결제 완료 후 "연결 가능한 매장" 목록을 반환합니다.
   * 회원(mno) 소유 매장 중, 활성(STATUS=0) 구독이 걸려있지 않은 매장만 포함됩니다
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
   * 이 API로는 변경할 수 없습니다 (대수/플랜을 바꾸고 싶으면 취소 후 재구독 안내).
   * 시작일 기준은 "오늘"과 "기존 EDATE" 중 늦은 날짜라, 남은 기간이 있으면 자동으로
   * 이어붙습니다. 취소(status=2)된 주문, 매장 미연결(edate 없음) 주문은 갱신 대상이 아닙니다.
   * @param orderno
   * @return 갱신 결과, 대상 아니면 null
   */
  public ShopOrderDTO.RenewResult renew(String orderno) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() == 2) return null;
    if (shopOrder.getEdate() == null) return null;

    LocalDate today = LocalDate.now();
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    LocalDate startDate = today.isAfter(oldEdate) ? today : oldEdate;
    shopOrder.setEdate(startDate.plusMonths(shopOrder.getPmonth()).toString());

    if (shopOrder.getStatus() == 1) {
      shopOrder.setStatus(0);
    }
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);

    return ShopOrderDTO.RenewResult.builder()
        .orderno(saved.getOrderno())
        .ccnt(saved.getCcnt())
        .totalprice(saved.getTotalprice())
        .edate(saved.getEdate())
        .build();
  }

  /**
   * 구독 취소 — 사용한 개월수(1개월 미만 올림)를 뺀 나머지 개월수만큼 환불액을 계산합니다.
   * 매장 미연결(SDATE 없음) 상태면 사용한 기간이 없으므로 전액 환불 처리합니다.
   * 매장 연결(SNO)은 그대로 두고 STATUS만 취소(2) 처리합니다 — 이 매장은 이후
   * "구독권이 연결되어 있지만 만료/취소된 매장" 목록에 다시 노출되어, 재구독 시
   * 같은 매장으로 바로 연결할 수 있습니다.
   * @param orderno
   * @return 환불 계산 결과, 대상 없으면 null
   */
  public ShopOrderDTO.CancelResult cancel(String orderno) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    if (shopOrder.getSdate() == null) {
      shopOrder.setStatus(2);
      shopOrder.setUdate(Tool.getDate());
      shopOrderRepository.save(shopOrder);

      long fullRefund = (long) (shopOrder.getBprice() * shopOrder.getCcnt() * shopOrder.getPmonth());
      return ShopOrderDTO.CancelResult.builder()
          .orderno(orderno)
          .usedMonths(0)
          .refundMonths(shopOrder.getPmonth())
          .refundAmount(fullRefund)
          .build();
    }

    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    LocalDate today = LocalDate.now();
    long usedDays = ChronoUnit.DAYS.between(sdate, today);
    int usedMonths = (int) Math.max(1, Math.ceil(usedDays / 30.0));
    int refundMonths = Math.max(0, shopOrder.getPmonth() - usedMonths);
    long refundAmount = (long) (shopOrder.getBprice() * shopOrder.getCcnt() * refundMonths);

    shopOrder.setStatus(2);
    shopOrder.setUdate(Tool.getDate());
    shopOrderRepository.save(shopOrder);

    return ShopOrderDTO.CancelResult.builder()
        .orderno(orderno)
        .usedMonths(usedMonths)
        .refundMonths(refundMonths)
        .refundAmount(refundAmount)
        .build();
  }
}