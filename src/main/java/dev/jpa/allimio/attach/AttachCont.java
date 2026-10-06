package dev.jpa.allimio.attach;

import dev.jpa.allimio.tool.Download;
import dev.jpa.allimio.tool.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/attach")
@RequiredArgsConstructor // 생성자 주입 자동 생성
public class AttachCont {

  private final AttachService attachService;
  private final Download download;

  /**
   * 특정 게시글의 첨부파일 목록 조회
   * GET /attach/list/10
   */
  @GetMapping(path = "/list/{bno}")
  public ResponseEntity<List<AttachDTO>> list(
      @PathVariable("bno") Long bno,
      @RequestParam(name = "tname", required = false) String tname) { // 게시판 구분 (QA, NOTICE …)
    List<AttachDTO> list = this.attachService.getAttachList(bno, tname);
    return ResponseEntity.ok(list);
  }

  /**
   * 첨부파일 저장 경로 사용 가능 여부 — 글 저장 전에 화면에서 확인 (안 되면 글도 저장하지 않고 안내)
   * GET /attach/check → { available, message }
   */
  @GetMapping(path = "/check")
  public ResponseEntity<java.util.Map<String, Object>> checkStorage() {
    boolean available = this.attachService.isStorageAvailable();
    return ResponseEntity.ok(java.util.Map.of(
        "available", available,
        "message", available ? "" : AttachService.STORAGE_UNAVAILABLE_MESSAGE));
  }

  /** 저장 경로 오류 → 503 + { message } (화면 오류 모달에 그대로 표시) */
  @ExceptionHandler(AttachService.AttachStorageException.class)
  public ResponseEntity<java.util.Map<String, Object>> handleStorage(AttachService.AttachStorageException e) {
    return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
        .body(java.util.Map.of("success", false, "message", e.getMessage()));
  }

  /**
   * 다중 파일 업로드 등록 처리
   * POST /attach/create
   */
  @PostMapping(value = "/create")
  public ResponseEntity<List<AttachDTO>> create(@ModelAttribute AttachDTO attachDTO) {
    List<MultipartFile> files = attachDTO.getFiles();

    List<AttachDTO> savedList = this.attachService.saveAttachFiles(
        attachDTO.getTname(),
        attachDTO.getBno(),
        files
    );

    return ResponseEntity.ok(savedList);
  }

  /**
   * 단건 첨부파일 상세 조회
   * GET /attach/read/1
   */
  @GetMapping(path = "/read/{no}")
  public ResponseEntity<AttachDTO> read(@PathVariable("no") Long no) {
    AttachDTO attachDTO = this.attachService.getAttachWithMenu(no);
    if (attachDTO == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(attachDTO);
  }

  /**
   * 관리자/통합 검색 및 페이징 목록 조회
   * GET /attach/list/admin?word=테스트&page=0&size=10
   */
  @GetMapping(path = "/list/admin")
  public ResponseEntity<PageResponse<AttachDTO>> searchAllAttach(
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "tno", required = false) Long tno,
      @RequestParam(name = "type", required = false) Integer type,
      @RequestParam(name = "cdate", defaultValue = "") String cdate,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("no").descending());
    Page<AttachDTO> p = this.attachService.searchAllAttach(word, tno, type, cdate, pageable);

    return ResponseEntity.ok(PageResponse.of(p));
  }

  /**
   * 단일 첨부파일 삭제 (실물 파일 + DB 삭제)
   * DELETE /attach/delete/1
   */
  @DeleteMapping(path = "/delete/{no}")
  public ResponseEntity<Integer> delete(@PathVariable("no") Long no) {
    int cnt = this.attachService.deleteAttachFile(no);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 특정 게시글 연관 파일 일괄 삭제
   * DELETE /attach/delete_by_bno/10
   */
  @DeleteMapping(path = "/delete_by_bno/{bno}")
  public ResponseEntity<Integer> deleteByBno(
      @PathVariable("bno") Long bno,
      @RequestParam(name = "tname", required = false) String tname) { // 게시판 구분 (QA, NOTICE …)
    this.attachService.deleteByTnoAndBno(bno, tname);
    return ResponseEntity.ok(1);
  }
}