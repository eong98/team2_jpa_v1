package dev.jpa.allimio.shopsurvey;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 AI 자동작성 요청 DTO
 *
 * POST /shop_survey/ai/generate
 *
 * mode
 * - create : 요청 문장 + 점주가 고른 이전 설문(refSvnos)으로 새 설문 생성
 * - revise : 현재 폼(currentForm) + 수정 요청 문장으로 고치기
 * - trend  : 현재 폼에 업종 뉴스 트렌드 문항 1~2개 추가
 *
 * 응답은 FastAPI 결과를 그대로 돌려줍니다.
 * { industry, form: ShopSurveyDTO 형식, notes[], articles[], addedIndexes[], message }
 */
public class ShopSurveyAiDTO {

  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class Request {
    /** 매장번호 (점주 소유 확인) */
    private Long sno;

    /** create / revise / trend */
    private String mode;

    /** 점주 요청 문장 (create, revise) */
    private String request;

    /** 첫 생성 때 AI가 뽑은 업종 (revise, trend에서 재사용) */
    private String industry;

    /** 참고할 이전 설문 번호 (create, 점주가 체크한 것) */
    private List<Long> refSvnos = new ArrayList<>();

    /** 현재 폼 (revise, trend) */
    private ShopSurveyDTO currentForm;
  }
}