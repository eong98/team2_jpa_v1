package dev.jpa.allimio.attach;

import dev.jpa.allimio.tool.Download;
import dev.jpa.allimio.tool.PageResponse;
import dev.jpa.allimio.tool.Tool;
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
   * 특정 게시글의 첨부파일 목록 조회, http://localhost:9102/attach/list/1/10
   * 
   * @param tno 메뉴/게시판 번호
   * @param bno 게시글 번호
   * @return 해당 게시글의 첨부파일 DTO 목록
   */
  @GetMapping(path = "/list/{tno}/{bno}")
  public ResponseEntity<List<AttachDTO>> list(
      @PathVariable("tno") Long tno, 
      @PathVariable("bno") Long bno) {
    System.out.println("-> tno: " + tno + ", bno: " + bno);
    List<AttachDTO> list = this.attachService.getAttachList(tno, bno);
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
    System.out.println("-> tno: " + attachDTO.getTno());
    System.out.println("-> tname: " + attachDTO.getTname());
    System.out.println("-> bno: " + attachDTO.getBno());

    // ------------------------------------------------------------------------------
    // 파일 전송 및 DB 저장
    // ------------------------------------------------------------------------------
    List<MultipartFile> files = attachDTO.getFiles();
    System.out.println("-> files: " + attachDTO.getFiles());
    List<AttachDTO> savedList = this.attachService.saveAttachFiles(
        attachDTO.getTno(), 
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
   * 
   * @param word  검색어
   * @param tno   메뉴 번호
   * @param tname 테이블/폴더명
   * @param name  원본 파일명
   * @param type  파일 구분 (0: 이미지, 1: 일반 파일)
   * @param cdate 등록일자
   * @param page  페이지 번호 (0부터 시작)
   * @param size  한 페이지당 레코드 수
   * @return PageResponse<AttachDTO>
   */
  @GetMapping(path = "/list_all_paging_search")
  public ResponseEntity<PageResponse<AttachDTO>> list_all_paging_search(
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "tno", required = false) Long tno,
      @RequestParam(name = "tname", defaultValue = "") String tname,
      @RequestParam(name = "name", defaultValue = "") String name,
      @RequestParam(name = "type", required = false) Integer type,
      @RequestParam(name = "cdate", defaultValue = "") String cdate,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("no").descending());
    Page<AttachDTO> p = this.attachService.searchAllAttach(word, tno, tname, name, type, cdate, pageable);

    // PageResponse.of() 편의 메서드를 통해 간결하게 응답
    return ResponseEntity.ok(PageResponse.of(p));
  }

  /**
   * 단일 첨부파일 삭제, http://localhost:9102/attach/delete/1
   * 
   * @param no 삭제할 첨부파일 번호(PK)
   * @return 0: 삭제 실패, 1: 삭제 성공
   */
  @DeleteMapping(path = "/delete/{no}")
  public ResponseEntity<Integer> delete(@PathVariable("no") Long no) {
    System.out.println("-> delete attach no: " + no);
    int cnt = this.attachService.deleteAttachFile(no);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 특정 게시글 연관 파일 일괄 삭제, http://localhost:9102/attach/delete_by_bno?tno=1&bno=10
   * 
   * @param tno 메뉴 번호
   * @param bno 게시글 번호
   * @return 1: 삭제 완료
   */
  @DeleteMapping(path = "/delete_by_bno")
  public ResponseEntity<Integer> deleteByBno(
      @RequestParam(name = "tno", defaultValue = "0") Long tno,
      @RequestParam(name = "bno", defaultValue = "0") Long bno) {
    System.out.println("-> deleteByBno - tno: " + tno + ", bno: " + bno);
    this.attachService.deleteByTnoAndBno(tno, bno);
    return ResponseEntity.ok(1);
  }
}