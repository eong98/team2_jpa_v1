package dev.jpa.allimio.shopplan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

//import dev.jpa.allimio.shoporder.ShopOrderRepository;
import dev.jpa.allimio.tool.Tool;

@Service
public class ShopPlanService {
  @Autowired
  ShopPlanRepository shopPlanRepository;
  
//  @Autowired
//  ShopOrderRepository shopOrderRepository;

  public ShopPlanService() {

  }

  /***
   * 구독권 등록
   * @param request
   * @return
   */
  public ShopPlanDTO.Response save(ShopPlanDTO.Request request) {
    ShopPlan shopPlan = request.toEntity();
    shopPlan.setCdate(Tool.getDate());

    ShopPlan saved = shopPlanRepository.save(shopPlan);
    return ShopPlanDTO.Response.from(saved);
  }
  
  /**
   * 구독권 조회 (관리자 조회용)
   * @return
   */
  public Page<ShopPlanDTO.Response> findAll(ShopPlanDTO.PlanSearchRequest req, Pageable pageable) {
    Page<ShopPlan> spPage = shopPlanRepository.searchAllPlan(
        req.getWord(),
        req.getPmonth(),
        req.getBprice(),
        req.getIssell(),
        req.getCdate(),
        pageable);

    return spPage.map(ShopPlanDTO.Response::from);
  }
  

  /**
   * 판매중인 구독권 전체 목록 (사용자 결제화면용).
   * SHOP_ORDER 집계 결과를 붙여서, 같은 이용기간(pmonth) 그룹 안에서
   * 주문 건수가 가장 많은 구독권에 popular=true를 표시합니다.
   */
//  public List<ShopPlanDTO.Response> findAllList() {
    public List<ShopPlan> findAllList() {
    List<ShopPlan> list = shopPlanRepository.findByIssellOrderByMincctv("Y");

    // pno -> 주문건수 맵
//    Map<Long, Long> countMap = shopOrderRepository.countByPno().stream()
//        .collect(Collectors.toMap(
//            ShopOrderRepository.PnoCountProjection::getPno,
//            ShopOrderRepository.PnoCountProjection::getCnt));
//
//    List<ShopPlanDTO.Response> responses = list.stream()
//        .map(ShopPlan -> {
//          ShopPlanDTO.Response r = ShopPlanDTO.Response.from(ShopPlan);
//          r.setOrderCount(countMap.getOrDefault(ShopPlan.getNo(), 0L));
//          return r;
//        })
//        .collect(Collectors.toList());
//
//    // pmonth별로 묶어서 그룹 내 최댓값(주문건수 1위)에 popular=true
//    Map<Integer, Optional<ShopPlanDTO.Response>> topByMonth = responses.stream()
//        .collect(Collectors.groupingBy(
//            ShopPlanDTO.Response::getPmonth,
//            Collectors.maxBy(Comparator.comparingLong(ShopPlanDTO.Response::getOrderCount))));
//
//    topByMonth.values().forEach(opt -> opt.ifPresent(top -> {
//      if (top.getOrderCount() > 0) { // 주문이 아예 없으면 인기 배지 안 붙임
//        top.setPopular(true);
//      }
//    }));

//    return responses;
    return list;
  }

  /**
   * 단일 조회
   * @param pk
   * @return
   */
  public ShopPlanDTO.Response findById(long pk) {
    Optional<ShopPlan> optional = shopPlanRepository.findById(pk);

    if (optional.isPresent()) {
      return ShopPlanDTO.Response.from(optional.get());
    }
    return null;
  }

  
  /**
   * 구독권 수정
   * @param pk
   * @param request
   * @return
   */
  public ShopPlanDTO.Response update(long pk, ShopPlanDTO.Request request) {
    Optional<ShopPlan> optional = shopPlanRepository.findById(pk);

    if (optional.isPresent()) {
      ShopPlan shopPlan = optional.get();
      shopPlan.setPname(request.getPname());
      shopPlan.setPmonth(request.getPmonth());
      shopPlan.setBprice(request.getBprice());
      shopPlan.setMaxcctv(request.getMaxcctv());
      shopPlan.setMincctv(request.getMincctv());
      shopPlan.setDescription(request.getDescription());
      if (request.getIssell() != null) {
        shopPlan.setIssell(request.getIssell());
      }

      ShopPlan savedEntity = shopPlanRepository.save(shopPlan);
      return ShopPlanDTO.Response.from(savedEntity);
    }
    return null;
  }

  /**
   * 구독권 삭제
   * @param pk
   * @return
   */
  public boolean deleteById(long pk) {
    Optional<ShopPlan> optional = shopPlanRepository.findById(pk);

    if (optional.isPresent()) {
      shopPlanRepository.deleteById(pk);
      return true;
    }
    return false;
  }
}