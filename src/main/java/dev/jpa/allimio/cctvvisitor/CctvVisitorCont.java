package dev.jpa.allimio.cctvvisitor;

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
@RequestMapping("/cctv_visitor")
public class CctvVisitorCont {
  @Autowired
  private CctvVisitorService cctvVisitorService;

  public CctvVisitorCont() {
    System.out.println("-> CctvVisitorCont created.");
  }

  @PostMapping(path = "/save")
  public ResponseEntity<CctvVisitor> save(@RequestBody CctvVisitorDTO cctvVisitorDTO) {
    CctvVisitor savedEntity = cctvVisitorService.save(cctvVisitorDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @GetMapping(path = "/find_all")
  public List<CctvVisitor> findAll() {
    List<CctvVisitor> list = cctvVisitorService.findAll();

    return list;
  }

  @GetMapping(path = "/{pk}")
  public ResponseEntity<CctvVisitor> findByIdRead(@PathVariable("pk") long pk) {
    CctvVisitor cctvVisitor = cctvVisitorService.findById(pk);

    if (cctvVisitor != null) {
      return ResponseEntity.ok(cctvVisitor);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping(path = "/update")
  public ResponseEntity<CctvVisitor> update(@RequestBody CctvVisitorDTO cctvVisitorDTO) {
    CctvVisitor savedEntity = cctvVisitorService.update(cctvVisitorDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = cctvVisitorService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}