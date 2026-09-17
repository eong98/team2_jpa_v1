package dev.jpa.allimio.chatbot.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.chatbot.log.ChatLogDTO;
import dev.jpa.allimio.chatbot.log.ChatLogService;
import dev.jpa.allimio.chatbot.menu.ChatMenuDTO;
import dev.jpa.allimio.chatbot.menu.ChatMenuRepository;
import dev.jpa.allimio.chatbot.menu.ChatMenuService;
import dev.jpa.allimio.tool.Tool;

@Service
public class ChatSessionService {
  @Autowired
  ChatSessionRepository chatSessionRepository;
 
  @Autowired
  ChatMenuRepository chatMenuRepository;
 
  @Autowired
  ChatMenuService chatMenuService;
 
  @Autowired
  ChatLogService chatLogService;
 
  /** 상담 유형 상수 */
  /** 옵션 선택중 */
  private static final int MODE_OPTION = 0;
  /** AI 상담 진행 */
  private static final int MODE_AI = 1;
  /** 상담 종료 */
  private static final int MODE_CLOSED = 2;
 
  /** MTYPE(메시지 유형) 상수 (CHAT_LOG.MTYPE)  */
  /** 옵션 선택지 선택 */
  private static final int MTYPE_MENU_SELECT = 0;
  /** 답변노출 */
  private static final int MTYPE_ANSWER = 1;
  /** 자유텍스트 */
  private static final int MTYPE_FREE_TEXT = 2;
  /** 뒤로가기 */
  private static final int MTYPE_BACK = 3;
  /** AI답변 */
  private static final int MTYPE_AI_ANSWER = 4;
  /** 시스템안내 */
  private static final int MTYPE_SYSTEM_NOTICE = 5;
 
  /** 발화자 상수 */
  /** 사용자 */
  private static final int SENDER_USER = 0;
  /** AI */
  private static final int SENDER_AI = 1;
  /** 시스템 */
  private static final int SENDER_SYSTEM = 2;
  
  /**
   *  사용자 상담진행 유형 상수
   * 종료/만족도/불만족 사유 선택/불만족 사유 기타메모/관리자연결 진입/연결 확정
  */
  private static final int END = 0;
  private static final int SATISFY = 1;
  private static final int UNSATISFY_REASON = 2;
  private static final int UNSATISFY_MEMO = 3;
  private static final int ESCALATE = 4;
  private static final int ESCALATE_CONFIRM = 5;
  

  /**
   * 세션 생성. 최상위 옵션 클릭 또는 "AI에게 바로 물어보기" 클릭 시점에만 호출됩니다.
   * cmode=1(AI상담)로 생성하는 경우, greeting이 있으면 인사말까지 로그로 남깁니다.
   */
  @Transactional
  public ChatSessionDTO.ActionResult create(ChatSessionDTO.CreateRequest req, 
      String greeting) {
    String now = Tool.getDate();
 
    ChatSession session = ChatSession.builder()
        .no(UUID.randomUUID().toString())
        .mno(req.getMno())
        .gno(req.getGno())
        .cno(req.getCno())
        .cmode(req.getCmode() != null ? req.getCmode() : MODE_OPTION)
        .channel(req.getChannel() != null ? req.getChannel() : 10)
        .cdate(now)
        .udate(now)
        .build();
    ChatSession saved = chatSessionRepository.save(session);
 
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    logs.add(log(saved.getNo(), SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, greeting, null));
    
 
    return ChatSessionDTO.ActionResult.builder().no(saved.getNo()).logs(logs).build();
  }
 
  /**
   * 세션 상세 조회 (현재 위치한 메뉴명까지 조인).
   */
  @Transactional(readOnly = true)
  public ChatSessionDTO.Response findById(String no) {
    List<Object[]> result = chatSessionRepository.findByIdWithMenu(no);
    if (result.isEmpty()) throw new IllegalArgumentException("존재하지 않는 세션입니다. no=" + no);
    Object[] row = result.get(0);
    ChatSession session = (ChatSession) row[0];
    String cnoLabel = (String) row[1];
    return ChatSessionDTO.Response.from(session, cnoLabel);
  }
 
