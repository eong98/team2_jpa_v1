package dev.jpa.allimio.shoprefund;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.tool.PageResponse;

@RestController
@RequestMapping("/shop_refund")
public class ShopRefundCont {
  @Autowired
  ShopRefundService shopRefundService;

  public ShopRefundCont() {
    System.out.println("-> ShopRefundCont created");
  }

  /**
   * 특정 주문의 환불계좌 (최신순)
   * GET /shop_refund/order/ORD-20260819-000001
   */
  @GetMapping("/order/{ono}")
  public ResponseEntity<List<ShopRefundDTO.Response>> findByOno(@PathVariable("ono") String ono) {
    return ResponseEntity.ok(shopRefundService.findByOno(ono));
  }

  /**
   * 회원 기준 환불계좌 검색 + 페이징 조회
   * GET /shop_refund/mno/1/search?status=&dateFrom=&dateTo=&page=0&size=10
   */
  @GetMapping("/mno/{mno}/search")
  public ResponseEntity<PageResponse<ShopRefundDTO.Response>> search(
      @PathVariable("mno") Long mno,
      ShopRefundDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    searchCondition.setMno(mno);
    Page<ShopRefundDTO.Response> pageResult = shopRefundService.search(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 전체 환불계좌 검색 + 페이징 조회
   * GET /shop_refund/list/admin?mno=&status=&dateFrom=&dateTo=&page=0&size=10
   */
  @GetMapping("/list/admin")
  public ResponseEntity<PageResponse<ShopRefundDTO.Response>> searchAllAdmin(
      ShopRefundDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<ShopRefundDTO.Response> pageResult = shopRefundService.searchAllAdmin(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 — 환불 처리상태 변경 (실제 이체 완료 처리)
   * PUT /shop_refund/1/status
   */
  @PutMapping("/{no}/status")
  public ResponseEntity<ShopRefundDTO.Response> updateStatus(
      @PathVariable("no") Long no,
      @RequestBody ShopRefundDTO.UpdateStatusRequest request) {
    ShopRefundDTO.Response response = shopRefundService.updateStatus(no, request);
    if (response == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
  }
}