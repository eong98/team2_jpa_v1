package dev.jpa.allimio.cctv;

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
   CCTV(카메라 장비) 컨트롤러.

   ⚠️ 등록(save)/수정(update)/삭제(delete)는 관리자(dbms/cctv) 전용입니다.
     사용자(user/shop) 화면은 /search(조회 전용)만 사용하고, save/update/delete는
     호출하지 않습니다(프론트에서 호출 안 함 - ShopCont처럼 컨트롤러 자체를
     역할별로 나누지 않는 이 프로젝트 관례를 그대로 따름).
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/cctv")
public class CctvCont {
  @Autowired
  private CctvService cctvService;

  public CctvCont() {
    System.out.println("-> CctvCont created.");
  }

  /**
   * 등록 (관리자 전용, dbms/cctv/CctvForm.tsx), http://localhost:9102/cctv/save
   */
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

  /**
   * 매장 CCTV 목록 검색 + 페이징 (/user/shop/cctv, 조회 전용), http://localhost:9102/cctv/search
   * sno(입장한 매장)는 필수, keyword는 선택 사항. 기본 정렬: cdate(등록일) 내림차순(최신순)
   * @param sno 매장 번호 (필수, GlobalCurrentShop().no 기준)
   * @param keyword CCTV명/MAC주소 포함 검색
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 10)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/search")
  public Map<String, Object> search(
      @RequestParam(value = "sno") long sno,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "cdate"));
    Page<Cctv> result = cctvService.search(sno, keyword, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
  }

  /**
   * 관리자 CCTV 목록 검색 + 페이징 (/dbms/cctv), http://localhost:9102/cctv/admin/search
   * sno 상관없이 전체 CCTV 대상. sno를 넘기면 해당 매장 소유 CCTV만 조회.
   * @param sno 매장 번호 (선택 사항)
   * @param state 상태 코드 (선택 사항)
   * @param keyword CCTV명/MAC주소 포함 검색
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 10)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/admin/search")
  public Map<String, Object> adminSearch(
      @RequestParam(value = "sno", required = false) Long sno,
      @RequestParam(value = "state", required = false) Integer state,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "cdate"));
    Page<Cctv> result = cctvService.searchAdmin(sno, state, keyword, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
  }

  /**
   * 조회, Primary Key를 이용한 조회
   * http://localhost:9102/cctv/9
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<Cctv> findByIdRead(@PathVariable("pk") long pk) {
    Cctv cctv = cctvService.findById(pk);

    if (cctv != null) {
      return ResponseEntity.ok(cctv);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * 수정 (관리자 전용, dbms/cctv/CctvForm.tsx), http://localhost:9102/cctv/update
   */
  @PutMapping(path = "/update")
  public ResponseEntity<Cctv> update(@RequestBody CctvDTO cctvDTO) {
    Cctv savedEntity = cctvService.update(cctvDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제 (관리자 전용, dbms/cctv/CctvList.tsx), http://localhost:9102/cctv/9
   */
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