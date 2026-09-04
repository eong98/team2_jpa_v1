package dev.jpa.allimio.shoporder;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.shop.ShopWithCctvCount;
import dev.jpa.allimio.tool.PageResponse;

@RestController
@RequestMapping("/shop_order")
public class ShopOrderCont {
  @Autowired
  ShopOrderService shopOrderService;

  public ShopOrderCont() {
    System.out.println("-> ShopOrderCont created");
  }

  /**
   * 신규 구독 결제 등록 (매장 미연결 상태로 생성). 
   * POST /shop_order
   */
  @PostMapping
  public ResponseEntity<ShopOrderDTO.Response> save(@RequestBody ShopOrderDTO.Request request) {
    ShopOrderDTO.Response response = shopOrderService.save(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 단건 조회
   * GET /shop_order/ORD-20260819-000001
   */
  @GetMapping("/{no}")
  public ResponseEntity<ShopOrderDTO.Response> findById(@PathVariable("no") String no) {
    ShopOrderDTO.Response response = shopOrderService.findById(no);
    if (response == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
  }

  /**
   * 내 구독 내역 검색 + 페이징 조회
   * GET /shop_order/mno/1/search?word=ORD&status=0&pno=2&sno=5&dateType=cdate&dateFrom=2026-08-01&dateTo=2026-08-31&page=0&size=10
   */
  @GetMapping("/mno/{mno}/search")
  public ResponseEntity<PageResponse<ShopOrderDTO.Response>> search(
      @PathVariable("mno") Long mno,
      ShopOrderDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    searchCondition.setMno(mno);
    Page<ShopOrderDTO.Response> pageResult = shopOrderService.searchOrders(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
  
  /**
   * 매장별 구독 내역 검색 + 페이징 조회
   * GET /shop_order/1/1/...
   */
  @GetMapping("/{mno}/{sno}")
  public ResponseEntity<PageResponse<ShopOrderDTO.Response>> searchShopOrders(
      @PathVariable("mno") Long mno,
      @PathVariable("sno") Long sno,
      ShopOrderDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {
    
    searchCondition.setMno(mno);
    Page<ShopOrderDTO.Response> pageResult = shopOrderService.searchShopOrders(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 구독 내역 전체 검색 + 페이징 조회. mno를 넘기면 특정 회원만,
   * 안 넘기면 전체 회원 대상으로 조회됩니다.
   * GET /shop_order/list/admin?mno=1&word=ORD&status=0&dateType=cdate&dateFrom=2026-08-01&dateTo=2026-08-31&page=0&size=10
   */
//  @GetMapping("/list/admin")
//  public ResponseEntity<PageResponse<ShopOrderDTO.Response>> searchAllAdmin(
//      ShopOrderDTO.SearchRequest searchCondition,
//      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {
//
//    Page<ShopOrderDTO.Response> pageResult = shopOrderService.searchAllAdmin(searchCondition, pageable);
//    return ResponseEntity.ok(PageResponse.of(pageResult));
//  }

//  /**
//   * 회원 기준 목록 (페이징 없는 단순 목록, 마이페이지 요약 등에서 사용)
//   * GET /shop_order/mno/1
//   */
//  @GetMapping("/mno/{mno}")
//  public ResponseEntity<List<ShopOrderDTO.Response>> findByMno(@PathVariable("mno") long mno) {
//    return ResponseEntity.ok(shopOrderService.findByMno(mno));
//  }
//
  /**
   * 매장 기준 목록
   * GET /shop_order/sno/1
   */
  @GetMapping("/sno/{sno}")
  public ResponseEntity<List<ShopOrderDTO.Response>> findBySno(@PathVariable("sno") long sno) {
    return ResponseEntity.ok(shopOrderService.findBySno(sno));
  }

  /**
   * 구독 결제 완료 후 연결 가능한 매장 목록 (무구독 매장 + 만료/취소된 구독이 걸린 매장)
   * GET /shop_order/linkable-shops/1
   */
  @GetMapping("/linkable-shops/{mno}")
  public ResponseEntity<List<ShopWithCctvCount>> findLinkableShops(@PathVariable("mno") long mno) {
    return ResponseEntity.ok(shopOrderService.findLinkableShops(mno));
  }
  /**
   * 특정 매장에 연결 가능한 구독권 목록 (매장 CCTV 대수와 일치하는 것만)
   * GET /shop_order/linkable-plans/{mno}/{sno}
   */
  @GetMapping("/linkable-plans/{mno}/{sno}")
  public ResponseEntity<List<ShopOrderDTO.Response>> findLinkableOrders(@PathVariable("mno") Long mno, @PathVariable("sno") Long sno) {
    return ResponseEntity.ok(shopOrderService.findLinkableOrders(mno, sno));
  }
  
  
  

  /**
   * 매장 선택 확정 (SNO 연결). 실패 사유를 구분해서 응답합니다:
   *  - 404: 존재하지 않는 구독 내역
   *  - 409(Conflict): 이미 연결된 매장 / 취소된 구독
   *  - 422(Unprocessable Entity): CCTV 대수 불일치 → 프론트에서 이 코드로 전용 문구 표시
   * PUT /shop_order/ORD-20260819-000001/link-shop
   */
  @PutMapping("/{no}/link-shop")
  public ResponseEntity<?> linkShop(
      @PathVariable("no") String no,
      @RequestBody ShopOrderDTO.LinkShopRequest request) {
    try {
      ShopOrderDTO.Response response = shopOrderService.linkShop(no, request);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    } catch (IllegalStateException e) {
      HttpStatus status = e.getMessage().contains("CCTV 대수")
          ? HttpStatus.UNPROCESSABLE_ENTITY
          : HttpStatus.CONFLICT;
      return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
    }
  }

  /**
   * 구독 갱신 — 순수 기간 연장 전용 (대수/플랜 변경 없음)
   * PUT /shop_order/ORD-20260819-000001/renew
   */
  @PutMapping("/{no}/renew")
  public ResponseEntity<ShopOrderDTO.RenewResult> renew(
      @PathVariable("no") String no,
      @RequestParam(required = false) Integer newPmonth,
      @RequestParam(required = false) Integer pmethod
      ) {
    ShopOrderDTO.RenewResult result = shopOrderService.renew(no, newPmonth, pmethod);
    if (result == null) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(result);
  }

  /**
   * 구독 취소 (환불액 계산 포함, 환불 대상이면 계좌 정보 필수)
   * PUT /shop_order/ORD-20260819-000001/cancel
   */
  @PutMapping("/{no}/cancel")
  public ResponseEntity<ShopOrderDTO.CancelResult> cancel(
      @PathVariable("no") String no,
      @RequestBody(required = false) ShopOrderDTO.CancelRequest request) {
    ShopOrderDTO.CancelResult result = shopOrderService.cancel(no, request);
    if (result == null) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(result);
  }
  
  
  
}