  /**
   * 진행 중인 세션 확인. 회원이면 mno, 비회원이면 gno로 조회합니다.
   */
  @Transactional(readOnly = true)
  public ChatSessionDTO.Response findActive(Long mno, String gno) {
    Optional<ChatSession> optional = mno != null
        ? chatSessionRepository.findActiveByMno(mno)
        : chatSessionRepository.findActiveByGno(gno);
    return optional.map(ChatSessionDTO.Response::from).orElse(null);
  }
 
  /**
   * 채팅방 목록 (요약). 최신 활동순으로 정렬됩니다.
   */
  @Transactional(readOnly = true)
  public List<ChatSessionDTO.Summary> getList(Long mno, String gno) {
    List<ChatSession> sessions = mno != null
        ? chatSessionRepository.findByMnoOrderByCmodeAscUdateDesc(mno)
        : chatSessionRepository.findByGnoOrderByCmodeAscUdateDesc(gno);
    return sessions.stream().map(this::toSummary).collect(Collectors.toList());
  }
 
  /**
   * 채팅방 목록에 노출될 요약 타이틀
   * 추후 ai 요약 기능 추가하여 반영
   * @param session
   * @return
   */
  private ChatSessionDTO.Summary toSummary(ChatSession session) {
    String title = "AI 상담";
    if (session.getCmode() == MODE_OPTION && session.getCno() != null) {
      title = chatMenuRepository.findById(session.getCno()).map(m -> m.getLabel()).orElse("상담");
    }
    return ChatSessionDTO.Summary.builder()
        .no(session.getNo()).title(title).cmode(session.getCmode()).udate(session.getUdate()).build();
  }
 
  /**
   * 옵션 선택 처리. CNO 갱신 + "선택함" 로그 + "답변" 로그를 저장하고,
   * 하위 선택지가 있으면 그것까지 조회해서 함께 반환합니다.
   */
  @Transactional
  public ChatSessionDTO.ActionResult selectMenu(String no, Long cno) {
    ChatSession session = getSession(no);
 
    ChatMenuDTO.Response menu = chatMenuService.selectMenu(cno);
 
    session.setCno(cno);
    session.setUdate(Tool.getDate());
 
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    logs.add(log(no, SENDER_USER, MTYPE_MENU_SELECT, menu.getLabel(), cno));
 
    String answer = menu.getAnswer() != null && !menu.getAnswer().isBlank() ? menu.getAnswer() : "서비스 준비 중입니다.";
    logs.add(log(no, SENDER_SYSTEM, MTYPE_ANSWER, answer, cno));
 
    List<ChatMenuDTO.Response> nextOptions = menu.getHasChildren()
        ? chatMenuService.getChildren(cno)
        : List.of();
 
    return ChatSessionDTO.ActionResult.builder()
        .logs(logs).nextOptions(nextOptions).hasChildren(menu.getHasChildren()).build();
  }
 
