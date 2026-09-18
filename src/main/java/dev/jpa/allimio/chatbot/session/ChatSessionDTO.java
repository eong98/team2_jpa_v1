package dev.jpa.allimio.chatbot.session;

import java.util.List;

import dev.jpa.allimio.chatbot.log.ChatLogDTO;
import dev.jpa.allimio.chatbot.menu.ChatMenuDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatSessionDTO {
 
  /** 세션 생성 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CreateRequest {
    /** 회원번호 (회원이면 값 있음, 비회원이면 NULL) */
    private Long mno;
    /** 비회원 식별용 UUID (MNO가 NULL일 때 사용) */
    private String gno;
    /** 접속 채널 (10: WEB, 20: MOBILE), 미지정 시 10으로 처리 */
    private Integer channel;
    /** 시작 모드 (0 옵션형, 1 AI상담) */
    private Integer cmode;
    /** cmode=0일 때, 선택한 최상위 메뉴 번호 */
    private Long cno;
    /** 시스템/AI 인사말 */
    private String greeting;
  }
 
  /** 옵션 선택 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SelectRequest {
    /** 선택한 메뉴 번호 (FK -> CHAT_MENU.NO) */
    private Long cno;
  }
 

  /**
   * 상담 진행
   * (종료/만족도/불만족 사유 선택/불만족 사유 기타 - 메모/관리자연결 진입/연결 확정)
   * action 값으로 하나를 지정합니다.
   *
   * systemMessage는 UNSATISFY_REASONS의 label과 동일한 방식 — 프론트
   * SYSTEM_MESSAGES 배열에서 code로 찾은 label(텍스트)을 그대로 보내고,
   * 백엔드는 검증 없이 그 텍스트를 그대로 로그에 저장합니다. 백엔드에
   * 별도 문구 매핑 클래스는 두지 않습니다.
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class StepRequest {
    /** 상담진행 유형 */
    private Integer action;
    /** 만족도 */
    private Integer satisfy;
    /** 불만족 사유 코드 */
    private Integer sreason;
    /** 선택된 라벨 */
    private String label;
    /** 불만족 기타 사유 직접입력 내용 */
    private String smemo;
    /** 관리자 연결 여부(네/아니오) */
    private Boolean goQa;
    /** 이 액션의 결과로 저장할 시스템 안내 문구 (SYSTEM_MESSAGES에서 찾은 label 그대로) */
    private String systemMessage;
  }
 
  /** AI 자유질문 요청 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AiChatRequest {
    /** 사용자가 입력한 자유텍스트 질문 */
    private String message;
  }
 
  /**
   * 액션 처리 결과 — 이번 액션으로 새로 생성된 로그들 + (옵션형이면) 다음 선택지 목록.
   * 프론트는 logs를 그대로 화면 말풍선으로 그리기만 하면 됩니다.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ActionResult {
    /** 세션 식별키 — 세션 생성 액션(create, startAiConsult(no=null))에서만 채워짐 */
    private String no;
    /** 이 액션 이후 세션의 현재 모드 (0 옵션형, 1 AI상담, 2 종료) */
    private Integer cmode;
    /** 이번 액션으로 새로 생성된 로그들 (화면에 순서대로 이어붙이면 됨) */
    private List<ChatLogDTO.Response> logs;
    /** 다음 단계 하위 선택지 (옵션형 selectMenu 응답일 때만 채워짐) */
    private List<ChatMenuDTO.Response> nextOptions;
    /** 방금 선택한 메뉴에 하위 선택지가 있는지 */
    private boolean hasChildren;
    /** 이 액션으로 세션이 종료(CMODE=2)됐는지 */
    private boolean sessionEnded;
    /** AI 가 대답하지 못하는 질문인 경우 관리자 연결 판단 */
    private boolean needsAdmin;
  }
 
  /** 세션 응답 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    /** 세션식별키 (UUID, PK) */
    private String no;
    /** 회원번호 (비회원이면 NULL) */
    private Long mno;
    /** 비회원 식별용 UUID */
    private String gno;
    /** 현재 위치한 옵션형 메뉴 (AI상담중이면 NULL) */
    private Long cno;
    /** 진행모드 (0 옵션형진행중, 1 AI상담진행중, 2 종료) */
    private Integer cmode;
    /** 접속 채널 (10 WEB, 20 MOBILE) */
    private Integer channel;
    /** 관리자연결시 생성된 QA 문의글 번호 */
    private Long qno;
    /** 상담 세션 시작일시 */
    private String cdate;
    /** 마지막 활동 시각 */
    private String udate;
    /** 실제 종료 시각 (CMODE=2 전환 시점) */
    private String closedat;
    /** 세션 종료 사유 (0 해결됨, 1 관리자연결, 2 자동종료) */
    private Integer creason;
    /** 상담 만족도 (0 불만족, 1 만족, NULL 미응답) */
    private Integer satisfy;
    /** 불만족 사유 코드 */
    private Integer sreason;
    /** 불만족 사유 기타 직접입력 */
    private String smemo;
    private String stitle;
    private String readat;
    private Integer endflow;
    
    /** 현재 위치한 메뉴명 (join) — 채팅방 헤더 타이틀 등에 사용 */
    private String cnoLabel;
 
    public static Response from(ChatSession entity) {
      if (entity == null) return null;
      return Response.builder()
          .no(entity.getNo())
          .mno(entity.getMno())
          .gno(entity.getGno())
          .cno(entity.getCno())
          .cmode(entity.getCmode())
          .channel(entity.getChannel())
          .qno(entity.getQno())
          .cdate(entity.getCdate())
          .udate(entity.getUdate())
          .closedat(entity.getClosedat())
          .creason(entity.getCreason())
          .satisfy(entity.getSatisfy())
          .sreason(entity.getSreason())
          .smemo(entity.getSmemo())
          .stitle(entity.getStitle())
          .readat(entity.getReadat())
          .endflow(entity.getEndflow())
          .build();
    }
 
    public static Response from(ChatSession entity, String cnoLabel) {
      Response response = from(entity);
      if (response != null) response.setCnoLabel(cnoLabel);
      return response;
    }
  }

  
  /** 목록용 요약 응답 (채팅방 목록 화면) */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Summary {
    /** 세션식별키 (UUID) */
    private String no;
    /** 대표 타이틀 — 옵션형이면 현재 메뉴명, AI상담이면 "AI 상담" */
    private String stitle;
    /** 진행모드 (0 옵션형진행중, 1 AI상담진행중, 2 종료) */
    private Integer cmode;
    /** 마지막 활동 시각 — 목록 정렬 기준 */
    private String udate;
    /** 마지막으로 읽은 시각. udate보다 이전(또는 NULL)이면 안읽음 상태 */
    private String readat;
  }
}