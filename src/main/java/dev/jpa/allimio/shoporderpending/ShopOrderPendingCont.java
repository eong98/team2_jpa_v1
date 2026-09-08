package dev.jpa.allimio.shoporderpending;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.shoporder.ShopOrderDTO;
import dev.jpa.allimio.tool.PageResponse;

@RestController
@RequestMapping("/shop_order_pending")
public class ShopOrderPendingCont {
  @Autowired
  ShopOrderPendingService shopOrderPendingService;
  
  public ShopOrderPendingCont() {
    System.out.println("-> ShopOrderPendingCont created");
  }
  
  
  /**
   * 특정 주문번호의 변경 내역 상세조회
   * 
   * GET /shop_order_pending/ORD-20260819-000001
   * @param ono
   * @return
   */
  @GetMapping("/{ono}")
  public ResponseEntity<ShopOrderPendingDTO.Response> findByOno(@PathVariable("ono") String ono) {
    ShopOrderPendingDTO.Response res = shopOrderPendingService.findByOno(ono);
    if (res == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(res);
  }
  
  
  /**
   * 구독권 변경 신청 (기간만 변경 시 즉시 반영, 대수 변경 시 승인 대기 전환)
   * POST /shop_order_pending
   */
  @PostMapping
  public ResponseEntity<ShopOrderPendingDTO.ChangeResult> requestChange(
      @RequestBody ShopOrderPendingDTO.Request request) {
    ShopOrderPendingDTO.ChangeResult result = shopOrderPendingService.save(request);
    return ResponseEntity.ok(result);
  }

  /**
   * 변경 예상 결과 미리보기
   * POST /shop_order_pending/preview
   */
  @PostMapping("/preview")
  public ResponseEntity<ShopOrderPendingDTO.ChangePreview> previewChange(
      @RequestBody ShopOrderPendingDTO.Request request) {
    ShopOrderPendingDTO.ChangePreview preview = shopOrderPendingService.previewChange(request);
    return ResponseEntity.ok(preview);
  }
  
  /**
   * 관리자용 — 승인대기 목록
   * GET /shop_order_pending/list?status=0
   */
  @GetMapping("/list")
  public ResponseEntity<PageResponse<ShopOrderPendingDTO.Response>> findList(
      ShopOrderPendingDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {
    
    Page<ShopOrderPendingDTO.Response> pageResult = shopOrderPendingService.searchPending(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }

  /**
   * 관리자용 — 승인/반려
   * PUT /shop_order_pending/{no}/approve
   */
  @PutMapping("/{no}/approve")
  public ResponseEntity<ShopOrderPendingDTO.Response> approveChange(
      @PathVariable("no") Long no,
      @RequestBody ShopOrderPendingDTO.ApprovalRequest request) {
    ShopOrderPendingDTO.Response result = shopOrderPendingService.approveChange(no, request);
    if (result == null) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(result);
  }  

}