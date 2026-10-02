package dev.jpa.allimio.shopmap;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import dev.jpa.allimio.shop.Shop;


/**
 * 매장 도면 Controller
 *
 * 원본 도면 파일은 H200 FastAPI 서버에 저장하고
 * SHOPMAP DB에는 파일 정보만 저장합니다.
 */
@RestController
@RequestMapping("/api/shopmaps")
public class ShopMapController {

  @Autowired
  private ShopMapService shopMapService;


  // ========================================
  // H200 FastAPI 주소
  // ========================================

  private static final String FASTAPI_URL =
      "http://139.150.91.194:11200/api/shopmap";


  private final RestTemplate restTemplate =
      new RestTemplate();


  // ========================================
  // 관리자용 전체 매장 도면 조회
  // ========================================

  @GetMapping
  public ResponseEntity<List<ShopMapDTO>> list() {

    List<ShopMapDTO> list =
        shopMapService.list();

    return ResponseEntity.ok(list);
  }


  // ========================================
  // 관리자 매장 + 도면 목록 조회
  // ========================================

  @GetMapping("/admin")
  public ResponseEntity<List<Shop>> adminShopMapList(
      @RequestParam(
          value = "keyword",
          required = false
      ) String keyword) {

    List<Shop> list =
        shopMapService.findShopMapJoin(keyword);

    return ResponseEntity.ok(list);
  }


  // ========================================
  // 매장번호로 도면 조회
  // ========================================

