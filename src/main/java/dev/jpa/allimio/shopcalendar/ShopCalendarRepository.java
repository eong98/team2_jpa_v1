package dev.jpa.allimio.shopcalendar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopCalendarRepository extends JpaRepository<ShopCalendar, Long> {

  /**
   * 매장번호(SNO) 기준 캘린더 목록 조회 — 캘린더 화면에 매장별로 뿌릴 때 사용
   */
  List<ShopCalendar> findBySno(long sno);

}
