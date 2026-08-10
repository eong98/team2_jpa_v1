package dev.jpa.allimio.inmenu;

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
@RequestMapping("/inmenu")
public class InMenuCont {
  @Autowired
  private InMenuService inMenuService;

  public InMenuCont() {
    System.out.println("-> InMenuCont created.");
  }

  /**
   * 등록, http://localhost:9100/inmenu/save
   * @param inMenuDTO
   * @return
   */
  @PostMapping(path = "/save")
  public ResponseEntity<InMenu> save(@RequestBody InMenuDTO inMenuDTO) {
    InMenu savedEntity = inMenuService.save(inMenuDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 전체 목록, http://localhost:9100/inmenu/find_all
   * @return
   */
  @GetMapping(path = "/find_all")
  public List<InMenu> findAll() {
    List<InMenu> list = inMenuService.findAll();

    return list;
  }

  /**
   * 상위메뉴번호 기준 하위메뉴 목록 (트리 조립용)
   * 최상위메뉴는 fkno 파라미터 없이 호출
   * http://localhost:9100/inmenu/find_by_fkno
   * http://localhost:9100/inmenu/find_by_fkno?fkno=1
   * @param fkno
   * @return
   */
  @GetMapping(path = "/find_by_fkno")
  public List<InMenu> findByFkno(@RequestParam(value = "fkno", required = false) Long fkno) {
    List<InMenu> list = inMenuService.findByFkno(fkno);

    return list;
  }

  /**
   * 조회, Primary Key를 이용한 조회
   * http://localhost:9100/inmenu/9
   * @return
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<InMenu> findByIdRead(@PathVariable("pk") long pk) {
    InMenu inMenu = inMenuService.findById(pk);

    if (inMenu != null) {
      return ResponseEntity.ok(inMenu);
    } else {
      return ResponseEntity.notFound().build(); // 404
    }
  }

  /**
   * 수정, http://localhost:9100/inmenu/update
   * @param inMenuDTO
   * @return
   */
  @PutMapping(path = "/update")
  public ResponseEntity<InMenu> update(@RequestBody InMenuDTO inMenuDTO) {
    InMenu savedEntity = inMenuService.update(inMenuDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제, http://localhost:9100/inmenu/9
   * @param pk
   * @return
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = inMenuService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
