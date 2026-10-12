package dev.jpa.allimio.qa.mail;

import org.springframework.data.jpa.repository.JpaRepository;

/** 문의사항 메일 발송 이력 (QA_SEND_LOG) — 관리자 목록 조회는 QaSendLogQuery */
public interface QaSendLogRepository extends JpaRepository<QaSendLog, Long> {

  /** 이 줄보다 나중에 같은 글·같은 종류로 발송에 성공한 기록이 있는지 (다시 보내기 중복 방지) */
  boolean existsByQnoAndStypeAndResultAndNoGreaterThan(Long qno, int stype, String result, Long no);
}
