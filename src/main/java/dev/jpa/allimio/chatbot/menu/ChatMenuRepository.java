package dev.jpa.allimio.chatbot.menu;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ChatMenuRepository extends JpaRepository<ChatMenu, Long> {

  /**
   * 최상위(STEP1) 메뉴 목록 — 챗봇 첫 진입 시 노출할 선택지.
   * 사용중(USEYN='Y')인 것만, 노출순서(VSEQ) 기준 정렬.
   */
  List<ChatMenu> findByPnoIsNullAndUseynOrderByVseqAsc(String useyn);

  /**
   * 특정 부모의 하위 선택지 목록 — 사용자가 선택지 클릭 시 다음 단계 노출.
   */
  List<ChatMenu> findByPnoAndUseynOrderByVseqAsc(Long pno, String useyn);

  /**
   * 특정 노드에 하위 선택지가 있는지 확인 — "이 노드가 리프 노드인지" 판단용
   * (하위 선택지 없으면 다른질문하기/AI상담/종료 버튼 노출).
   */
  boolean existsByPnoAndUseyn(Long pno, String useyn);
  

  /**
   * 관리자용 — 전체 메뉴 검색 + 페이징. 트리 관리 화면에서 사용.
   */
  @Query("""
      SELECT m FROM ChatMenu m
      WHERE (:word IS NULL OR :word = '' OR m.label LIKE CONCAT('%', :word, '%'))
        AND (:step IS NULL OR m.step = :step)
        AND (:useyn IS NULL OR :useyn = '' OR m.useyn = :useyn)
      ORDER BY m.step ASC, m.pno ASC, m.vseq ASC
      """)
  Page<ChatMenu> searchAllAdmin(
      @Param("word") String word,
      @Param("step") Integer step,
      @Param("useyn") String useyn,
      Pageable pageable);

  /**
   * 관리자용 — 트리 전체를 한 번에 (페이징 없이, 관리 화면에서 트리뷰로 렌더링할 때 사용)
   */
  List<ChatMenu> findAllByOrderByStepAscPnoAscVseqAsc();
}
