package dev.jpa.alllimio.surveyquestion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SURVEYQUESTION 테이블 접근 Repository.
 */
@Repository
public interface SurveyQuestionRepository
        extends JpaRepository<SurveyQuestion, Long> {

    /** 특정 설문의 문항을 출력 순서대로 조회한다. */
    List<SurveyQuestion> findBySurveyNoOrderBySeqNoAscNoAsc(Long surveyNo);
}
