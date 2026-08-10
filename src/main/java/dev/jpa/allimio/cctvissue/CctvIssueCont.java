package dev.jpa.allimio.cctvissue;

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

  /**
   * 관리자 목록 검색 + 페이징, http://localhost:9102/cctv_issue/search
   * 조건은 전부 선택 사항(안 넘기면 전체 대상). 기본 정렬: no 내림차순(최신순)
   * @param cno CCTV번호
   * @param code 문제유형코드
   * @param state 오탐여부
   * @param noticeyn 발송여부 Y/N
   * @param keyword 상황설명(comnet) 포함 검색
   * @param cdateFrom 등록일 시작 (yyyy-MM-dd)
   * @param cdateTo 등록일 종료 (yyyy-MM-dd)
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 10)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/search")
  public Map<String, Object> search(
      @RequestParam(value = "cno", required = false) Long cno,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "state", required = false) Integer state,
      @RequestParam(value = "noticeyn", required = false) String noticeyn,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "cdateFrom", required = false) String cdateFrom,
      @RequestParam(value = "cdateTo", required = false) String cdateTo,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "no"));
    Page<CctvIssue> result = cctvIssueService.search(cno, code, state, noticeyn, keyword, cdateFrom, cdateTo, pageable);

    Map<String, Object> body = new HashMap<>();
    body.put("content", result.getContent());
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
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
