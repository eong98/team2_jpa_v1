package dev.jpa.allimio.aiissuemap;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 이슈 도면 Controller
 */
@RestController
@RequestMapping("/api/aiissuemaps")
public class AiIssueMapController {

  @Autowired
  private AiIssueMapService aiIssueMapService;


  /**
   * 전체 조회
   *
   * GET /api/aiissuemaps
   */
  @GetMapping
  public ResponseEntity<List<AiIssueMapDTO>> list() {

    return ResponseEntity.ok(
        aiIssueMapService.list()
    );
  }

  /**
   * 단건 조회
   *
   * GET /api/aiissuemaps/1
   */
  @GetMapping("/{no}")
  public ResponseEntity<AiIssueMapDTO> read(
      @PathVariable("no") long no) {

    AiIssueMapDTO dto =
        aiIssueMapService.read(no);

    if (dto == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(dto);
  }


  /**
   * 원본 매장 도면 번호로 조회
   *
   * GET /api/aiissuemaps/shopmap/1
   */
  @GetMapping("/shopmap/{smno}")
  public ResponseEntity<List<AiIssueMapDTO>> listByShopmapno(
      @PathVariable("smno") long smno) {

    return ResponseEntity.ok(
        aiIssueMapService.listBySmno(smno)
    );
  }

}