package dev.jpa.allimio.qa.faq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.qa.Qa;
import dev.jpa.allimio.qa.QaRepository;
import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   FAQ AI 추천 — AI 서버에 보낼 자료 모으기

   자주 들어오는 문의를 FAQ로 올리려면 관리자가 문의를 하나하나 읽어야 했습니다.
   AI가 비슷한 문의를 묶어 대표 질문·답변 초안을 만들어 주면, 관리자는 확인하고 고쳐서 등록만 하면 됩니다.

   보내는 것
   - 관리자 답변이 달린 1:1 문의 (기간 내, 최근 순 MAX_QUESTIONS건) — 답변이 없는 문의는 답의 근거가 없어 제외
   - 지금 등록된 FAQ — 같은 내용을 또 추천하지 않도록

   이메일·작성자 번호는 보내지 않습니다. 제목·내용·답변만 보냅니다.
--------------------------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QaFaqSuggestService {

  /** 조회기간(일) — 그 외 값은 90으로 보정 */
  private static final List<Integer> ALLOWED_DAYS = List.of(30, 90, 180, 365);
  /** AI에 보낼 문의 최대 건수 (임베딩·LLM 시간과 요청 크기 제한) */
  private static final int MAX_QUESTIONS = 200;
  /** 비교용으로 보낼 기존 FAQ 최대 건수 */
  private static final int MAX_EXISTING = 200;
  /** 문의 하나에서 보낼 글자 수 */
  private static final int MAX_TEXT_LENGTH = 1000;

  private final QaRepository qaRepository;

  public int normalizeDays(int days) {
    return ALLOWED_DAYS.contains(days) ? days : 90;
  }

  /**
   * @throws IllegalStateException 관리자에게 그대로 보여줄 안내 문구 (분석할 문의 부족)
   */
  public QaFaqSuggestDTO.Request buildRequest(int days) {
    days = normalizeDays(days);
    LocalDate today = LocalDate.now();
    String from = today.minusDays(days - 1L).toString();
    String to = today.toString();

    List<Qa> answered = qaRepository.findAnsweredQuestionsSince(from, PageRequest.of(0, MAX_QUESTIONS));
    if (answered.size() < 2) {
      throw new IllegalStateException("기간 내 관리자 답변이 달린 문의가 2건 이상 있어야 추천할 수 있습니다. (현재 " + answered.size() + "건)");
    }

    List<Map<String, Object>> questions = new ArrayList<>();
    Map<Long, String> titles = new HashMap<>();
    Map<Long, Integer> types = new HashMap<>(); // 후보의 문의 유형을 정하는 데 사용 (AI에는 보내지 않음)
    for (Qa q : answered) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("no", q.getNo());
      row.put("title", cut(q.getTitle()));
      row.put("content", cut(q.getContent()));
      row.put("answer", cut(q.getAnswer()));
      questions.add(row);
      titles.put(q.getNo(), q.getTitle());
      types.put(q.getNo(), q.getType());
    }

    List<Map<String, Object>> existing = new ArrayList<>();
    for (Qa f : qaRepository.findFaqsForCompare(PageRequest.of(0, MAX_EXISTING))) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("no", f.getNo());
      row.put("title", cut(f.getTitle()));
      row.put("content", cut(f.getContent()));
      existing.add(row);
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("period", from + " ~ " + to);
    payload.put("questions", questions);
    payload.put("existing", existing);
    return new QaFaqSuggestDTO.Request(payload, titles, types, questions.size(), from, to);
  }

  private String cut(String s) {
    if (s == null) {
      return "";
    }
    String text = s.trim();
    return text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH) : text;
  }
}
