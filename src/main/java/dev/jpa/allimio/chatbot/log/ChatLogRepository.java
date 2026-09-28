package dev.jpa.allimio.chatbot.log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

  /** 특정 세션의 전체 대화 로그를 시간순으로 조회 (세션 재진입 시 복원용) */
  List<ChatLog> findBySnoOrderByNoAsc(String sno);

  /**
   * 메뉴 삭제 전 참조 해제 — CHAT_LOG.CNO가 CHAT_MENU를 FK로 참조하므로 NULL로 바꾼다.
   * 대화 내용(CONTENT)은 그대로 남아 상담 기록은 보존된다.
   */
  @Modifying
  @Query("UPDATE ChatLog l SET l.cno = null WHERE l.cno IN :nos")
  int clearCno(@Param("nos") List<Long> nos);
}