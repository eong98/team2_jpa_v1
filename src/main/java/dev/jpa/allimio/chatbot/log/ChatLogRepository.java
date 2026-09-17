package dev.jpa.allimio.chatbot.log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

  /** 특정 세션의 전체 대화 로그를 시간순으로 조회 (세션 재진입 시 복원용) */
  List<ChatLog> findBySnoOrderByNoAsc(String sno);
}