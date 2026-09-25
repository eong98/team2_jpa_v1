package dev.jpa.allimio.chatbot.session;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

  /**
   * 회원의 세션 목록 (최신순) — 채팅방 목록 화면.
   */
  List<ChatSession> findByMnoOrderByCmodeAscUdateDesc(Long mno);

  /**
   * 비회원의 세션 목록 (최신순) — 채팅방 목록 화면.
   */
  List<ChatSession> findByGnoOrderByCmodeAscUdateDesc(String gno);

  /**
   * 회원이 지금 진행 중인 세션(MODE != 2)이 있는지 확인.
   * 챗봇을 다시 열었을 때 "이어서 볼지, 새로 시작할지" 판단용.
   */
  Optional<ChatSession> findFirstByMnoAndCmodeNotOrderByUdateDesc(Long mno, Integer cmode);

  /**
   * 비회원이 지금 진행 중인 세션(MODE != 2)이 있는지 확인.
   */
  Optional<ChatSession> findFirstByGnoAndCmodeNotOrderByUdateDesc(String gno, Integer cmode);

  /**
   * 현재 메뉴명(cnoLabel)까지 조인해서 세션 상세 조회.
   */
  @Query("""
      SELECT s, m.label
      FROM ChatSession s
      LEFT JOIN ChatMenu m ON s.cno = m.no
      WHERE s.no = :no
      """)
  List<Object[]> findByIdWithMenu(@Param("no") String no);
  
  /**
   * 채팅방 읽음처리
   * @param no
   * @param readat
   * @return
   */
  @Modifying
  @Transactional
  @Query("UPDATE ChatSession s SET s.readat = :readat WHERE s.no = :no")
  int updateReadAt(@Param("no") String no, @Param("readat") String readat);

  /**
   * 메뉴 삭제 전 참조 해제 — CHAT_SESSION.CNO가 CHAT_MENU를 FK로 참조하므로,
   * 지울 메뉴에 머물러 있던 세션의 CNO를 NULL로 바꿔야 삭제가 가능하다.
   */
  @Modifying
  @Query("UPDATE ChatSession s SET s.cno = null WHERE s.cno IN :nos")
  int clearCno(@Param("nos") List<Long> nos);
}