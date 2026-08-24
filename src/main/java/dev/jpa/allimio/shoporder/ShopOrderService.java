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
   * 신규 구독 결제 등록. 매장(SNO)은 아직 비워두고, BPRICE는 결제 시점 단가를
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
      orderno = "ORD-" + datePart + "-" + randomPart;
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
  
    if (shopOrderRepository.findBySnoAndStatus(request.getSno(), 0).isPresent()) {
      return null;
    }
  
    long actualCctvCount = cctvRepository.countBySno(request.getSno());
    if (shopOrder.getCcnt() != actualCctvCount) {
      return null;
    }
  
    // 매장 연결 확정 시점을 실제 구독 시작일로 확정
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
   * 매장 필터링(활성 구독 없는 매장만) 자체는 ShopOrderRepository.findLinkableShops()에서
   * JOIN 서브쿼리로 한 번에 처리하고, 여기선 CCTV 등록 대수만 붙여줍니다.
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
   * 구독 갱신/변경 — 같은 구독권 조건으로만 가능(구독권 자체 변경 불가).
   *
   * 대수 변경(newCcnt): 안 넘기거나 기존과 같으면 변경 없음. 다르면(늘리거나 줄이거나)
   * 그 차이만큼 추가결제/환불이 계산되는데, 기준이 되는 개월수(chargeMonths)가
   * extendPeriod 여부에 따라 다릅니다:
   *   - extendPeriod=true (갱신): 새로 시작되는 이용기간 전체(PMONTH) 기준으로 계산
   *   - extendPeriod=false (변경): 이미 지난 기간은 그대로 두고, 오늘부터 기존
   *     종료일(EDATE)까지 "남은 기간"만 일할 계산 (한 달 미만 자투리도 1개월로 올림)
   *
   * 기간 연장(extendPeriod): true일 때만("갱신") EDATE가 이용기간만큼 연장됩니다. 시작일
   * 기준은 "오늘"과 "기존 EDATE" 중 늦은 날짜라, 남은 기간이 있으면 자동으로 이어붙습니다.
   * false/미전달이면("변경") 대수만 바뀌고 EDATE는 그대로 유지됩니다.
   *
   * 이미 취소(status=2)된 주문, 아직 매장 연결 전(EDATE가 없는) 주문은 갱신/변경
   * 대상이 아니라 거부합니다.
   *
   * @param orderno
   * @param request
   * @return 갱신/변경 결과(변경된 대수/총액/종료일 + 추가결제/환불 금액), 실패 시 null
   */
  public ShopOrderDTO.RenewResult renew(String orderno, ShopOrderDTO.RenewRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();
    if (shopOrder.getStatus() == 2) return null; // 취소된 구독은 갱신/변경 불가
    if (shopOrder.getEdate() == null) return null; // 매장 연결 전에는 갱신/변경 대상 아님

    Integer newCcnt = request.getNewCcnt();
    boolean extendPeriod = Boolean.TRUE.equals(request.getExtendPeriod());

    // 이미 만료된 구독은 "변경"(기간 연장 없음) 대상이 아님 — 남은 기간이라는 개념 자체가
    // 없으므로, 만료 상태에서는 반드시 "갱신"(extendPeriod=true)만 허용합니다.
    if (shopOrder.getStatus() == 1 && !extendPeriod) {
      return null;
    }

    long extraCharge = 0;
    long refundAmount = 0;

    if (newCcnt != null && !newCcnt.equals(shopOrder.getCcnt())) {
      int diff = newCcnt - shopOrder.getCcnt();

      int chargeMonths = extendPeriod
          ? shopOrder.getPmonth()
          : remainingMonths(shopOrder.getEdate());

      if (diff > 0) {
        extraCharge = (long) (shopOrder.getBprice() * diff * chargeMonths);
        shopOrder.setTotalprice(shopOrder.getTotalprice() + extraCharge);
      } else {
        int reduced = -diff;
        refundAmount = (long) (shopOrder.getBprice() * reduced * chargeMonths);
        shopOrder.setTotalprice(Math.max(0, shopOrder.getTotalprice() - refundAmount));
      }

      shopOrder.setCcnt(newCcnt);
    }

    if (extendPeriod) {
      LocalDate today = LocalDate.now();
      LocalDate oldEdate = LocalDate.parse(shopOrder.getEdate());
      LocalDate startDate = today.isAfter(oldEdate) ? today : oldEdate;
      LocalDate newEdate = startDate.plusMonths(shopOrder.getPmonth());
      shopOrder.setEdate(newEdate.toString());

      if (shopOrder.getStatus() == 1) {
        shopOrder.setStatus(0);
      }
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
   * 오늘부터 종료일(edate)까지 남은 개월수를 계산합니다. 한 달 미만 자투리 기간도
   * 최소 1개월로 올림 처리합니다 (예: 20일 남았어도 1개월분으로 계산).
   * @param edate 종료일 (YYYY-MM-DD)
   * @return 남은 개월수 (최소 1)
   */
  private int remainingMonths(String edate) {
    LocalDate today = LocalDate.now();
    LocalDate end = LocalDate.parse(edate);
    if (!end.isAfter(today)) return 1;

    long days = ChronoUnit.DAYS.between(today, end);
    return (int) Math.max(1, Math.ceil(days / 30.0));
  }



  /**
   * 구독 취소 — 사용한 개월수(1개월 미만 올림)를 뺀 나머지 개월수만큼 환불액을 계산합니다.
   * 매장 연결(SNO)은 그대로 두고 STATUS만 취소(2) 처리합니다 — 이 매장은 이후
   * "구독권이 연결되어 있지만 만료/취소된 매장" 목록에 다시 노출됩니다.
   * @param orderno
   * @return 환불 계산 결과, 대상 없으면 null
   */
  public ShopOrderDTO.CancelResult cancel(String orderno, ShopOrderDTO.CancelRequest request) {
    Optional<ShopOrder> optional = shopOrderRepository.findById(orderno);
    if (optional.isEmpty()) return null;

    ShopOrder shopOrder = optional.get();

    LocalDate sdate = LocalDate.parse(shopOrder.getSdate());
    LocalDate today = LocalDate.now();
    long usedDays = ChronoUnit.DAYS.between(sdate, today);
    int usedMonths = (int) Math.max(1, Math.ceil(usedDays / 30.0));
    int refundMonths = Math.max(0, shopOrder.getPmonth() - usedMonths);
    long refundAmount = (long) (shopOrder.getBprice() * shopOrder.getCcnt() * refundMonths);

    // 환불 대상인데 계좌 정보가 없으면 처리 거부
    if (refundAmount > 0 && (request.getRefundAccount() == null
        || request.getRefundAccount().getAccountNo() == null
        || request.getRefundAccount().getAccountNo().isBlank())) {
      return null;
    }

    shopOrder.setStatus(2);
    shopOrder.setUdate(Tool.getDate());
    shopOrderRepository.save(shopOrder);

    // TODO: 실제 환불 처리(PG/이체 연동) 및 계좌정보 로그 저장은 SHOP_ORDER_LOG 등 별도 테이블 설계 후 반영 예정

    return ShopOrderDTO.CancelResult.builder()
        .orderno(orderno)
        .usedMonths(usedMonths)
        .refundMonths(refundMonths)
        .refundAmount(refundAmount)
        .build();
  }
}