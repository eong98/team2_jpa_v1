package dev.jpa.allimio.shopmap;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import dev.jpa.allimio.tool.Tool;
import dev.jpa.allimio.tool.Upload;


/**
 * 매장 도면 Controller
 *
 * 매장 도면 등록, 조회, 수정, 삭제 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/shopmaps")
public class ShopMapController {

  @Autowired
  private ShopMapService shopMapService;


  /**
   * 매장 도면 파일 저장 경로
   *
   * 프로젝트에서 공통으로 사용하는 Tool.getServerDir()를 이용합니다.
   */
  private final String uploadPath = Tool.getServerDir("shopmap");

  /**
   * 관리자용 전체 매장 도면 조회
   *
   * GET /api/shopmaps
   *
   * @return 전체 도면 목록
   */
  @GetMapping
  public ResponseEntity<List<ShopMapDTO>> list() {

    List<ShopMapDTO> list = shopMapService.list();

    return ResponseEntity.ok(list);
  }


  /**
   * 매장번호로 도면 조회
   *
   * GET /api/shopmaps/shop/3
   *
   * @param sno 매장번호
   * @return 해당 매장의 도면 정보
   */
  @GetMapping("/shop/{sno}")
  public ResponseEntity<ShopMapDTO> readByNo(@PathVariable("sno") long sno) {

    ShopMapDTO dto = shopMapService.readByNo(sno);

    if (dto == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(dto);
  }


  /**
   * 새로운 매장 도면 등록
   *
   * POST /api/shopmaps
   *
   * sno   : 매장번호
   * file : 등록할 도면 이미지
   */
  @PostMapping
  public ResponseEntity<String> create(
      @RequestParam("sno") long sno,
      @RequestParam("file") MultipartFile file) {

    // 파일을 선택하지 않은 경우
    if (file.isEmpty()) {
      return ResponseEntity.badRequest()
          .body("도면 파일을 선택해주세요.");
    }

    // 해당 매장에 이미 도면이 있는지 확인
    ShopMapDTO existing = shopMapService.readByNo(sno);

    if (existing != null) {
      return ResponseEntity.badRequest()
          .body("이미 등록된 매장 도면이 있습니다.");
    }

    // 사용자가 올린 원본 파일명
    String fname = file.getOriginalFilename();

    // 서버에 실제 파일 저장
    String fsaved = Upload.saveFileSpring(file, uploadPath);

    // 파일 저장에 실패한 경우
    if (fsaved == null || fsaved.isBlank()) {
      return ResponseEntity.internalServerError()
          .body("도면 파일 저장 중 오류가 발생했습니다.");
    }

    // DB에 저장할 정보
    ShopMapDTO dto = new ShopMapDTO();

    dto.setNo(sno);
    dto.setFname(fname);
    dto.setFsaved(fsaved);

    boolean result = shopMapService.create(dto);

    if (!result) {
      return ResponseEntity.badRequest()
          .body("매장 도면 등록에 실패했습니다.");
    }

    return ResponseEntity.ok("매장 도면이 등록되었습니다.");
  }

  /**
   * 기존 매장 도면 변경
   *
   * PUT /api/shopmaps/{no}
   */
  @PutMapping("/{no}")
  public ResponseEntity<String> update(
      @PathVariable("no") long no,
      @RequestParam("file") MultipartFile file) {

    if (file.isEmpty()) {
      return ResponseEntity.badRequest()
          .body("도면 파일을 선택해주세요.");
    }

    // 기존 도면 정보 조회
    ShopMapDTO oldDto = shopMapService.read(no);

    if (oldDto == null) {
      return ResponseEntity.notFound().build();
    }

    // 새 파일 저장
    String fname = file.getOriginalFilename();
    String fsaved = Upload.saveFileSpring(file, uploadPath);

    if (fsaved == null || fsaved.isBlank()) {
      return ResponseEntity.internalServerError()
          .body("도면 파일 저장 중 오류가 발생했습니다.");
    }

    // DB에 새 파일 정보 저장
    ShopMapDTO dto = new ShopMapDTO();
    dto.setFname(fname);
    dto.setFsaved(fsaved);

    boolean result = shopMapService.update(no, dto);

    if (!result) {
      return ResponseEntity.badRequest()
          .body("매장 도면 수정에 실패했습니다.");
    }

    // 수정 성공 후 기존 파일 삭제
    File oldFile = new File(uploadPath + oldDto.getFsaved());

    System.out.println("기존 파일 경로: " + oldFile.getAbsolutePath());
    System.out.println("기존 파일 존재 여부: " + oldFile.exists());

    if (oldFile.exists()) {

      boolean deleted = oldFile.delete();

      System.out.println("기존 파일 삭제 결과: " + deleted);
    }

    return ResponseEntity.ok("매장 도면이 수정되었습니다.");
  }


  /**
   * 매장 도면 삭제
   *
   * DELETE /api/shopmaps/{no}
   */
  @DeleteMapping("/{no}")
  public ResponseEntity<String> delete(
      @PathVariable("no") long no) {

    // 삭제 전에 기존 도면 정보 조회
    ShopMapDTO dto = shopMapService.read(no);

    if (dto == null) {
      return ResponseEntity.notFound().build();
    }

    // DB 데이터 삭제
    boolean result = shopMapService.delete(no);

    if (!result) {
      return ResponseEntity.badRequest()
          .body("매장 도면 삭제에 실패했습니다.");
    }

    // DB 삭제 성공 후 실제 파일 삭제
    File file = new File(uploadPath + dto.getFsaved());

    System.out.println("삭제할 파일 경로: " + file.getAbsolutePath());
    System.out.println("삭제할 파일 존재 여부: " + file.exists());

    if (file.exists()) {

      boolean deleted = file.delete();

      System.out.println("파일 삭제 결과: " + deleted);
    }

    return ResponseEntity.ok("매장 도면이 삭제되었습니다.");
  }

  }