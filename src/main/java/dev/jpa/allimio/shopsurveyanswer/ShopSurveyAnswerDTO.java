package dev.jpa.allimio.shopsurveyanswer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dev.jpa.allimio.attach.AttachDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 응답 관련 DTO 모음
 *
 * - Submit / SubmitAnswer : 고객 제출 요청
 * - Response / Answer     : 점주 응답 목록 조회 결과
 * - Stats / QuestionStat / OptionStat : 문항별 집계 결과
 */
public class ShopSurveyAnswerDTO {

  // ==========================================
  // [고객 제출 요청]
  // ==========================================

  /**
   * 고객 제출 요청
   * multipart의 "data" 파트에 JSON 문자열로 담겨 옵니다.
   * 사진은 "file_{문항번호}" 이름의 파트로 따로 옵니다.
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class Submit {
    private List<SubmitAnswer> answers = new ArrayList<>();
  }

  /**
   * 고객 제출 - 문항별 답
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class SubmitAnswer {
    /** 문항번호 */
    private Long sqno;

    /** 단답/장문 답변 */
    private String content;

    /** 점수 (0 ~ 10, 소수 첫째 자리까지) */
    private BigDecimal scale;

    /** 선택한 보기번호 목록 (SINGLE은 1개, MULTI는 1개 이상) */
    private List<Long> sonos = new ArrayList<>();
  }

  // ==========================================
  // [점주 응답 목록 조회]
  // ==========================================

  /**
   * 응답 1건 (고객 1회 제출)
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class Response {
    /** 응답번호 */
    private Long no;

    /** 응답일시 */
    private String cdate;

    /** 문항별 답 */
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
  }

  /**
   * 응답 안의 문항별 답
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class Answer {
    /** 답변번호 */
    private Long no;

    /** 문항번호 */
    private Long sqno;

    /** 문항제목 */
    private String questionTitle;

    /** 답변타입 */
    private String atype;

    /** 단답/장문 답변 */
    private String content;

    /** 점수 */
    private BigDecimal scale;

    /** 선택한 보기 내용 목록 */
    @Builder.Default
    private List<String> options = new ArrayList<>();

    /** 첨부 사진 목록 (ATTACH) */
    @Builder.Default
    private List<AttachDTO> files = new ArrayList<>();
  }

  // ==========================================
  // [점주 문항별 집계]
  // ==========================================

  /**
   * 설문 집계 결과
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class Stats {
    /** 설문번호 */
    private Long svno;

    /** 전체 응답 수 */
    private Long totalResponses;

    /** 문항별 집계 */
    @Builder.Default
    private List<QuestionStat> questions = new ArrayList<>();
  }

  /**
   * 문항별 집계
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class QuestionStat {
    /** 문항번호 */
    private Long sqno;

    /** 문항제목 */
    private String title;

    /** 답변타입 */
    private String atype;

    /** 이 문항에 답한 수 */
    private Long answerCount;

    /** 평균 점수 (SCALE일 때만, 소수 첫째 자리 반올림) */
    private Double scaleAvg;

    /** 보기별 선택 수 (SINGLE/MULTI일 때만) */
    @Builder.Default
    private List<OptionStat> options = new ArrayList<>();
  }

  /**
   * 보기별 선택 수
   */
  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @Builder
  public static class OptionStat {
    /** 보기번호 */
    private Long sono;

    /** 보기내용 */
    private String label;

    /** 선택 수 */
    private Long count;
  }
}
