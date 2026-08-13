package dev.jpa.allimio.aiissuemap;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 이슈 도면 Repository
 */
@Repository
public interface AiIssueMapRepository
    extends JpaRepository<AiIssueMap, Long> {

  /**
   * 원본 매장 도면 번호로 AI 이슈 도면 조회
   */
  List<AiIssueMap> findByShopmapno(long shopmapno);

  /**
   * 회원번호로 AI 이슈 도면 조회
   */
  List<AiIssueMap> findByMno(long mno);
}