  /**
   * AI 상담 전환. no가 null이면 새 세션을 생성하며 시작하고, 있으면 모드만 전환합니다.
   * "AI 상담" 버튼 클릭 로그 + 인사말까지 저장합니다. (구분선은 저장하지 않음 — 프론트 전용 표시)
   * @param no null이면 새 세션 생성
   * @param greeting 프론트 SYSTEM_MESSAGES에서 고른 인사말 텍스트 그대로
   */
  @Transactional
  public ChatSessionDTO.ActionResult startAiConsult(String no, String startAi, String greeting) {
    ChatSession session = getSession(no);
 
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    logs.add(log(no, SENDER_USER, MTYPE_MENU_SELECT, "AI 상담", null));
 
    session.setCmode(MODE_AI);
    session.setCno(null);
    session.setUdate(Tool.getDate());

    logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, startAi, null));
    logs.add(log(no, SENDER_AI, MTYPE_AI_ANSWER, greeting, null));
 
    return ChatSessionDTO.ActionResult.builder().logs(logs).cmode(session.getCmode()).build();
  }
 
  /**
   * 다른 질문하기(처음으로). 옵션형 모드로 복귀.
   * (AI상담 종료 구분선은 저장하지 않음 — 프론트가 화면에만 표시)
   */
  @Transactional
  public ChatSessionDTO.ActionResult backToIntro(String no, String endAi, String greeting) {
    ChatSession session = getSession(no);
    boolean wasAi = session.getCmode() == MODE_AI;
 
    session.setCmode(MODE_OPTION);
    session.setCno(null);
    session.setUdate(Tool.getDate());
 
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    logs.add(log(no, SENDER_USER, MTYPE_BACK, "다른 질문하기", null));
    
    if (wasAi) {
      logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, endAi, null)); // 구분선도 저장
    }
    
    logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, greeting, null));
 
    return ChatSessionDTO.ActionResult.builder().logs(logs).cmode(session.getCmode()).build();
  }

  /**
   * 사용자 상담 진행 유형 통합 처리.
   * action으로 상황을 구분합니다. 
   */
  @Transactional
  public ChatSessionDTO.ActionResult step(String no, ChatSessionDTO.StepRequest req) {
    ChatSession session = getSession(no);
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    String sysMsg = req.getSystemMessage();
 
    switch (req.getAction()) {
      case END -> {
        logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, "상담 종료", null));
        logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      case SATISFY -> {
        int satisfy = req.getSatisfy();
        logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null));
        logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        
        // 만족 선택시 상담 종료
        if (satisfy == 1) {
          closeSession(session, 0, 1, null, null);
          return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
        }
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      case UNSATISFY_REASON -> {
        logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null));
        logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        
        // 불만족 사유 : 기타
        if (req.getSreason() == 9) {
          return ChatSessionDTO.ActionResult.builder().logs(logs).build();
        }
        closeSession(session, 0, 0, req.getSreason(), null);
        return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
      }
      case UNSATISFY_MEMO -> {
        logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getSmemo(), null));
        logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        closeSession(session, 0, 0, 9, req.getSmemo());
        return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
      }
      case ESCALATE -> {
        // 버튼 클릭 자체를 사용자 발화로 기록
        logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null)); 
        logs.add(log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      case ESCALATE_CONFIRM -> {
        logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null));
        // TODO: confirmed일 때 대화로그 기반 AI요약 → QA 등록화면 진입 연동
        if (req.getGoQa()) {
          closeSession(session, 1, null, null, null);
          return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
        }
        
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      default -> throw new IllegalArgumentException("알 수 없는 액션입니다: " + req.getAction());
    }
  }
 
  /**
   * AI 자유질문. 사용자 메시지 저장 + (임시 목업) AI 응답 저장.
   * TODO: 실제 RAG/LLM 연동 시 이 안의 answer/needsAdmin 계산 로직만 교체하면 됩니다.
   */
  @Transactional
  public ChatSessionDTO.ActionResult aiChat(String no, String message) {
    getSession(no);
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    logs.add(log(no, SENDER_USER, MTYPE_FREE_TEXT, message, null));
 
    // TODO: 실제 RAG/LLM 응답 API 연동
    String answer = "죄송합니다, 정확한 답변을 찾지 못했습니다.";
    boolean needsAdmin = true;
 
    logs.add(log(no, SENDER_AI, MTYPE_AI_ANSWER, answer, null));
 
    return ChatSessionDTO.ActionResult.builder().logs(logs).needsAdmin(needsAdmin).build();
  }
 
  /**
   * 세션 종료
   * @param session
   * @param creason
   * @param satisfy
   * @param sreason
   * @param smemo
   */
  private void closeSession(ChatSession session, Integer creason, Integer satisfy, Integer sreason, String smemo) {
    String now = Tool.getDate();
    session.setCmode(MODE_CLOSED);
    session.setClosedat(now);
    session.setUdate(now);
    session.setCreason(creason);
    session.setSatisfy(satisfy);
    session.setSreason(sreason);
    session.setSmemo(smemo);
  }
 
  private ChatSession getSession(String no) {
    return chatSessionRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. no=" + no));
  }
 
  /** CHAT_LOG.SNO(FK)에 값을 채워 로그를 저장하는 헬퍼. */
  private ChatLogDTO.Response log(String no, int sender, int mtype, String content, Long cno) {
    return chatLogService.create(ChatLogDTO.Request.builder()
        .sno(no).sender(sender).mtype(mtype).content(content).cno(cno).build());
  }
 
  /** 방치 세션 자동종료 (배치/타임아웃 감지용) */
  @Transactional
  public void autoClose(String no) {
    ChatSession session = chatSessionRepository.findById(no).orElse(null);
    if (session == null || session.getCmode() == MODE_CLOSED) return;
    closeSession(session, 2, null, null, null);
  }
}
 