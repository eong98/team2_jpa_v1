package dev.jpa.allimio.shop;

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
@RequestMapping("/shop")
public class ShopCont {
  @Autowired
  private ShopService shopService;

  public ShopCont() {
    System.out.println("-> ShopCont created.");
  }

  /**
   * 등록, http://localhost:9100/shop/save
   * @param shopDTO
   * @return
   */
  @PostMapping(path = "/save")
  public ResponseEntity<Shop> save(@RequestBody ShopDTO shopDTO) {
    Shop savedEntity = shopService.save(shopDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 전체 목록, http://localhost:9100/shop/find_all
   * @return
   */
  @GetMapping(path = "/find_all")
  public List<Shop> findAll() {
    List<Shop> list = shopService.findAll();

    return list;
  }

  /**
   * 조회, Primary Key를 이용한 조회
   * http://localhost:9100/shop/9
   * @return
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<Shop> findByIdRead(@PathVariable("pk") long pk) {
    Shop shop = shopService.findById(pk);

    if (shop != null) {
      return ResponseEntity.ok(shop);
    } else {
      return ResponseEntity.notFound().build(); // 404
    }
  }

  /**
   * 수정, http://localhost:9100/shop/update
   * @param shopDTO
   * @return
   */
  @PutMapping(path = "/update")
  public ResponseEntity<Shop> update(@RequestBody ShopDTO shopDTO) {
    Shop savedEntity = shopService.update(shopDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제, http://localhost:9100/shop/9
   * @param pk
   * @return
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = shopService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}