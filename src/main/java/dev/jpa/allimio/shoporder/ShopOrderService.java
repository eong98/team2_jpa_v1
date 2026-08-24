package dev.jpa.allimio.shoporder;

import dev.jpa.allimio.cctv.CctvRepository;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.shop.Shop;
import dev.jpa.allimio.shop.ShopRepository;
import dev.jpa.allimio.shop.ShopWithCctvCount;
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

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopOrderService {
  @Autowired
  ShopOrderRepository shopOrderRepository;
  
  @Autowired
  CctvRepository cctvRepository;
  
  @Autowired
  MemberRepository memberRepository;

  /**
   * 신규 구독 결제 등록. 
   * 매장(SNO)은 아직 비워두고, BPRICE는 결제 시점 단가를
   * 그대로 스냅샷으로 저장합니다(이후 SHOP_PLAN.BPRICE가 바뀌어도 영향 없음).
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
        .sdate(request.getSdate())
        .edate(request.getEdate())
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
      orderno = "ORD-" + datePart + "-" + randomPart;
    } while (shopOrderRepository.existsById(orderno));
    return orderno;
  }

  /**
   * 단일 조회
   * @param orderno
   * @return
   */
  public ShopOrderDTO.Response findById(String orderno) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    return optional.map(ShopOrderDTO.Response::from).orElse(null);
  }

  /**
   * 회원 기준 구독·결제 내역 (마이페이지)
   * @param mno
   * @return
   */
  public List<ShopOrderDTO.Response> findByMno(long mno) {
    return shopOrderRepository.findByMnoOrderByCdateDesc(mno)
        .stream().map(ShopOrderDTO.Response::from).collect(Collectors.toList());
  }

  /**
   * 매장 기준 구독·결제 내역
   * @param sno
   * @return
   */
  public List<ShopOrderDTO.Response> findBySno(long sno) {
    return shopOrderRepository.findBySnoOrderByCdateDesc(sno)
        .stream().map(ShopOrderDTO.Response::from).collect(Collectors.toList());
  }
  
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
   * @param pageable
   * @return
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
   * 그 매장에 이미 활성(정상) 구독이 걸려있으면 거부하고, 매장에 실제 등록된
   * CCTV 대수(CctvRepository로 서버에서 직접 계산)와 결제시 지정한 CCTV 대수가
   * 다르면 거부합니다.
   * @param orderno 연결할 구독 내역 번호
   * @param request 연결할 매장 번호
   * @return 연결된 구독 내역, 검증 실패 시 null
   */
  public ShopOrderDTO.Response linkShop(String orderno, ShopOrderDTO.LinkShopRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    // 매장에 이미 활성 구독이 있으면 연결 불가
    if (shopOrderRepository.findBySnoAndStatus(request.getSno(), 0).isPresent()) {
      return null;
    }

    // 매장에 실제 등록된 CCTV 대수와 결제시 지정한 대수가 다르면 연결 불가
    long actualCctvCount = cctvRepository.countBySno(request.getSno());
    if (shopOrder.getCcnt() != actualCctvCount) {
      return null;
    }

    shopOrder.setSno(request.getSno());
    shopOrder.setUdate(Tool.getDate());

    ShopOrder saved = shopOrderRepository.save(shopOrder);
    return ShopOrderDTO.Response.from(saved);
  }
  
  /**
   * 구독 결제 완료 후 "연결 가능한 매장" 목록을 반환합니다.
   * 매장 필터링(활성 구독 없는 매장만) 자체는 ShopRepository.findLinkableShops()에서
   * JOIN 서브쿼리로 한 번에 처리하고, 여기선 CCTV 등록 대수만 붙여줍니다.
   * @param mno 회원번호
   * @return 연결 가능한 매장 목록 (CCTV 등록 대수 포함)
   */
  public List<ShopWithCctvCount> findLinkableShops(long mno) {
    List<Shop> shops = shopOrderRepository.findLinkableShops(mno); // shopRepository → shopOrderRepository로 변경

    return shops.stream()
        .map(shop -> new ShopWithCctvCount(shop, cctvRepository.countBySno(shop.getNo())))
        .collect(Collectors.toList());
  }
  
  /**
   * 구독 갱신 — 같은 구독권 조건으로만 가능(구독권 자체 변경 불가).
   * newCcnt를 안 넘기거나 기존과 같으면 대수 변경 없이 EDATE만 이용기간(PMONTH)만큼
   * 연장합니다. newCcnt가 더 크면(대수 증가) 늘어난 대수만큼 이용기간 전체 단가로
   * 추가 결제하고, newCcnt가 더 작으면(대수 감소) 줄어든 대수만큼 이용기간 전체
   * 단가로 환불합니다. 시작일 기준은 "오늘"과 "기존 EDATE" 중 늦은 날짜라, 남은
   * 기간이 있으면 자동으로 이어붙습니다.
   * @param orderno
   * @param request
   * @return 갱신 결과(변경된 대수/총액/종료일 + 추가결제/환불 금액)
   */
  public ShopOrderDTO.RenewResult renew(String orderno, ShopOrderDTO.RenewRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    Integer newCcnt = request.getNewCcnt();
    long extraCharge = 0;
    long refundAmount = 0;

    if (newCcnt != null && !newCcnt.equals(shopOrder.getCcnt())) {
      int diff = newCcnt - shopOrder.getCcnt();

      if (diff > 0) {
        extraCharge = (long) (shopOrder.getBprice() * diff * shopOrder.getPmonth());
        shopOrder.setTotalprice(shopOrder.getTotalprice() + extraCharge);
      } else {
        int reduced = -diff;
        refundAmount = (long) (shopOrder.getBprice() * reduced * shopOrder.getPmonth());
        shopOrder.setTotalprice(Math.max(0, shopOrder.getTotalprice() - refundAmount));
      }

      shopOrder.setCcnt(newCcnt);
    }

    LocalDate today = LocalDate.now();
    LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
    LocalDate startDate = today.isAfter(oldEdate) ? today : oldEdate;
    LocalDate newEdate = startDate.plusMonths(shopOrder.getPmonth());
    shopOrder.setEdate(newEdate.toString());

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
        .extraCharge(extraCharge)
        .refundAmount(refundAmount)
        .build();
  }

  /**
   * 구독 취소 — 사용한 개월수(1개월 미만 올림)를 뺀 나머지 개월수만큼 환불액을 계산합니다.
   * 매장 연결(SNO)은 그대로 두고 STATUS만 취소(2) 처리합니다 — 이 매장은 이후
   * "구독권이 연결되어 있지만 만료/취소된 매장" 목록에 다시 노출됩니다.
   * @param orderno
   * @return 환불 계산 결과, 대상 없으면 null
   */
  public ShopOrderDTO.CancelResult cancel(String orderno) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    LocalDate today = LocalDate.now();
    long usedDays = ChronoUnit.DAYS.between(sdate, today);
    int usedMonths = (int) Math.max(1, Math.ceil(usedDays / 30.0)); // 1개월 미만도 1개월로 올림
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