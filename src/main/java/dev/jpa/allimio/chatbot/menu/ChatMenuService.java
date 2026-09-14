package dev.jpa.allimio.chatbot.menu;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.tool.Tool;

@Service
public class ChatMenuService {
  @Autowired
  ChatMenuRepository chatMenuRepository;

  /**
   * 챗봇 첫 진입 시 노출할 최상위 선택지 목록.
   * @return STEP1 메뉴 목록 (사용중인 것만, 노출순서 정렬)
   */
  public List<ChatMenuDTO.Response> getRootMenus() {
    return chatMenuRepository.findByPnoIsNullAndUseynOrderByVseqAsc("Y")
        .stream()
        .map(m -> ChatMenuDTO.Response.from(m, chatMenuRepository.existsByPnoAndUseyn(m.getNo(), "Y")))
        .collect(Collectors.toList());
  }

  /**
   * 특정 메뉴 클릭 시, 그 답변과 하위 선택지 목록을 함께 반환.
   * @param no 클릭한 메뉴 번호
   * @return 클릭한 메뉴 정보(answer 포함) + hasChildren으로 하위선택지 유무 표시
   */
  @Transactional(readOnly = true)
  public ChatMenuDTO.Response selectMenu(Long no) {
    ChatMenu menu = chatMenuRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다. no=" + no));

    boolean hasChildren = chatMenuRepository.existsByPnoAndUseyn(no, "Y");
    return ChatMenuDTO.Response.from(menu, hasChildren);
  }

  /**
   * 특정 부모의 하위 선택지 목록.
   * @param pno 부모 메뉴 번호
   * @return 하위 선택지 목록
   */
  public List<ChatMenuDTO.Response> getChildren(Long pno) {
    return chatMenuRepository.findByPnoAndUseynOrderByVseqAsc(pno, "Y")
        .stream()
        .map(m -> ChatMenuDTO.Response.from(m, chatMenuRepository.existsByPnoAndUseyn(m.getNo(), "Y")))
        .collect(Collectors.toList());
  }

  /**
   * 관리자용 — 메뉴 등록.
   */
  @Transactional
  public ChatMenuDTO.Response create(ChatMenuDTO.Request request) {
    ChatMenu menu = ChatMenu.builder()
        .pno(request.getPno())
        .step(request.getStep())
        .label(request.getLabel())
        .answer(request.getAnswer())
        .userag(request.getUserag())
        .vseq(request.getVseq())
        .useyn(request.getUseyn())
        .cdate(Tool.getDate())
        .build();

    ChatMenu saved = chatMenuRepository.save(menu);
    return ChatMenuDTO.Response.from(saved);
  }

  /**
   * 관리자용 — 메뉴 수정.
   */
  @Transactional
  public ChatMenuDTO.Response update(Long no, ChatMenuDTO.Request request) {
    ChatMenu menu = chatMenuRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다. no=" + no));

    menu.setPno(request.getPno());
    menu.setStep(request.getStep());
    menu.setLabel(request.getLabel());
    menu.setAnswer(request.getAnswer());
    menu.setUserag(request.getUserag());
    menu.setVseq(request.getVseq());
    menu.setUseyn(request.getUseyn());

    return ChatMenuDTO.Response.from(menu); // Dirty Checking으로 자동 반영
  }

  /**
   * 관리자용 — 메뉴 삭제. 하위 선택지가 있으면 FK 제약으로 실패하니,
   * 먼저 하위 선택지 존재 여부를 확인해서 안내합니다.
   */
  @Transactional
  public void delete(Long no) {
    if (chatMenuRepository.existsByPnoAndUseyn(no, "Y") || chatMenuRepository.existsByPnoAndUseyn(no, "N")) {
      throw new IllegalStateException("하위 선택지가 있는 메뉴는 삭제할 수 없습니다. 먼저 하위 선택지를 삭제해주세요.");
    }
    chatMenuRepository.deleteById(no);
  }

  /**
   * 관리자용 — 전체 메뉴 검색 + 페이징.
   */
  public Page<ChatMenuDTO.Response> searchAllAdmin(ChatMenuDTO.SearchRequest req, Pageable pageable) {
    Page<ChatMenu> result = chatMenuRepository.searchAllAdmin(req.getWord(), req.getStep(), req.getUseyn(), pageable);
    return result.map(ChatMenuDTO.Response::from);
  }

  /**
   * 관리자용 — 전체 트리 조회 (관리 화면 트리뷰용, 페이징 없음).
   */
  public List<ChatMenuDTO.Response> getFullTree() {
    return chatMenuRepository.findAllByOrderByStepAscPnoAscVseqAsc()
        .stream().map(ChatMenuDTO.Response::from).collect(Collectors.toList());
  }
}