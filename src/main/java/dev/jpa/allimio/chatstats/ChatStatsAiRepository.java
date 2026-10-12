package dev.jpa.allimio.chatstats;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** 챗봇 상담 만족도 AI 분석 결과 (CHAT_STATS_AI) */
public interface ChatStatsAiRepository extends JpaRepository<ChatStatsAi, Long> {

  /** 해당 기간(7/30/90일)의 분석 결과 — 기간마다 한 줄이지만, 예전에 쌓인 줄이 있으면 가장 최근 것 */
  Optional<ChatStatsAi> findFirstByDaysOrderByNoDesc(int days);
}
