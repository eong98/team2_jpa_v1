package dev.jpa.allimio.shopsurvey;

import dev.jpa.allimio.shopsurveyoption.ShopSurveyOption;
import dev.jpa.allimio.shopsurveyquestion.ShopSurveyQuestion;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 폼 DTO
 *
 * 이 형식 하나를 아래 세 곳에서 공통으로 사용합니다.
 *  1. 프론트 설문 생성/수정 폼 state
 *  2. 임시저장(SHOP_SURVEY.DRAFT) JSON
 *  3. AI 자동작성(FastAPI) 출력 형식
 *
 * 그래서 AI가 이 JSON을 돌려주면 프론트는 폼 state에 그대로 넣기만 하면 됩니다.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ShopSurveyDTO {

  /** 설문번호 (신규 작성 시 null) */
  private Long no;

  /** 매장번호 */
  private Long sno;

  /** 매장명 (고객 화면 표시용, 조회 전용) */
  private String shopTitle;

  /** 설문제목 */
  private String title;

  /** 설문설명 */
  private String description;

  /** 상태 DRAFT/OPEN/CLOSED/DELETE (조회 전용) */
  private String status;

  /** QR코드 접속용 UUID (조회 전용) */
  private String qrid;

  /** 수정일 (조회 전용) */
  private String udate;

  /** 등록일 (조회 전용) */
  private String cdate;

  /** 응답 수 (조회 전용, 1건 이상이면 수정 잠금) */
  private Long responseCount;
  
  /** AI생성여부 0:직접작성 / 1:AI관여. 폼에서 AI를 한 번이라도 쓰면 1로 보냄 */
  private Integer aiyn;

  /** 문항 목록 */
  @Builder.Default
  private List<Question> questions = new ArrayList<>();

  /**
   * 문항
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class Question {
    /** 문항번호 (신규 작성 시 null) */
    private Long no;

    /** 문항제목 */
    private String title;

    /** 답변타입 SHORT/LONG/SINGLE/MULTI/SCALE */
    private String atype;

    /** 필수여부 0:필수아님/1:필수 */
    @Builder.Default
    private Integer requiredyn = 0;

    /** 파일첨부여부 0:첨부불가/1:첨부가능 */
    @Builder.Default
    private Integer fileyn = 0;

    /** 정렬순서 */
    private Integer sort;

    /** 객관식 보기 목록 (SINGLE/MULTI일 때만) */
    @Builder.Default
    private List<Option> options = new ArrayList<>();

    public static Question fromEntity(ShopSurveyQuestion entity) {
      return Question.builder()
          .no(entity.getNo())
          .title(entity.getTitle())
          .atype(entity.getAtype())
          .requiredyn(entity.getRequiredyn())
          .fileyn(entity.getFileyn())
          .sort(entity.getSort())
          .options(new ArrayList<>())
          .build();
    }
  }

  /**
   * 객관식 보기
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class Option {
    /** 보기번호 (신규 작성 시 null) */
    private Long no;

    /** 보기내용 */
    private String label;

    /** 정렬순서 */
    private Integer sort;

    public static Option fromEntity(ShopSurveyOption entity) {
      return Option.builder()
          .no(entity.getNo())
          .label(entity.getLabel())
          .sort(entity.getSort())
          .build();
    }
  }

  /**
   * 상태 변경 요청 (OPEN <-> CLOSED)
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class StatusRequest {
    private String status;
  }
}
