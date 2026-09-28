package dev.jpa.allimio.chatbot.menu;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat_menu")
public class ChatMenuCont {
  @Autowired
  ChatMenuService chatMenuService;

  public ChatMenuCont() {
    System.out.println("-> ChatMenuCont created");
  }

  /**
   * 챗봇 첫 진입 시 노출할 최상위 선택지 목록
   * GET /chat_menu/root
   */
  @GetMapping("/root")
  public ResponseEntity<List<ChatMenuDTO.Response>> getRootMenus() {
    return ResponseEntity.ok(chatMenuService.getRootMenus());
  }

  /**
   * 특정 메뉴 선택 — 답변 + 하위선택지 유무 반환
   * GET /chat_menu/5
   */
  @GetMapping("/{no}")
  public ResponseEntity<ChatMenuDTO.Response> selectMenu(@PathVariable("no") Long no) {
    return ResponseEntity.ok(chatMenuService.selectMenu(no));
  }

  /**
   * 특정 메뉴의 하위 선택지 목록
   * GET /chat_menu/5/children
   */
  @GetMapping("/{pno}/children")
  public ResponseEntity<List<ChatMenuDTO.Response>> getChildren(@PathVariable("pno") Long pno) {
    return ResponseEntity.ok(chatMenuService.getChildren(pno));
  }

  /**
   * 관리자용 — 메뉴 등록
   * POST /chat_menu
   */
  @PostMapping
  public ResponseEntity<?> create(@RequestBody ChatMenuDTO.Request request) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(chatMenuService.create(request));
    } catch (IllegalStateException e) {
      // 최상위 메뉴 6개 초과
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }
  }

  /**
   * 관리자용 — 메뉴 수정
   * PUT /chat_menu/5
   */
  @PutMapping("/{no}")
  public ResponseEntity<ChatMenuDTO.Response> update(
      @PathVariable("no") Long no,
      @RequestBody ChatMenuDTO.Request request) {
    return ResponseEntity.ok(chatMenuService.update(no, request));
  }

  /**
   * 관리자용 — 공개/비공개(USEYN) 토글
   * PUT /chat_menu/5/useyn  body: { "useyn": "Y" }
   */
  @PutMapping("/{no}/useyn")
  public ResponseEntity<ChatMenuDTO.Response> toggleUseyn(
      @PathVariable("no") Long no,
      @RequestBody Map<String, String> body) {
    return ResponseEntity.ok(chatMenuService.toggleUseyn(no, body.get("useyn")));
  }

  /**
   * 관리자용 — 메뉴 삭제 (하위 선택지까지 일괄 삭제)
   * DELETE /chat_menu/5
   */
  @DeleteMapping("/{no}")
  public ResponseEntity<Void> delete(@PathVariable("no") Long no) {
    chatMenuService.delete(no);
    return ResponseEntity.noContent().build();
  }

  /**
   * 관리자용 — 전체 트리 조회 (트리뷰 렌더링용)
   * GET /chat_menu/tree/admin
   */
  @GetMapping("/tree/admin")
  public ResponseEntity<List<ChatMenuDTO.Response>> getFullTree() {
    return ResponseEntity.ok(chatMenuService.getFullTree());
  }
}