package dev.jpa.allimio.shopplan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.tool.PageResponse;

@RestController // RESTFull 방식 지원
@RequestMapping("/shop_plan") // http://localhost:9102/shop_plan
public class ShopPlanCont {
  @Autowired
  ShopPlanService shopPlanService;
  
  public ShopPlanCont() {
    System.out.println("-> ShopPlanCont created");
  }
  
  /**
   * 구독권 상품 등록
   * POST /shop_plan
   * http://localhost:9102/shop_plan
   */
  @PostMapping
  public ResponseEntity<ShopPlanDTO.Response> createPlan(@RequestBody ShopPlanDTO.Request request) {
    ShopPlanDTO.Response response = shopPlanService.save(request);
    return ResponseEntity
        .status(HttpStatus.CREATED) // 새로운 자원이 성공적으로 생성됨 (201 Created)
        .body(response);
  }

  /**
   * 구독권 상품 목록 전체/검색 조회 (페이징)
   * GET /shop_plan/list/admin?word=월간&pmonth=1&page=0&size=10
   * http://localhost:9102/shop_plan/list/admin
   */
  @GetMapping(path="/list/admin")
  public ResponseEntity<PageResponse<ShopPlanDTO.Response>> getAllPlans(
      ShopPlanDTO.PlanSearchRequest searchCondition, // 👈 검색 조건 묶음 DTO
      @PageableDefault(size = 10, sort = "no", direction = Sort.Direction.DESC) Pageable pageable) {

    // Service로 searchCondition 전달
    Page<ShopPlanDTO.Response> pageResult = shopPlanService.findAll(searchCondition, pageable);

    // PageResponse.of()를 통해 생성자 호출 코드 중복 없이 깔끔하게 반환
    return ResponseEntity.ok(PageResponse.of(pageResult));
  }
  
  /**
   * 구독권 상품 전체 목록 (페이징 없음, 사용자 결제화면용)
   * GET /shop_plan/list
   */
  @GetMapping(path = "/list")
  public ResponseEntity<List<ShopPlan>> getList() {
    List<ShopPlan> list = shopPlanService.findAllList();
    return ResponseEntity.ok(list);
//    public ResponseEntity<List<ShopPlanDTO.Response>> getList() {
//      List<ShopPlanDTO.Response> list = shopPlanService.findAllList();
//      return ResponseEntity.ok(list);
  }

  /**
   * 구독권 상품 단건 상세 조회
   * GET /shop_plan/1
   * http://localhost:9102/shop_plan/1
   */
  @GetMapping("/{no}")
  public ResponseEntity<ShopPlanDTO.Response> getPlanDetail(@PathVariable("no") long no) {
    ShopPlanDTO.Response response = shopPlanService.findById(no);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  /**
   * 구독권 상품 수정
   * PUT /shop_plan/1
   * http://localhost:9102/shop_plan/1
   */
  @PutMapping(path="/{no}")
  public ResponseEntity<ShopPlanDTO.Response> updatePlan(
      @PathVariable("no") long no,
      @RequestBody ShopPlanDTO.Request request) {

    ShopPlanDTO.Response response = shopPlanService.update(no, request);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  /**
   * 구독권 상품 삭제
   * DELETE /shop_plan/1
   * http://localhost:9102/shop_plan/1
   */
  @DeleteMapping(path="/{no}")
  public ResponseEntity<String> deletePlan(@PathVariable("no") long no) {
    boolean isDeleted = shopPlanService.deleteById(no);
    if (isDeleted) {
      return ResponseEntity.ok("구독권이 성공적으로 삭제되었습니다.");
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 구독권입니다.");
  }
}