package dev.jpa.allimio.cctv;

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
@RequestMapping("/cctv")
public class CctvCont {
  @Autowired
  private CctvService cctvService;

  public CctvCont() {
    System.out.println("-> CctvCont created.");
  }

  @PostMapping(path = "/save")
  public ResponseEntity<Cctv> save(@RequestBody CctvDTO cctvDTO) {
    Cctv savedEntity = cctvService.save(cctvDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @GetMapping(path = "/find_all")
  public List<Cctv> findAll() {
    List<Cctv> list = cctvService.findAll();

    return list;
  }

  @GetMapping(path = "/{pk}")
  public ResponseEntity<Cctv> findByIdRead(@PathVariable("pk") long pk) {
    Cctv cctv = cctvService.findById(pk);

    if (cctv != null) {
      return ResponseEntity.ok(cctv);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping(path = "/update")
  public ResponseEntity<Cctv> update(@RequestBody CctvDTO cctvDTO) {
    Cctv savedEntity = cctvService.update(cctvDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = cctvService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}