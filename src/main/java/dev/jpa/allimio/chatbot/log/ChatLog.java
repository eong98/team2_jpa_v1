package dev.jpa.allimio.chatbot.log;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 챗봇대화로그 Entity (세션 안의 메시지 시간순 기록)
 */
@Entity
@Table(name = "CHAT_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatLog {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_log_seq_use")
  @SequenceGenerator(name = "chat_log_seq_use", sequenceName = "CHAT_LOG_SEQ", allocationSize = 1)
  /** 대화 로그 번호 (PK) */
  private Long no;

  /** 현재 세션 식별키 (UUID, FK) */
  private String sno;

  /** 발송자 유형 (0: 사용자, 1: AI, 2: 시스템) */
  private Integer sender;

  /** 메시지 유형 (0: 선택지선택, 1: 답변노출, 2: 자유텍스트, 3: 뒤로가기, 4: AI답변, 5: 시스템안내) */
  private Integer mtype;

  /** 화면에 보일 메시지 내용 */
  private String content;

  /** 선택지 관련 메시지면 그 메뉴 노드 번호 (FK -> CHAT_MENU.NO) */
  private Long cno;

  /** 로그 작성일시 */
  private String cdate;
}