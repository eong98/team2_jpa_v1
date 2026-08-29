package dev.jpa.allimio.shoporderlog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.tool.PageResponse;

@RestController
@RequestMapping("/shop_order_log")
public class ShopOrderLogCont {
  @Autowired
  ShopOrderLogService shopOrderLogService;

  public ShopOrderLogCont() {
    System.out.println("-> ShopOrderLogCont created");
  }

  /**
   * 특정 주문의 전체 이력 (최신순)
   * GET /shop_order_log/order/ORD-20260819-000001
   */
  @GetMapping("/{ono}")
  public ResponseEntity<List<ShopOrderLogDTO.Response>> findByOno(@PathVariable("ono") String ono) {
    return ResponseEntity.ok(shopOrderLogService.findByOno(ono));
  }

  /**
   * 회원 기준 변경 이력 검색 + 페이징 조회
   * GET /shop_order_log/mno/1/search?sno=&action=&dateFrom=&dateTo=&page=0&size=10
   */
  @GetMapping("{mno}/{ono}")
  public ResponseEntity<PageResponse<ShopOrderLogDTO.Response>> search(
      @PathVariable("mno") Long mno,
      @PathVariable("ono") String ono,
      ShopOrderLogDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    searchCondition.setMno(mno);
    searchCondition.setOno(ono);
    Page<ShopOrderLogDTO.Response> pageResult = shopOrderLogService.search(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 전체 변경 이력 검색 + 페이징 조회
   * GET /shop_order_log/list/admin?mno=&sno=&action=&dateFrom=&dateTo=&page=0&size=10
   */
  @GetMapping("/list/admin")
  public ResponseEntity<PageResponse<ShopOrderLogDTO.Response>> searchAllAdmin(
      ShopOrderLogDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<ShopOrderLogDTO.Response> pageResult = shopOrderLogService.searchAllAdmin(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
}