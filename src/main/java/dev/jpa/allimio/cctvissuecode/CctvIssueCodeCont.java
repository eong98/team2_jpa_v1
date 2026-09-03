package dev.jpa.allimio.cctvissuecode;

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
   이상행동유형코드(CCTV_ISSUE_CODE) 컨트롤러.

   ⚠️ CctvIssue.ts의 CODE_LABELS 하드코딩을 대체하는 API입니다.
   /list는 화면(dbms·user 공통)에서 코드→라벨 매핑을 만들 때 쓰는 가벼운 전체 조회용이고,
   /search는 관리자 코드관리 화면(dbms/cctv/CctvIssueCodeList.tsx)의 CRUD 목록용입니다.
--------------------------------------------------------------------- */
@RestController
@RequestMapping("/cctv_issue_code")
public class CctvIssueCodeCont {
  @Autowired
  private CctvIssueCodeService cctvIssueCodeService;

  public CctvIssueCodeCont() {
    System.out.println("-> CctvIssueCodeCont created.");
  }

  /**
   * 등록 (관리자 전용, dbms/cctv/CctvIssueCodeForm.tsx), http://localhost:9102/cctv_issue_code/save
   */
  @PostMapping(path = "/save")
  public ResponseEntity<CctvIssueCode> save(@RequestBody CctvIssueCodeDTO cctvIssueCodeDTO) {
    CctvIssueCode savedEntity = cctvIssueCodeService.save(cctvIssueCodeDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 사용 중인(useYn='Y') 코드 전체 목록, 정렬순서(ord)대로.
   * CctvIssue.ts의 CODE_LABELS 하드코딩을 대체하기 위해 dbms/user 양쪽 CctvIssueList.tsx에서
   * 공통으로 호출합니다(코드→라벨 매핑용, 가벼운 캐시 대상).
   * http://localhost:9102/cctv_issue_code/list
   */
  @GetMapping(path = "/list")
  public List<CctvIssueCode> list() {
    return cctvIssueCodeService.findAllUse();
  }

  /** 미사용 코드까지 포함한 전체 목록 (참고용) */
  @GetMapping(path = "/find_all")
  public List<CctvIssueCode> findAll() {
    return cctvIssueCodeService.findAll();
  }

  /**
   * 관리자 코드관리 목록 검색 + 페이징, http://localhost:9102/cctv_issue_code/search
   * 조건은 전부 선택 사항. 기본 정렬: ord(정렬순서) 오름차순
   */
  @GetMapping(path = "/search")
  public Map<String, Object> search(
      @RequestParam(value = "useYn", required = false) String useYn,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "ord"));
    Page<CctvIssueCode> result = cctvIssueCodeService.search(useYn, keyword, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
  }

  /**
   * 조회, Primary Key(CODE)를 이용한 조회
   * http://localhost:9102/cctv_issue_code/01
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<CctvIssueCode> findByIdRead(@PathVariable("pk") String pk) {
    CctvIssueCode cctvIssueCode = cctvIssueCodeService.findById(pk);

    if (cctvIssueCode != null) {
      return ResponseEntity.ok(cctvIssueCode);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * 수정 (관리자 전용), http://localhost:9102/cctv_issue_code/update
   */
  @PutMapping(path = "/update")
  public ResponseEntity<CctvIssueCode> update(@RequestBody CctvIssueCodeDTO cctvIssueCodeDTO) {
    CctvIssueCode savedEntity = cctvIssueCodeService.update(cctvIssueCodeDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제 (관리자 전용). CCTV_ISSUE.CODE에 FK 제약을 걸어둔 경우, 이미 사용 중인 코드는
   * 삭제 대신 사용여부(useYn)를 'N'으로 바꾸는 걸 권장합니다(수정 화면에서 지원).
   * http://localhost:9102/cctv_issue_code/01
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") String pk) {
    boolean sw = cctvIssueCodeService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
