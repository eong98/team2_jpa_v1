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
   *  사용자 상담진행 대기 상수
   * 0: 만족도평가, 1: 불만족이유, 2: 불만족 이유 작성, 3: 관리자 문의노출, 4: AI 답변실패
  */
  private static final int ENDFLOW_ASK_SATISFY = 0;
  private static final int ENDFLOW_ASK_UNSATISFY_REASON = 1;
  private static final int ENDFLOW_ASK_UNSATISFY_MEMO = 2;
  private static final int ENDFLOW_ASK_ESCALATE_CONFIRM = 3;
  private static final int ENDFLOW_FAIL_AI_ANSWER= 4;
  
  
  /**
   * 채팅방을 읽음 처리. 세션 조회(진입) 시점에 호출되어 READAT을 현재 시각으로 갱신합니다.
   */
  @Transactional
  public void markAsRead(String no) {
    chatSessionRepository.updateReadAt(no, Tool.getDate());
  }
 
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
        .readat(now)
        .build();
    ChatSession saved = chatSessionRepository.save(session);
 
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    if (greeting != null && !greeting.isBlank()) {  // null/빈 문자열이면 로그 자체를 생략
      addIfPresent(logs, log(saved.getNo(), SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, greeting, null));
    }
    
 
    return ChatSessionDTO.ActionResult.builder().no(saved.getNo()).logs(logs).build();
  }
 
  /**
   * 세션 상세 조회 (현재 위치한 메뉴명까지 조인).
   */
  @Transactional
  public ChatSessionDTO.Response findById(String no) {
    List<Object[]> result = chatSessionRepository.findByIdWithMenu(no);
    if (result.isEmpty()) throw new IllegalArgumentException("존재하지 않는 세션입니다. no=" + no);
    Object[] row = result.get(0);
    ChatSession session = (ChatSession) row[0];
    String cnoLabel = (String) row[1];
    
    markAsRead(session.getNo());
    
    return ChatSessionDTO.Response.from(session, cnoLabel);
  }
 
  /**
   * 진행 중인 세션 확인. 회원이면 mno, 비회원이면 gno로 조회합니다.
   */
  @Transactional
  public ChatSessionDTO.Response findActive(Long mno, String gno) {
    if (mno == null && (gno == null || gno.isBlank())) return null;
    Optional<ChatSession> optional = mno != null
        ? chatSessionRepository.findFirstByMnoAndCmodeNotOrderByUdateDesc(mno, MODE_CLOSED)
        : chatSessionRepository.findFirstByGnoAndCmodeNotOrderByUdateDesc(gno, MODE_CLOSED);
    
    optional.ifPresent(session -> session.setReadat(Tool.getDate()));
    
    return optional.map(ChatSessionDTO.Response::from).orElse(null);
  }
 
  /**
   * 채팅방 목록 (요약). 최신 활동순으로 정렬됩니다.
   */
  @Transactional(readOnly = true)
  public List<ChatSessionDTO.Summary> getList(Long mno, String gno) {
    if (mno == null && (gno == null || gno.isBlank())) return List.of();
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
    String title;
    if (session.getStitle() != null && !session.getStitle().isBlank()) {
      title = session.getStitle(); // AI요약 제목이 있으면 최우선
    } else if (session.getCmode() == MODE_OPTION && session.getCno() != null) {
      title = chatMenuRepository.findById(session.getCno()).map(m -> m.getLabel()).orElse("상담");
    } else {
      title = "AI 상담";
    }
    return ChatSessionDTO.Summary.builder()
        .no(session.getNo())
        .stitle(title)
        .cmode(session.getCmode())
        .udate(session.getUdate())
        .readat(session.getReadat())
        .endflow(session.getEndflow()) // 이게 있는지 확인 필요
        .build();
  }
 
  /**
   * 옵션 선택 처리. CNO 갱신 + "선택함" 로그 + "답변" 로그를 저장하고,
   * 하위 선택지가 있으면 그것까지 조회해서 함께 반환합니다.
   */
  @Transactional
  public ChatSessionDTO.ActionResult selectMenu(String no, Long cno) {
    ChatSession session = getOpenSession(no);
    if (cno == null) throw new IllegalArgumentException("선택한 메뉴 번호가 없습니다.");
 
    ChatMenuDTO.Response menu = chatMenuService.selectMenu(cno);
 
    session.setCno(cno);
    session.setUdate(Tool.getDate());
    session.setReadat(Tool.getDate());
 
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    addIfPresent(logs, log(no, SENDER_USER, MTYPE_MENU_SELECT, menu.getLabel(), cno));
 
    String answer = menu.getAnswer() != null && !menu.getAnswer().isBlank() ? menu.getAnswer() : "서비스 준비 중입니다.";
    addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_ANSWER, answer, cno));
 
    List<ChatMenuDTO.Response> nextOptions = menu.getHasChildren()
        ? chatMenuService.getChildren(cno)
        : List.of();
 
    return ChatSessionDTO.ActionResult.builder()
        .logs(logs).nextOptions(nextOptions).hasChildren(menu.getHasChildren()).build();
  }

  /**
   * 다른 질문하기(처음으로). 옵션형 모드로 복귀.
   */
  @Transactional
  public ChatSessionDTO.ActionResult backToIntro(String no, String greeting) { // endAi 파라미터 제거
   ChatSession session = getOpenSession(no);
  
   session.setCmode(MODE_OPTION);
   session.setCno(null);
   session.setUdate(Tool.getDate());
   session.setReadat(Tool.getDate());
  
   List<ChatLogDTO.Response> logs = new ArrayList<>();
   addIfPresent(logs, log(no, SENDER_USER, MTYPE_BACK, "다른 질문하기", null));
   addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, greeting, null)); // 구분선 로직 제거됨
  
   return ChatSessionDTO.ActionResult.builder().logs(logs).cmode(session.getCmode()).build();
  }

  /**
   * 사용자 상담 진행 유형 통합 처리.
   * action으로 상황을 구분합니다. 
   */
  @Transactional
  public ChatSessionDTO.ActionResult step(String no, ChatSessionDTO.StepRequest req) {
    if (req.getAction() == null) throw new IllegalArgumentException("action 값이 없습니다.");
    // 이미 종료된 세션에 다시 요청(버튼 연타, 다른 탭) → 로그 중복/재종료 방지
    ChatSession session = getOpenSession(no);
    List<ChatLogDTO.Response> logs = new ArrayList<>();
    String sysMsg = req.getSystemMessage();
    session.setReadat(Tool.getDate());
 
    switch (req.getAction()) {
      case END -> {
        addIfPresent(logs, log(no, SENDER_USER, MTYPE_FREE_TEXT, "상담 종료", null));
        addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        session.setEndflow(ENDFLOW_ASK_SATISFY);
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      case SATISFY -> {
        if (req.getSatisfy() == null) throw new IllegalArgumentException("만족도 값이 없습니다.");
        int satisfy = req.getSatisfy();
        addIfPresent(logs, log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null));
        addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        
        // 만족 선택시 상담 종료
        if (satisfy == 1) {
          closeSession(session, 0, 1, null, null);
          session.setEndflow(null); // 종료됐으니 대기상태 해제
          return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
        }
        session.setEndflow(ENDFLOW_ASK_UNSATISFY_REASON);
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      case UNSATISFY_REASON -> {
        addIfPresent(logs, log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null));
        addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        
        if (req.getSreason() == null) throw new IllegalArgumentException("불만족 사유 값이 없습니다.");
        // 불만족 사유 : 기타
        if (req.getSreason() == 9) {
          session.setEndflow(ENDFLOW_ASK_UNSATISFY_MEMO); // 추가
          return ChatSessionDTO.ActionResult.builder().logs(logs).build();
        }
        closeSession(session, 0, 0, req.getSreason(), null);
        session.setEndflow(null);
        return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
      }
      case UNSATISFY_MEMO -> {
        addIfPresent(logs, log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getSmemo(), null));
        addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        closeSession(session, 0, 0, 9, req.getSmemo());
        session.setEndflow(null);
        return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
      }
      case ESCALATE -> {
        // 버튼 클릭 자체를 사용자 발화로 기록
        addIfPresent(logs, log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null)); 
        addIfPresent(logs, log(no, SENDER_SYSTEM, MTYPE_SYSTEM_NOTICE, sysMsg, null));
        session.setEndflow(ENDFLOW_ASK_ESCALATE_CONFIRM); // 추가
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      case ESCALATE_CONFIRM -> {
        addIfPresent(logs, log(no, SENDER_USER, MTYPE_FREE_TEXT, req.getLabel(), null));
        // TODO: confirmed일 때 대화로그 기반 AI요약 → QA 등록화면 진입 연동
        if (Boolean.TRUE.equals(req.getGoQa())) {
          closeSession(session, 1, null, null, null);
          session.setEndflow(null); // 추가
          return ChatSessionDTO.ActionResult.builder().logs(logs).sessionEnded(true).build();
        }

        session.setEndflow(null); // 아니오 선택 시 대기상태 해제, 대화 계속
        return ChatSessionDTO.ActionResult.builder().logs(logs).build();
      }
      default -> throw new IllegalArgumentException("알 수 없는 액션입니다: " + req.getAction());
    }
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
    session.setReadat(Tool.getDate());
    session.setCreason(creason);
    session.setSatisfy(satisfy);
    session.setSreason(sreason);
    session.setSmemo(smemo);
    session.setEndflow(null);
  }
 
  private ChatSession getSession(String no) {
    return chatSessionRepository.findById(no)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. no=" + no));
  }

  /** 내용이 비어 저장을 생략한 로그(null)는 응답 목록에서 제외 */
  private static void addIfPresent(List<ChatLogDTO.Response> logs, ChatLogDTO.Response log) {
    if (log != null) logs.add(log);
  }

  /** 진행 중인 세션만 — 종료된 세션에 대한 선택/단계 요청은 409로 거부 */
  private ChatSession getOpenSession(String no) {
    ChatSession session = getSession(no);
    if (session.getCmode() != null && session.getCmode() == MODE_CLOSED) {
      throw new IllegalStateException("이미 종료된 상담입니다.");
    }
    return session;
  }
 
  /** CHAT_LOG.SNO(FK)에 값을 채워 로그를 저장하는 헬퍼. */
  private ChatLogDTO.Response log(String no, int sender, int mtype, String content, Long cno) {
    if (content == null || content.isBlank()) return null;
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
 