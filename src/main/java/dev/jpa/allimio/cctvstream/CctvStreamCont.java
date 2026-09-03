package dev.jpa.allimio.cctvstream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

/* ---------------------------------------------------------------------
   CCTV 스트림 연결정보(CCTV_STREAM) 컨트롤러 - 관리자 전용(dbms/cctv/CctvStream*.tsx).
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/cctv_stream")
public class CctvStreamCont {
  @Autowired
  private CctvStreamService cctvStreamService;

  public CctvStreamCont() {
    System.out.println("-> CctvStreamCont created.");
  }

  @PostMapping(path = "/save")
  public ResponseEntity<CctvStream> save(@RequestBody CctvStreamDTO cctvStreamDTO) {
    CctvStream savedEntity = cctvStreamService.save(cctvStreamDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @GetMapping(path = "/find_all")
  public List<CctvStream> findAll() {
    List<CctvStream> list = cctvStreamService.findAll();

    return list;
  }

  /**
   * 관리자 스트림 목록 검색 + 페이징, http://localhost:9102/cctv_stream/search
   * 조건은 전부 선택 사항. 기본 정렬: cdate(등록일) 내림차순(최신순)
   */
  @GetMapping(path = "/search")
  public Map<String, Object> search(
      @RequestParam(value = "cno", required = false) Long cno,
      @RequestParam(value = "connState", required = false) Integer connState,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "cdate"));
    Page<CctvStream> result = cctvStreamService.search(cno, connState, keyword, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
  }

  /**
   * CCTV번호(cno)로 스트림 조회 - CctvForm.tsx에서 CCTV 수정 화면 들어갈 때 연결정보 유무 확인용,
   * Jetson 워커가 자기 담당 CCTV의 접속정보를 가져올 때도 사용.
   * http://localhost:9102/cctv_stream/by_cno/9
   */
  @GetMapping(path = "/by_cno/{cno}")
  public ResponseEntity<CctvStream> findByCno(@PathVariable("cno") long cno) {
    CctvStream cctvStream = cctvStreamService.findByCno(cno);

    if (cctvStream != null) {
      return ResponseEntity.ok(cctvStream);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(path = "/{pk}")
  public ResponseEntity<CctvStream> findByIdRead(@PathVariable("pk") long pk) {
    CctvStream cctvStream = cctvStreamService.findById(pk);

    if (cctvStream != null) {
      return ResponseEntity.ok(cctvStream);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping(path = "/update")
  public ResponseEntity<CctvStream> update(@RequestBody CctvStreamDTO cctvStreamDTO) {
    CctvStream savedEntity = cctvStreamService.update(cctvStreamDTO);

    return ResponseEntity.ok(savedEntity);
  }

  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = cctvStreamService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
