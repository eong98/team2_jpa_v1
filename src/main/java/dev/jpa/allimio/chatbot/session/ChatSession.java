package dev.jpa.allimio.chatbot.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 챗봇상담 세션 Entity (상담 1건 단위)
 *
 * CHAT_SESSION 테이블과 연결됩니다. PK(NO)는 UUID이며 자동채번이 아니라
 * 서비스에서 직접 생성해서 넣습니다.
 */
@Entity
@Table(name = "CHAT_SESSION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
  @Id
  /** 세션식별키 (UUID, PK) */
  private String no;

  /** 회원번호 (비회원: NULL, GNO 부여) */
  private Long mno;

  /** 비회원 식별용 UUID */
  private String gno;

  /** 현재 위치한 옵션형 메뉴 (AI상담중인 경우 NULL) */
  private Long cno;

  /** 진행모드 (0: 옵션형 진행중, 1: AI상담 진행중, 2: 종료) */
  @Builder.Default
  private Integer cmode = 0;

  /** 접속 채널 (10: WEB, 20: MOBILE) */
  @Builder.Default
  private Integer channel = 10;

  /** 관리자연결시 생성될 QA 문의글 번호 */
  private Long qno;

  /** 상담 세션 시작일시 */
  private String cdate;

  /** 마지막 활동 시각 */
  private String udate;

  @Column(name = "CLOSEDAT")
  /** 실제 종료 시각 (cmode=2 전환 시점) */
  private String closedat;

  /** 세션 종료 사유 (0: 사용자 종료, 1: 관리자연결, 2: 자동종료) */
  private Integer creason;

  /** 상담 만족도 (0: 불만족, 1: 만족, NULL: 미응답) */
  private Integer satisfy;

  /** 불만족 사유 코드 
   * (0: 답변부정확, 1: 응답느림, 2: 원하는답못찾음, 9: 기타) */
  private Integer sreason;

  /** 불만족 사유 기타 직접입력 */
  private String smemo;
}