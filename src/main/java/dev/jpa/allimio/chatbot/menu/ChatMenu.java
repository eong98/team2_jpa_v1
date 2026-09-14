package dev.jpa.allimio.chatbot.menu;

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
 * 챗봇옵션 메뉴 Entity
 *
 * CHAT_MENU 테이블과 연결됩니다. 옵션형 선택지 트리(최대 3단계)를 표현하며,
 * PNO(자기참조 FK)로 부모-자식 관계를 맺습니다. 최상위(STEP=1)는 PNO가 NULL입니다.
 */

@Entity
@Table(name = "CHAT_MENU")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMenu {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_menu_seq_use")
  @SequenceGenerator(name = "chat_menu_seq_use", sequenceName = "CHAT_MENU_SEQ", allocationSize = 1)
  /** 메뉴 고유번호 (PK) */
  private Long no;

  /** 상위 메뉴 번호 (FK, 최상위면 NULL) */
  private Long pno;

  /** 옵션 단계 (1~3) */
  private Integer step;

  /** 선택지에 표시될 텍스트 */
  private String label;

  /** 이 선택지 클릭 시 노출할 답변 (최상위 STEP1은 보통 NULL) */
  private String answer;

  /** RAG검색여부 - 이 노드 도달 시 RAG 검색도 같이 수행할지 (Y/N) */
  @Builder.Default
  private String userag = "N";

  /** 노출순서 - 같은 부모 안에서의 노출 순서 */
  @Builder.Default
  private Integer vseq = 0;

  /** 사용여부 (N이면 목록에서 숨김) */
  @Builder.Default
  private String useyn = "Y";

  /** 등록일시 */
  private String cdate;
}