package dev.jpa.allimio.sendlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 알림 발송 로그 Repository
 *
 * SENDLOG 테이블에
 * 이메일 발송 성공/실패 결과를 저장한다.
 *
 * 현재 관리자 조회 페이지를 만들지 않으므로
 * 별도의 조회 메서드는 추가하지 않는다.
 */
@Repository
public interface SendLogRepository
        extends JpaRepository<SendLog, Long> {

}