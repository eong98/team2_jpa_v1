package dev.jpa.allimio.chatbot.menu;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.chatbot.log.ChatLogRepository;
import dev.jpa.allimio.chatbot.session.ChatSessionRepository;
import dev.jpa.allimio.tool.Tool;

@Service
public class ChatMenuService {
  @Autowired
  ChatMenuRepository chatMenuRepository;

  @Autowired
  ChatSessionRepository chatSessionRepository;

  @Autowired
  ChatLogRepository chatLogRepository;

  /** 관리자가 직접 등록하는 메뉴는 항상 AI관리대상 아님(N)으로 저장 */
  private static final String AIYN_MANUAL = "N";

  /** 최상위(STEP1) 메뉴 최대 개수 — 챗봇 첫 화면 선택지. FastAPI manual_doc.MAX_CATEGORIES와 같은 값 */
  public static final int MAX_TOP_MENUS = 6;

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
   * 관리자용 — 메뉴 등록. 관리자가 직접 등록하는 메뉴는 항상 AIYN='N'
   * (AI 관리 대상 아님)으로 저장되어, [AI 옵션생성] 재실행 시에도 보존된다.
   */
  @Transactional
  public ChatMenuDTO.Response create(ChatMenuDTO.Request request) {
    if (request.getPno() == null && chatMenuRepository.countByPnoIsNullAndAiyn(AIYN_MANUAL) >= MAX_TOP_MENUS) {
      throw new IllegalStateException("최상위 메뉴는 최대 " + MAX_TOP_MENUS + "개까지 등록할 수 있습니다.");
    }
    ChatMenu menu = ChatMenu.builder()
        .pno(request.getPno())
        .step(request.getStep())
        .label(request.getLabel())
        .answer(request.getAnswer())
        .vseq(request.getVseq())
        .useyn(request.getUseyn())
        .aiyn(AIYN_MANUAL)
        .cdate(Tool.getDate())
        .build();

    ChatMenu saved = chatMenuRepository.save(menu);
    return ChatMenuDTO.Response.from(saved);
  }

  /**
   * 관리자용 — 메뉴 수정. AIYN은 건드리지 않는다 — AI가 생성한 메뉴의
   * 텍스트를 관리자가 고치더라도, 여전히 AI 관리 대상(AIYN='Y')으로 남아서
   * [AI 옵션생성]을 다시 누르면 이 수정 내용도 새로 생성된 내용으로
   * 교체될 수 있다(의도된 동작).
   */
  @Transactional
  public ChatMenuDTO.Response update(Long no, ChatMenuDTO.Request request) {
    ChatMenu menu = chatMenuRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다. no=" + no));

    menu.setPno(request.getPno());
    menu.setStep(request.getStep());
    menu.setLabel(request.getLabel());
    menu.setAnswer(request.getAnswer());
    menu.setVseq(request.getVseq());
    menu.setUseyn(request.getUseyn());

    return ChatMenuDTO.Response.from(menu); // Dirty Checking으로 자동 반영
  }

  /**
   * 관리자용 — 사용여부(공개/비공개)만 토글. 옵션메뉴관리 화면의
   * 공개/비공개 버튼에서 호출된다. 기존 관리자 작성 메뉴, AI생성 메뉴
   * 구분 없이 전부 이 메서드로 처리 가능하다.
   */
  @Transactional
  public ChatMenuDTO.Response toggleUseyn(Long no, String useyn) {
    ChatMenu menu = chatMenuRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다. no=" + no));

    menu.changeUseyn(useyn);
    return ChatMenuDTO.Response.from(menu);
  }

  /**
   * 관리자용 — 메뉴 삭제. 하위 선택지(STEP2, STEP3)까지 한 번에 삭제합니다.
   * PNO가 자기참조 FK라서, 가장 깊은 하위부터 순서대로 지운 뒤 자신을 지웁니다.
   * @return 삭제된 메뉴 개수(자신 포함)
   */
  @Transactional
  public int delete(Long no) {
    if (!chatMenuRepository.existsById(no)) {
      throw new IllegalArgumentException("존재하지 않는 메뉴입니다. no=" + no);
    }

    // BFS로 하위 전체 수집 → 뒤집으면 깊은 노드가 앞에 옴
    List<Long> ordered = new ArrayList<>();
    Deque<Long> queue = new ArrayDeque<>();
    queue.add(no);
    while (!queue.isEmpty()) {
      Long current = queue.poll();
      ordered.add(current);
      chatMenuRepository.findByPno(current).forEach(child -> queue.add(child.getNo()));
    }
    Collections.reverse(ordered);

    // CHAT_SESSION.CNO, CHAT_LOG.CNO가 이 메뉴들을 FK로 참조하고 있으면 삭제가 막히므로 먼저 해제
    chatSessionRepository.clearCno(ordered);
    chatLogRepository.clearCno(ordered);

    for (Long target : ordered) {
      chatMenuRepository.deleteById(target);
      chatMenuRepository.flush(); // 자식 DELETE가 부모보다 먼저 실행되도록 즉시 반영
    }
    return ordered.size();
  }

  /**
   * 관리자용 — 전체 트리 조회 (관리 화면 트리뷰용, 페이징 없음).
   */
  public List<ChatMenuDTO.Response> getFullTree() {
    return chatMenuRepository.findAllByOrderByStepAscPnoAscVseqAsc()
        .stream().map(ChatMenuDTO.Response::from).collect(Collectors.toList());
  }
}