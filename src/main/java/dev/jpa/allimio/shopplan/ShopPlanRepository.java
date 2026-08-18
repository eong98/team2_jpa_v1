package dev.jpa.allimio.shopplan;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopPlanRepository extends JpaRepository<ShopPlan, Long> {
  
  List<ShopPlan> findByIssell(String issell);

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

}