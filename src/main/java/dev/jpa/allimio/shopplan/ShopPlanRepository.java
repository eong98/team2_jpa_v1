package dev.jpa.allimio.shopplan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.Tuple;

public interface ShopPlanRepository extends JpaRepository<ShopPlan, Long> {
  
  List<ShopPlan> findByIssellOrderByMincctv(String issell);

  /**
   * 요금제 목록 검색
   * @param word
   * @param pmonth
   * @param bprice
   * @param issell
   * @param cdate
   * @param pageable
   * @return
   */
  @Query("""
      SELECT sp FROM ShopPlan sp 
      WHERE (:word IS NULL OR :word = '' 
        OR sp.pname LIKE %:word% 
        OR sp.description LIKE %:word%) 
        AND (:pmonth IS NULL OR sp.pmonth = :pmonth) 
        AND (:bprice IS NULL OR sp.bprice = :bprice) 
        AND (:issell IS NULL OR :issell = '' OR sp.issell = :issell) 
        AND (:cdate IS NULL OR :cdate = '' OR sp.cdate = :cdate) 
      ORDER BY sp.no DESC, sp.cdate DESC
      """)
  Page<ShopPlan> searchAllPlan(
      @Param("word") String word,
      @Param("pmonth") Integer pmonth,
      @Param("bprice") Double bprice,
      @Param("issell") String issell,
      @Param("cdate") String cdate,
      Pageable pageable);
  
  
  /**
   * 특정 이용기간(pmonth) 안에서, 주어진 CCTV 대수(ccnt)가 속하는 구독권(등급)을 찾습니다.
   * 대수 변경 시 등급 자동전환 판단에 사용합니다.
   */
  @Query("""
      SELECT p FROM ShopPlan p
      WHERE p.pmonth = :pmonth
        AND p.mincctv <= :ccnt
        AND p.maxcctv >= :ccnt
      """)
  Optional<ShopPlan> findByPmonthAndCcntInRange(@Param("pmonth") Integer pmonth, @Param("ccnt") Integer ccnt);
  

}