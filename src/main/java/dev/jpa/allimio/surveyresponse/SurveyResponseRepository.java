package dev.jpa.allimio.surveyresponse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SURVEYRESPONSE 테이블 접근 Repository.
 */
@Repository
public interface SurveyResponseRepository
        extends JpaRepository<SurveyResponse, Long> {

    /** 동일한 점주가 동일한 설문에 이미 응답했는지 확인한다. */
    boolean existsBySurveyNoAndMemberNo(Long surveyNo, Long memberNo);

    /** 특정 점주의 특정 설문 응답을 조회한다. */
    @EntityGraph(attributePaths = {"answers", "answers.question"})
    Optional<SurveyResponse> findBySurveyNoAndMemberNo(
            Long surveyNo,
            Long memberNo);

    /** 특정 설문의 전체 점주 응답을 최신순으로 조회한다. */
    List<SurveyResponse> findBySurveyNoOrderByNoDesc(Long surveyNo);

    /** 응답 상세와 실제 답변·문항을 함께 조회한다. */
    @EntityGraph(attributePaths = {"answers", "answers.question"})
    Optional<SurveyResponse> findWithAnswersByNo(Long no);
}
