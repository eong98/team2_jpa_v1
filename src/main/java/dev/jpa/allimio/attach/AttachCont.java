package dev.jpa.allimio.attach;

import dev.jpa.allimio.tool.Download;
import dev.jpa.allimio.tool.PageResponse;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attach")
public class AttachCont {

  private final Download download;

  @Autowired
  private AttachService attachService;

  public AttachCont(Download download) {
    System.out.println("-> AttachCont created.");
    this.download = download;
  }

  /**
   * 특정 게시글의 첨부파일 목록 조회
   * - http://localhost:9102/attach/list/1/10 또는 http://localhost:9102/attach/list/10
   * 
   * @param bno 게시글 번호
   * @return 해당 게시글의 첨부파일 DTO 목록
   */
  @GetMapping(path = {"/list/{bno}"})
  public ResponseEntity<List<AttachDTO>> list(
      @PathVariable("bno") Long bno) {
    
    List<AttachDTO> list = this.attachService.getAttachList(bno);
    return ResponseEntity.ok(list);
  }

  /**
   * 다중 파일 업로드 등록 처리, http://localhost:9102/attach/create
   * 
   * @param attachDTO 첨부파일 업로드 폼 데이터
   * @return 저장된 AttachDTO 리스트
   */
  @PostMapping(value = "/create")
  public ResponseEntity<List<AttachDTO>> create(@ModelAttribute("attachDTO") AttachDTO attachDTO) {
    System.out.println("-> tname: " + attachDTO.getTname());
    System.out.println("-> bno: " + attachDTO.getBno());

    // ------------------------------------------------------------------------------
    // 파일 전송 및 DB 저장 (tname으로 tno는 서비스 내부에서 자동 역조회)
    // ------------------------------------------------------------------------------
    List<MultipartFile> files = attachDTO.getFiles();
    System.out.println("-> files: " + files);
    
    List<AttachDTO> savedList = this.attachService.saveAttachFiles(
        attachDTO.getTname(), 
        attachDTO.getBno(), 
        files
    );

    return ResponseEntity.ok(savedList);
  }

  /**
   * 단건 첨부파일 상세 조회, http://localhost:9102/attach/read/1
   * 
   * @param no 첨부파일 번호(PK)
   * @return AttachDTO
   */
  @GetMapping(path = "/read/{no}")
  public ResponseEntity<AttachDTO> read(@PathVariable("no") Long no) {
    System.out.println("-> attach no: " + no);
    AttachDTO attachDTO = this.attachService.getAttachWithMenu(no);
    return ResponseEntity.ok(attachDTO);
  }

  /**
   * 페이징 + 검색 목록, http://localhost:9102/attach/list_all_paging_search?word=테스트&page=0&size=10
   */
  @GetMapping(path = "/list_all_paging_search")
  public ResponseEntity<PageResponse<AttachDTO>> list_all_paging_search(
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
   * 단일 첨부파일 삭제, http://localhost:9102/attach/delete/1
   * 각각의 첨부파일만 삭제시도시 서버에 저장된 실제 파일도 삭제됩니다.
   */
  @DeleteMapping(path = "/delete/{no}")
  public ResponseEntity<Integer> delete(@PathVariable("no") Long no) {
    System.out.println("-> delete attach no: " + no);
    int cnt = this.attachService.deleteAttachFile(no);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 특정 게시글 연관 파일 일괄 삭제, http://localhost:9102/attach/delete_by_bno/10
   */
  @DeleteMapping(path = "/delete_by_bno/{bno}")
  public ResponseEntity<Integer> deleteByBno(@PathVariable("bno") Long bno) {
    this.attachService.deleteByTnoAndBno(bno);
    return ResponseEntity.ok(1);
  }
}