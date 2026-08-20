package dev.jpa.allimio.shop;

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
@RequestMapping("/shop")
public class ShopCont {
  @Autowired
  private ShopService shopService;

  public ShopCont() {
    System.out.println("-> ShopCont created.");
  }

  /**
   * 등록, http://localhost:9100/shop/save
   * @param shopDTO
   * @return
   */
  @PostMapping(path = "/save")
  public ResponseEntity<Shop> save(@RequestBody ShopDTO shopDTO) {
    Shop savedEntity = shopService.save(shopDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 전체 목록, http://localhost:9100/shop/find_all
   * @return
   */
  @GetMapping(path = "/find_all")
  public List<Shop> findAll() {
    List<Shop> list = shopService.findAll();

    return list;
  }

  /**
   * 매장 목록 검색 + 페이징 (/user/shop), http://localhost:9100/shop/search
   * mno(로그인 회원)는 필수, keyword는 선택 사항. 기본 정렬: cdate(등록일) 내림차순(최신순)
   * @param mno 회원번호 (필수, 세션의 로그인 회원)
   * @param keyword 매장명/주소/상세주소 포함 검색
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 6)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/search")
  public Map<String, Object> search(
      @RequestParam(value = "mno") long mno,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "6") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "cdate"));
    Page<Shop> result = shopService.search(mno, keyword, pageable);

    // 매장 카드에 "등록 CCTV 몇 대"를 보여주기 위해 CCTV 등록 대수를 붙여서 내려줍니다.
    List<ShopWithCctvCount> contentWithCctvCount = shopService.attachCctvCount(result.getContent());

    Map<String, Object> body = new HashMap<>();
    body.put("content", contentWithCctvCount);
    body.put("totalElements", result.getTotalElements());
    body.put("totalPages", result.getTotalPages());
    body.put("page", result.getNumber());
    body.put("size", result.getSize());

    return body;
  }

  /**
   * 관리자 매장 목록 검색 + 페이징 (/dbms/shop), http://localhost:9100/shop/admin/search
   * mno 상관없이 전체 매장 대상. mno를 넘기면 해당 회원 소유 매장만 조회.
   * @param mno 회원번호 (선택 사항)
   * @param keyword 매장명/주소/상세주소 포함 검색
   * @param page 0부터 시작하는 페이지 번호 (기본 0)
   * @param size 페이지당 개수 (기본 10)
   * @return content/totalElements/totalPages/page/size 를 담은 Map
   */
  @GetMapping(path = "/admin/search")
  public Map<String, Object> adminSearch(
      @RequestParam(value = "mno", required = false) Long mno,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "cdate"));
    Page<Shop> result = shopService.searchAdmin(mno, keyword, pageable);

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
   * http://localhost:9100/shop/9
   * @return
   */
  @GetMapping(path = "/{pk}")
  public ResponseEntity<Shop> findByIdRead(@PathVariable("pk") long pk) {
    Shop shop = shopService.findById(pk);

    if (shop != null) {
      return ResponseEntity.ok(shop);
    } else {
      return ResponseEntity.notFound().build(); // 404
    }
  }

  /**
   * 수정, http://localhost:9100/shop/update
   * @param shopDTO
   * @return
   */
  @PutMapping(path = "/update")
  public ResponseEntity<Shop> update(@RequestBody ShopDTO shopDTO) {
    Shop savedEntity = shopService.update(shopDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 삭제, http://localhost:9100/shop/9
   * @param pk
   * @return
   */
  @DeleteMapping(path = "/{pk}")
  public ResponseEntity<Void> delete(@PathVariable("pk") long pk) {
    boolean sw = shopService.deleteById(pk);

    if (sw) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}