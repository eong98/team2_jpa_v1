package dev.jpa.allimio.shoporder;

import java.util.List;

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
   * 신규 구독 결제 등록 (매장 미연결 상태로 생성)
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
  @GetMapping("/{orderno}")
  public ResponseEntity<ShopOrderDTO.Response> findById(@PathVariable("orderno") String orderno) {
    ShopOrderDTO.Response response = shopOrderService.findById(orderno);
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

    searchCondition.setMno(mno); // URL의 mno로 강제 세팅 — 본인 데이터만 조회되도록 위조 방지
    Page<ShopOrderDTO.Response> pageResult = shopOrderService.search(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 구독 내역 전체 검색 + 페이징 조회. mno를 넘기면 특정 회원만,
   * 안 넘기면 전체 회원 대상으로 조회됩니다.
   * GET /shop_order/list/admin?mno=1&word=ORD&status=0&dateType=cdate&dateFrom=2026-08-01&dateTo=2026-08-31&page=0&size=10
   */
  @GetMapping("/list/admin")
  public ResponseEntity<PageResponse<ShopOrderDTO.Response>> searchAllAdmin(
      ShopOrderDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<ShopOrderDTO.Response> pageResult = shopOrderService.searchAllAdmin(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 회원 기준 목록 (페이징 없는 단순 목록, 마이페이지 요약 등에서 사용)
   * GET /shop_order/mno/1
   */
  @GetMapping("/mno/{mno}")
  public ResponseEntity<List<ShopOrderDTO.Response>> findByMno(@PathVariable("mno") long mno) {
    return ResponseEntity.ok(shopOrderService.findByMno(mno));
  }

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
   * 구독 갱신/변경 — extendPeriod=true면 기간 연장(갱신), false/미전달이면 대수만 변경.
   * PUT /shop_order/ORD-20260819-000001/renew
   */
  @PutMapping("/{orderno}/renew")
  public ResponseEntity<ShopOrderDTO.RenewResult> renew(
      @PathVariable("orderno") String orderno,
      @RequestBody ShopOrderDTO.RenewRequest request) {
    ShopOrderDTO.RenewResult result = shopOrderService.renew(orderno, request);
    if (result == null) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(result);
  }

  /**
   * 구독 취소 (환불액 계산 포함, 환불 대상이면 계좌 정보 필수)
   * PUT /shop_order/ORD-20260819-000001/cancel
   */
  @PutMapping("/{orderno}/cancel")
  public ResponseEntity<ShopOrderDTO.CancelResult> cancel(
      @PathVariable("orderno") String orderno,
      @RequestBody ShopOrderDTO.CancelRequest request) {
    ShopOrderDTO.CancelResult result = shopOrderService.cancel(orderno, request);
    if (result == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(result);
  }
}