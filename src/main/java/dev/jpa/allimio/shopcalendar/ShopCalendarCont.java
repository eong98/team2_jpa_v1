package dev.jpa.allimio.shopcalendar;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopcalendar")
public class ShopCalendarCont {
  @Autowired
  private ShopCalendarService shopCalendarService;

  public ShopCalendarCont() {
    System.out.println("-> ShopCalendarCont created.");
  }

  /**
   * 등록, http://localhost:9100/shopcalendar/save
   * @param shopCalendarDTO
   * @return
   */
  @PostMapping(path = "/save")
  public ResponseEntity<ShopCalendar> save(@RequestBody ShopCalendarDTO shopCalendarDTO) {
    ShopCalendar savedEntity = shopCalendarService.save(shopCalendarDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 전체 목록, http://localhost:9100/shopcalendar/find_all
   * @return
   */
  @GetMapping(path = "/find_all")
  public List<ShopCalendar> findAll() {
    List<ShopCalendar> list = shopCalendarService.findAll();

    return list;
  }

  /**
   * 매장번호 기준 캘린더 목록, http://localhost:9100/shopcalendar/find_by_sno/1
   * @param sno
   * @return
   */
  @GetMapping(path = "/find_by_sno/{sno}")
  public List<ShopCalendar> findBySno(@PathVariable("sno") long sno) {
    List<ShopCalendar> list = shopCalendarService.findBySno(sno);

    return list;
  }

  /**
   * 조회, Primary Key를 이용한 조회
   * http://localhost:9100/shopcalendar/9
   * @return
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<ShopCalendar> findByIdRead(@PathVariable("pk") long pk) {
    ShopCalendar shopCalendar = shopCalendarService.findById(pk);

    if (shopCalendar != null) {
      return ResponseEntity.ok(shopCalendar);
    } else {
      return ResponseEntity.notFound().build(); // 404
    }
  }

  /**
   * 수정, http://localhost:9100/shopcalendar/update
   * @param shopCalendarDTO
   * @return
   */
  @PutMapping(path = "/update")
  public ResponseEntity<ShopCalendar> update(@RequestBody ShopCalendarDTO shopCalendarDTO) {
    ShopCalendar savedEntity = shopCalendarService.update(shopCalendarDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제, http://localhost:9100/shopcalendar/9
   * @param pk
   * @return
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = shopCalendarService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