  @GetMapping("/shop/{sno}")
  public ResponseEntity<ShopMapDTO> readByNo(
      @PathVariable("sno") long sno) {

    ShopMapDTO dto =
        shopMapService.readByNo(sno);

    if (dto == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(dto);
  }


  // ========================================
  // 새로운 매장 도면 등록
  // ========================================

  @PostMapping
  public ResponseEntity<String> create(
      @RequestParam("sno") long sno,
      @RequestParam("file") MultipartFile file) {

    if (file.isEmpty()) {
      return ResponseEntity.badRequest()
          .body("도면 파일을 선택해주세요.");
    }


    // 이미 등록된 도면 확인
    ShopMapDTO existing =
        shopMapService.readByNo(sno);

    if (existing != null) {
      return ResponseEntity.badRequest()
          .body("이미 등록된 매장 도면이 있습니다.");
    }


    // 사용자가 업로드한 원본 파일명
    String fname =
        file.getOriginalFilename();


    // ----------------------------------------
    // H200에 원본 도면 업로드
    // ----------------------------------------

    String fsaved;

    try {

      fsaved = uploadToH200(file);

    } catch (Exception e) {

      e.printStackTrace();

      return ResponseEntity
          .internalServerError()
          .body(
              "H200 도면 파일 저장 중 오류가 발생했습니다."
          );
    }


    if (fsaved == null ||
        fsaved.isBlank()) {

      return ResponseEntity
          .internalServerError()
          .body(
              "H200 도면 파일 저장에 실패했습니다."
          );
    }


    // ----------------------------------------
    // DB 저장
    // ----------------------------------------

    ShopMapDTO dto =
        new ShopMapDTO();

    dto.setSno(sno);
    dto.setFname(fname);
    dto.setFsaved(fsaved);


    boolean result =
        shopMapService.create(dto);


    // DB 저장 실패 시
    // H200에 먼저 저장된 파일 제거
    if (!result) {

      deleteFromH200(fsaved);

      return ResponseEntity.badRequest()
          .body("매장 도면 등록에 실패했습니다.");
    }


    return ResponseEntity.ok(
        "매장 도면이 등록되었습니다."
    );
  }


  // ========================================
  // 기존 매장 도면 변경
  // ========================================

  @PutMapping("/{no}")
  public ResponseEntity<String> update(
      @PathVariable("no") long no,
      @RequestParam("file") MultipartFile file) {

    if (file.isEmpty()) {

      return ResponseEntity.badRequest()
          .body("도면 파일을 선택해주세요.");
    }


    // 기존 도면 DB 정보
    ShopMapDTO oldDto =
        shopMapService.read(no);

    if (oldDto == null) {
      return ResponseEntity.notFound().build();
    }


    String fname =
        file.getOriginalFilename();

    String fsaved;


    // ----------------------------------------
    // 새 도면 H200 업로드
    // ----------------------------------------

    try {

      fsaved = uploadToH200(file);

    } catch (Exception e) {

      e.printStackTrace();

      return ResponseEntity
          .internalServerError()
          .body(
              "H200 도면 파일 저장 중 오류가 발생했습니다."
          );
    }


    if (fsaved == null ||
        fsaved.isBlank()) {

      return ResponseEntity
          .internalServerError()
          .body(
              "H200 도면 파일 저장에 실패했습니다."
          );
    }


    // ----------------------------------------
    // DB 파일정보 변경
    // ----------------------------------------

    ShopMapDTO dto =
        new ShopMapDTO();

    dto.setFname(fname);
    dto.setFsaved(fsaved);


    boolean result =
        shopMapService.update(
            no,
            dto
        );


    // DB 수정 실패
    // 새로 업로드한 파일 삭제
    if (!result) {

      deleteFromH200(fsaved);

      return ResponseEntity.badRequest()
          .body("매장 도면 수정에 실패했습니다.");
    }


    // ----------------------------------------
    // DB 수정 성공 후
    // 이전 H200 파일 삭제
    // ----------------------------------------

    if (oldDto.getFsaved() != null &&
        !oldDto.getFsaved().isBlank()) {

      deleteFromH200(
          oldDto.getFsaved()
      );
    }


    return ResponseEntity.ok(
        "매장 도면이 수정되었습니다."
    );
  }


  // ========================================
  // 매장 도면 삭제
  // ========================================

  @DeleteMapping("/{no}")
  public ResponseEntity<String> delete(
      @PathVariable("no") long no) {

    ShopMapDTO dto =
        shopMapService.read(no);

    if (dto == null) {
      return ResponseEntity.notFound().build();
    }


    // ----------------------------------------
    // DB 삭제
    // ----------------------------------------

    boolean result =
        shopMapService.delete(no);

    if (!result) {

      return ResponseEntity.badRequest()
          .body("매장 도면 삭제에 실패했습니다.");
    }


    // ----------------------------------------
    // H200 실제 파일 삭제
    // ----------------------------------------

    if (dto.getFsaved() != null &&
        !dto.getFsaved().isBlank()) {

      deleteFromH200(
          dto.getFsaved()
      );
    }


    return ResponseEntity.ok(
        "매장 도면이 삭제되었습니다."
    );
  }


  // ========================================
  // 관리자 도면 다운로드
  // ========================================

  @GetMapping("/admin/download/{no}")
  public ResponseEntity<Resource> download(
      @PathVariable("no") long no) {

    ShopMapDTO dto =
        shopMapService.read(no);

    if (dto == null ||
        dto.getFsaved() == null) {

      return ResponseEntity
          .notFound()
          .build();
    }


    try {

      byte[] image =
          downloadFromH200(
              dto.getFsaved()
          );


      if (image == null) {

        return ResponseEntity
            .notFound()
            .build();
      }


      ByteArrayResource resource =
          new ByteArrayResource(image);


      return ResponseEntity.ok()
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" +
                  dto.getFname() +
                  "\""
          )
          .contentType(
              MediaType.APPLICATION_OCTET_STREAM
          )
          .contentLength(image.length)
          .body(resource);


    } catch (Exception e) {

      e.printStackTrace();

      return ResponseEntity
          .internalServerError()
          .build();
    }
  }


  // ========================================
  // 사용자 도면 이미지 조회
  // ========================================

  @GetMapping("/view/{no}")
  public ResponseEntity<Resource> view(
      @PathVariable("no") long no) {

    ShopMapDTO dto =
        shopMapService.read(no);

    if (dto == null ||
        dto.getFsaved() == null) {

      return ResponseEntity
          .notFound()
          .build();
    }


    try {

      byte[] image =
          downloadFromH200(
              dto.getFsaved()
          );


      if (image == null) {

        return ResponseEntity
            .notFound()
            .build();
      }


      ByteArrayResource resource =
          new ByteArrayResource(image);


      // 이미지 타입
      MediaType mediaType =
          getMediaType(dto.getFname());


      return ResponseEntity.ok()
          .contentType(mediaType)
          .contentLength(image.length)
          .body(resource);


    } catch (Exception e) {

      e.printStackTrace();

      return ResponseEntity
          .internalServerError()
          .build();
    }
  }


