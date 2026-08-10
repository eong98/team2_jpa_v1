package dev.jpa.alllimio.survey;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SURVEY 테이블 접근 Repository.
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /** 설문 목록을 최신 설문번호순으로 조회한다. */
    List<Survey> findAllByOrderByNoDesc();

    /**
     * 설문 상세조회 시 문항 목록을 함께 조회한다.
     * questions 한 개 컬렉션만 fetch하므로 MultipleBagFetchException 위험이 없다.
     */
    @EntityGraph(attributePaths = "questions")
    Optional<Survey> findWithQuestionsByNo(Long no);
}
