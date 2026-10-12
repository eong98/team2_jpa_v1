package dev.jpa.allimio.qa.faq;

import java.util.List;
import java.util.Map;

/** FAQ AI 추천 응답 — React components/ts/QaFaqSuggest.ts와 1:1 */
public class QaFaqSuggestDTO {

  /** 근거가 된 문의 (관리자 화면에서 눌러 볼 수 있게 번호와 제목) */
  public record Source(long no, String title) {
  }

  /**
   * FAQ 후보 한 건 — 자동 등록하지 않고, 관리자가 확인·수정한 뒤 등록
   *
   * @param type  문의 유형 (QA.TYPE) — AI가 내용으로 판단한 값, 판단하지 못했거나 기타면 근거 문의들의 유형 중 가장 많은 것
   * @param count 비슷한 문의 수
   */
  public record Candidate(String title, String content, String answer, int type, int count, List<Source> sources,
      String reason) {
  }

  /**
   * 추천 결과
   *
   * @param analyzed 분석한 문의 수 (관리자 답변이 달린 문의)
   * @param groups   비슷한 문의 묶음 수 (2건 이상)
   * @param skipped  이미 FAQ에 있어서 제외한 묶음 수
   */
  public record Result(List<Candidate> candidates, int analyzed, int groups, int skipped, String from, String to) {
  }

  /** AI 서버에 보낼 자료 (DB 조회 결과) — AI 호출은 DB 연결을 놓은 뒤에 */
  public record Request(Map<String, Object> payload, Map<Long, String> titles, Map<Long, Integer> types, int analyzed,
      String from, String to) {
  }
}
