package dev.jpa.alllimio.surveyanswer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SURVEYANSWER 테이블 접근 Repository.
 */
@Repository
public interface SurveyAnswerRepository
        extends JpaRepository<SurveyAnswer, Long> {

    /** 특정 설문응답에 포함된 실제 답변을 답변번호순으로 조회한다. */
    List<SurveyAnswer> findByResponseNoOrderByNoAsc(Long responseNo);
}
