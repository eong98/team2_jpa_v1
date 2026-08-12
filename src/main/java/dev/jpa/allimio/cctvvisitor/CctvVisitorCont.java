package dev.jpa.allimio.cctvvisitor;

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

  /**
   * 관리자 손님(방문객) 목록 검색 + 페이징 (/dbms/cctvvisitor), http://localhost:9102/cctv_visitor/admin/search
   * 조건은 전부 선택 사항(안 넘기면 전체 대상). 매장(sno) 상관없이 전체 손님 내역이 기본이며,
   * sno를 넘기면 해당 매장 소유 CCTV로 찍힌 손님만 필터링됩니다. 기본 정렬: no 내림차순(최신순)
   * @param sno 매장 번호 (선택)
   * @param cno CCTV번호 (선택)
   * @param state 상태값 (선택)
   * @param keyword AI추적ID(trackId) 포함 검색
   * @param intimeFrom 입장일 시작 (yyyy-MM-dd)
   * @param intimeTo 입장일 종료 (yyyy-MM-dd)
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 10)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/admin/search")
  public Map<String, Object> adminSearch(
      @RequestParam(value = "sno", required = false) Long sno,
      @RequestParam(value = "cno", required = false) Long cno,
      @RequestParam(value = "state", required = false) Integer state,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "intimeFrom", required = false) String intimeFrom,
      @RequestParam(value = "intimeTo", required = false) String intimeTo,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "no"));
    Page<CctvVisitor> result = cctvVisitorService.searchAdmin(sno, cno, state, keyword, intimeFrom, intimeTo, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
  }

  /**
   * 매장(사용자) 손님 목록 검색 + 페이징 (/user/cctvvisitor), http://localhost:9102/cctv_visitor/search
   * sno(로그인 후 입장한 매장)는 필수, 나머지 조건은 admin/search와 동일하게 전부 선택 사항.
   * @param sno 매장 번호 (필수, GlobalCurrentShop().no 기준)
   * @param cno CCTV번호 (선택)
   * @param state 상태값 (선택)
   * @param keyword AI추적ID(trackId) 포함 검색
   * @param intimeFrom 입장일 시작 (yyyy-MM-dd)
   * @param intimeTo 입장일 종료 (yyyy-MM-dd)
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 10)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/search")
  public Map<String, Object> search(
      @RequestParam(value = "sno") long sno,
      @RequestParam(value = "cno", required = false) Long cno,
      @RequestParam(value = "state", required = false) Integer state,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "intimeFrom", required = false) String intimeFrom,
      @RequestParam(value = "intimeTo", required = false) String intimeTo,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "no"));
    Page<CctvVisitor> result = cctvVisitorService.searchByShop(sno, cno, state, keyword, intimeFrom, intimeTo, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
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