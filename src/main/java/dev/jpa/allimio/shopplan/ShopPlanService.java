package dev.jpa.allimio.shopplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopPlanService {
  @Autowired
  ShopPlanRepository shopPlanRepository;

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
  
  /** 사용자 조회용 */
  public List<ShopPlanDTO.Response> findAllList() {
    List<ShopPlan> list = shopPlanRepository.findAll();
    return list.stream()
        .map(ShopPlanDTO.Response::from)
        .collect(Collectors.toList());
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