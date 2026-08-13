package dev.jpa.allimio.aiissuemap;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @GetMapping("/{aiissueMapno}")
  public ResponseEntity<AiIssueMapDTO> read(
      @PathVariable("aiissueMapno") long aiissueMapno) {

    AiIssueMapDTO dto =
        aiIssueMapService.read(aiissueMapno);

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
  @GetMapping("/shopmap/{shopmapno}")
  public ResponseEntity<List<AiIssueMapDTO>> listByShopmapno(
      @PathVariable("shopmapno") long shopmapno) {

    return ResponseEntity.ok(
        aiIssueMapService.listByShopmapno(shopmapno)
    );
  }


  /**
   * AI 도면 데이터 등록
   *
   * POST /api/aiissuemaps
   */
  @PostMapping
  public ResponseEntity<AiIssueMapDTO> create(
      @RequestBody AiIssueMapDTO dto) {

    // 최초 등록 시 AI 이미지 생성 전 상태
    dto.setStatus(0);

    return ResponseEntity.ok(
        aiIssueMapService.create(dto)
    );
  }


  /**
   * AI 이미지 생성 결과 저장
   *
   * PATCH /api/aiissuemaps/{번호}/result
   */
  @PatchMapping("/{aiissueMapno}/result")
  public ResponseEntity<String> updateResult(
      @PathVariable("aiissueMapno") long aiissueMapno,
      @RequestBody Map<String, Object> body) {

    String fsaved =
        body.get("fsaved") == null
            ? null
            : body.get("fsaved").toString();

    int status =
        Integer.parseInt(body.get("status").toString());

    String err =
        body.get("err") == null
            ? null
            : body.get("err").toString();

    boolean result =
        aiIssueMapService.updateResult(
            aiissueMapno,
            fsaved,
            status,
            err
        );

    if (!result) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
        "AI 도면 생성 결과가 저장되었습니다."
    );
  }


  /**
   * 삭제
   *
   * DELETE /api/aiissuemaps/{번호}
   */
  @DeleteMapping("/{aiissueMapno}")
  public ResponseEntity<String> delete(
      @PathVariable("aiissueMapno") long aiissueMapno) {

    boolean result =
        aiIssueMapService.delete(aiissueMapno);

    if (!result) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
        "AI 이슈 도면이 삭제되었습니다."
    );
  }
}