package dev.jpa.allimio.chatbot.session;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
  @Query("SELECT s FROM ChatSession s WHERE s.mno = :mno AND s.cmode <> 2 ORDER BY s.udate DESC")
  Optional<ChatSession> findActiveByMno(@Param("mno") Long mno);

  /**
   * 비회원이 지금 진행 중인 세션(MODE != 2)이 있는지 확인.
   */
  @Query("SELECT s FROM ChatSession s WHERE s.gno = :gno AND s.cmode <> 2 ORDER BY s.udate DESC")
  Optional<ChatSession> findActiveByGno(@Param("gno") String gno);

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
}