  // =====================================================
  // H200 도면 업로드
  // =====================================================

  @SuppressWarnings("unchecked")
  private String uploadToH200(
      MultipartFile file
  ) throws Exception {

    String url =
        FASTAPI_URL + "/upload";


    // MultipartFile -> Resource
    ByteArrayResource resource =
        new ByteArrayResource(
            file.getBytes()
        ) {

          @Override
          public String getFilename() {

            return file.getOriginalFilename();
          }
        };


    MultiValueMap<String, Object> body =
        new LinkedMultiValueMap<>();

    body.add(
        "file",
        resource
    );


    HttpHeaders headers =
        new HttpHeaders();

    headers.setContentType(
        MediaType.MULTIPART_FORM_DATA
    );


    HttpEntity<
        MultiValueMap<String, Object>
    > request = new HttpEntity<>(
        body,
        headers
    );


    ResponseEntity<Map> response =
        restTemplate.postForEntity(
            url,
            request,
            Map.class
        );


    if (!response
        .getStatusCode()
        .is2xxSuccessful()) {

      throw new RuntimeException(
          "H200 도면 업로드 실패"
      );
    }


    Map<String, Object> result =
        response.getBody();


    if (result == null) {

      throw new RuntimeException(
          "H200 응답 데이터 없음"
      );
    }


    Object filename =
        result.get("filename");


    if (filename == null) {

      throw new RuntimeException(
          "H200 저장 파일명 없음"
      );
    }


    return filename.toString();
  }


  // =====================================================
  // H200 도면 삭제
  // =====================================================

  private void deleteFromH200(
      String filename
  ) {

    try {

      String encodedFilename =
          UriUtils.encodePathSegment(
              filename,
              StandardCharsets.UTF_8
          );


      String url =
          FASTAPI_URL +
          "/" +
          encodedFilename;


      restTemplate.delete(url);


      System.out.println(
          "[SHOPMAP][H200][DELETE][SUCCESS] "
          + filename
      );


    } catch (Exception e) {

      // DB 작업까지 실패시키지 않도록
      // 삭제 실패는 로그만 기록
      System.out.println(
          "[SHOPMAP][H200][DELETE][FAIL] "
          + filename
      );

      System.out.println(
          "- 오류: " +
          e.getMessage()
      );
    }
  }


  // =====================================================
  // H200 도면 조회
  // =====================================================

  private byte[] downloadFromH200(
      String filename
  ) {

    try {

      String encodedFilename =
          UriUtils.encodePathSegment(
              filename,
              StandardCharsets.UTF_8
          );


      String url =
          FASTAPI_URL +
          "/file/" +
          encodedFilename;


      ResponseEntity<byte[]> response =
          restTemplate.exchange(
              URI.create(url),
              HttpMethod.GET,
              null,
              byte[].class
          );


      if (!response
          .getStatusCode()
          .is2xxSuccessful()) {

        return null;
      }


      return response.getBody();


    } catch (Exception e) {

      System.out.println(
          "[SHOPMAP][H200][READ][FAIL] "
          + filename
      );

      System.out.println(
          "- 오류: " +
          e.getMessage()
      );

      return null;
    }
  }


  // =====================================================
  // 이미지 Content-Type
  // =====================================================

  private MediaType getMediaType(
      String filename
  ) {

    if (filename == null) {

      return MediaType
          .APPLICATION_OCTET_STREAM;
    }


    String lower =
        filename.toLowerCase();


    if (lower.endsWith(".png")) {

      return MediaType.IMAGE_PNG;
    }


    if (lower.endsWith(".jpg") ||
        lower.endsWith(".jpeg")) {

      return MediaType.IMAGE_JPEG;
    }


    if (lower.endsWith(".gif")) {

      return MediaType.IMAGE_GIF;
    }


    return MediaType
        .APPLICATION_OCTET_STREAM;
  }
}