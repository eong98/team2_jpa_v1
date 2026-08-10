package dev.jpa.allimio.cctvissue;

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
@RequestMapping("/cctv_issue")
public class CctvIssueCont {
  @Autowired
  private CctvIssueService cctvIssueService;

  public CctvIssueCont() {
    System.out.println("-> CctvIssueCont created.");
  }

  @PostMapping(path = "/save")
  public ResponseEntity<CctvIssue> save(@RequestBody CctvIssueDTO cctvIssueDTO) {
    CctvIssue savedEntity = cctvIssueService.save(cctvIssueDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @GetMapping(path = "/find_all")
  public List<CctvIssue> findAll() {
    List<CctvIssue> list = cctvIssueService.findAll();

    return list;
  }

  @GetMapping(path = "/{pk}")
  public ResponseEntity<CctvIssue> findByIdRead(@PathVariable("pk") long pk) {
    CctvIssue cctvIssue = cctvIssueService.findById(pk);

    if (cctvIssue != null) {
      return ResponseEntity.ok(cctvIssue);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping(path = "/update")
  public ResponseEntity<CctvIssue> update(@RequestBody CctvIssueDTO cctvIssueDTO) {
    CctvIssue savedEntity = cctvIssueService.update(cctvIssueDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = cctvIssueService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}