package dev.jpa.allimio.shoporder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
   * 회원 등급 조회
   * @param mno
   * @return
   */
  @GetMapping("/grade/{mno}")
  public ResponseEntity<Integer> memberGrade(@PathVariable(name = "mno") Long mno) {
    if (mno == null) {
      return ResponseEntity.badRequest().build();
  }
  
    int grade = shopOrderService.memberGrade(mno);
  return ResponseEntity.ok(grade);
  }
  

  /**
   * 전체 구독내역 목록 
   * GET /shop_order/list/1
   */
  @GetMapping("/list/{mno}")
  public ResponseEntity<PageResponse<ShopOrderDTO.Response>> search(
      @PathVariable("mno") Long mno,
      ShopOrderDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    searchCondition.setMno(mno);
    Page<ShopOrderDTO.Response> pageResult = shopOrderService.searchOrders(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
  
  /**
   *  매장별 전체 구독내역 목록
   * GET /shop_order/list/1/1/...
   */
  @GetMapping("/list/{mno}/{sno}")
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
   * 매장별 구독내역 상단 노출
   * GET /shop_order/top/1/1?status=1
   * @param sno
   * @param status
   * @return
   */
  @GetMapping(path="/top/{mno}/{sno}")
  public ResponseEntity<ShopOrderDTO.Response> findByShopTop(
      @PathVariable(name="mno") Long mno,
      @PathVariable(name="sno") Long sno,
      @RequestParam(name="status", required = true) Integer status) {
    ShopOrderDTO.Response response = shopOrderService.findByShopTop(mno, sno, status);
    if (response == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
  }
  
  
  /**
   * 주문내역 상세조회
   * 
   * GET /shop_order/ORD-20260819-000001
   */
  @GetMapping("/{no}")
  public ResponseEntity<ShopOrderDTO.Response> findById(@PathVariable("no") String no) {
    ShopOrderDTO.Response response = shopOrderService.findById(no);
    if (response == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
  }
  
  
  /**
   * 변경가능한 CCTV 대수 호출
   * 
   * GET /shop_order/set_cctv/ORD-20260819-000001
   */
  @GetMapping("/set_cctv/{no}")
  public ResponseEntity<ShopOrderDTO.Response> setChangeInfo(@PathVariable("no") String no) {
    ShopOrderDTO.Response response = shopOrderService.setChangeInfo(no);
    if (response == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
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
   * 정상 작동중인 구독권 개수 반환
   * 
   * 갱신버튼 노출용
   * @param sno
   * @return
   * 
   * GET /shop_order/active-count/1
   */
  @GetMapping("/active-count/{sno}")
  public int activeCount(@PathVariable("sno") Long sno) {
    return shopOrderService.setCount(sno);
  }
  

  /**
   * [관리자] 전체 구독내역 목록 
   * GET /shop_order/list/admin
   */
  @GetMapping("/list/admin")
  public ResponseEntity<PageResponse<ShopOrderDTO.Response>> searchAdmin(
      ShopOrderDTO.SearchRequest searchCondition,
      @PageableDefault(size = 10, sort = "cdate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<ShopOrderDTO.Response> pageResult = shopOrderService.searchAllOrders(searchCondition, pageable);
    return ResponseEntity.ok(PageResponse.of(pageResult));
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
      @RequestParam(name="newPmonth", required = false) Integer newPmonth,
      @RequestParam(name="pmethod", required = false) Integer pmethod
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