package dev.jpa.allimio.shopmenu;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopmenu")
public class ShopMenuCont {
  @Autowired
  private ShopMenuService shopMenuService;

  public ShopMenuCont() {
    System.out.println("-> ShopMenuCont created.");
  }

  /**
   * 등록, http://localhost:9102/shopmenu/save
   * @param shopMenuDTO
   * @return
   */
  @PostMapping(path = "/save")
  public ResponseEntity<ShopMenu> save(@RequestBody ShopMenuDTO shopMenuDTO) {
    ShopMenu savedEntity = shopMenuService.save(shopMenuDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 전체 목록, http://localhost:9102/shopmenu/find_all
   * @return
   */
  @GetMapping(path = "/find_all")
  public List<ShopMenu> findAll() {
    List<ShopMenu> list = shopMenuService.findAll();

    return list;
  }

  /**
   * 상위메뉴번호 기준 하위메뉴 목록 (트리 조립용)
   * 최상위메뉴는 fkno 파라미터 없이 호출
   * http://localhost:9102/shopmenu/find_by_fkno
   * http://localhost:9102/shopmenu/find_by_fkno?fkno=1
   * @param fkno
   * @return
   */
  @GetMapping(path = "/find_by_fkno")
  public List<ShopMenu> findByFkno(@RequestParam(value = "fkno", required = false) Long fkno) {
    List<ShopMenu> list = shopMenuService.findByFkno(fkno);

    return list;
  }

  /**
   * 조회, Primary Key를 이용한 조회
   * http://localhost:9102/shopmenu/9
   * @return
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<ShopMenu> findByIdRead(@PathVariable("pk") long pk) {
    ShopMenu shopMenu = shopMenuService.findById(pk);

    if (shopMenu != null) {
      return ResponseEntity.ok(shopMenu);
    } else {
      return ResponseEntity.notFound().build(); // 404
    }
  }

  /**
   * 수정, http://localhost:9102/shopmenu/update
   * @param shopMenuDTO
   * @return
   */
  @PutMapping(path = "/update")
  public ResponseEntity<ShopMenu> update(@RequestBody ShopMenuDTO shopMenuDTO) {
    ShopMenu savedEntity = shopMenuService.update(shopMenuDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제, http://localhost:9102/shopmenu/9
   * @param pk
   * @return
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = shopMenuService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
