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

  /** 노출순서 - 같은 부모 안에서의 노출 순서 */
  @Builder.Default
  private Integer vseq = 0;

  /** 사용여부 (N이면 목록에서 숨김) */
  @Builder.Default
  private String useyn = "Y";

  /** 등록일시 */
  private String cdate;

  /** AI관리여부 - AI(옵션형메뉴 자동생성)가 만든 노드면 Y, 관리자가 직접 등록한 노드면 N.
   *  [AI 옵션생성]을 다시 실행하면 AIYN='Y'인 노드만 삭제 후 재생성됨. */
  @Builder.Default
  private String aiyn = "N";

  /**
   * 사용여부(USEYN)만 토글할 때 호출 (Notice.changeTopFix()와 동일한 패턴).
   * 관리자가 옵션메뉴관리 화면에서 공개/비공개 버튼을 누르면 이 메서드로 처리한다.
   */
  public void changeUseyn(String useyn) {
    this.useyn = useyn;
  }
}