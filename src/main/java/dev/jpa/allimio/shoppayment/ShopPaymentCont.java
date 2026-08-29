package dev.jpa.allimio.shoppayment;

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
@RequestMapping("/shop_payment")
public class ShopPaymentCont {
  @Autowired
  ShopPaymentService shopPaymentService;

  public ShopPaymentCont() {
    System.out.println("-> ShopPaymentCont created");
  }

  /**
   * 결제 단건 조회 (PK 기준). 목록에서 특정 결제 건 상세로 바로 들어갈 때 사용.
   * GET /shop_payment/1
   */
  @GetMapping("/{no}")
  public ResponseEntity<ShopPaymentDTO.Response> findById(@PathVariable("no") Long no) {
    ShopPaymentDTO.Response response = shopPaymentService.findById(no);
    if (response == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
  }
  
  /**
   * 특정 주문의 결제 내역 (최신순)
   * GET /shop_payment/order/ORD-20260819-000001
   */
  @GetMapping("/order/{ono}")
  public ResponseEntity<List<ShopPaymentDTO.Response>> findByOno(@PathVariable("ono") String ono) {
    return ResponseEntity.ok(shopPaymentService.findByOno(ono));
  }

  /**
   * 회원 기준 결제 내역 검색 + 페이징 조회
   * GET /shop_payment/mno/1/search?pmethod=&pstatus=&dateFrom=&dateTo=&page=0&size=10
   */
  @GetMapping("/{mno}/{ono}")
  public ResponseEntity<PageResponse<ShopPaymentDTO.Response>> search(
      @PathVariable("mno") Long mno,
      @PathVariable("ono") String ono,
      ShopPaymentDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    searchCondition.setMno(mno);
    searchCondition.setOno(ono);
    Page<ShopPaymentDTO.Response> pageResult = shopPaymentService.search(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 전체 결제 내역 검색 + 페이징 조회
   * GET /shop_payment/list/admin?mno=&pmethod=&pstatus=&dateFrom=&dateTo=&page=0&size=10
   */
//  @GetMapping("/list/admin")
//  public ResponseEntity<PageResponse<ShopPaymentDTO.Response>> searchAllAdmin(
//      ShopPaymentDTO.SearchRequest searchCondition,
//      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {
//
//    Page<ShopPaymentDTO.Response> pageResult = shopPaymentService.searchAllAdmin(searchCondition, pageable);
//    return ResponseEntity.ok(PageResponse.of(pageResult));
//  }
}