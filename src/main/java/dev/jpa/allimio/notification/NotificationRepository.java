package dev.jpa.allimio.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 알림 Repository
 *
 * Oracle NOTIFICATION 테이블의 알림 데이터를
 * 조회 및 저장하기 위한 JPA Repository
 */
@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * 회원별 알림 목록 조회
     *
     * 로그인한 회원의 알림을
     * 최신 알림 순으로 조회한다.
     *
     * SQL 개념:
     * SELECT *
     * FROM NOTIFICATION
     * WHERE MNO = ?
     * ORDER BY CDATE DESC;
     */
    List<Notification> findByMnoOrderByCdateDesc(Long mno);


    /**
     * 회원별 안 읽은 알림 개수 조회
     *
     * READYN = 'N'인 알림 개수를 조회한다.
     *
     * SQL 개념:
     * SELECT COUNT(*)
     * FROM NOTIFICATION
     * WHERE MNO = ?
     * AND READYN = 'N';
     */
    long countByMnoAndReadyn(Long mno, String readyn);


    /**
     * 회원의 특정 알림 상세 조회
     *
     * 알림 번호(NO)와 회원 번호(MNO)를 같이 확인한다.
     * 다른 회원의 알림을 조회하지 못하도록 하기 위함이다.
     *
     * SQL 개념:
     * SELECT *
     * FROM NOTIFICATION
     * WHERE NO = ?
     * AND MNO = ?;
     */
    Optional<Notification> findByNoAndMno(Long no, Long mno);


    /**
     * AI 이슈맵 번호(ASMNO)로 생성된 이슈 이미지 파일명을 조회
     *
     * NOTIFICATION.ASMNO
     *      → AIISSUEMAP.NO
     *      → AIISSUEMAP.FSAVED
     *
     * @param asmno AIISSUEMAP 번호
     * @return 생성된 이슈 이미지 파일명
     */
    @Query(
        value = """
            SELECT A.FSAVED
            FROM AIISSUEMAP A
            WHERE A.NO = :asmno
            """,
        nativeQuery = true
    )
    Optional<String> findFsavedByAsmno(
        @Param("asmno") Long asmno
    );
    
    
    
    /**
     * 관리자 전체 알림 발송 내역 조회
     *
     * NOTIFICATION : 알림 정보
     * MEMBER       : 수신자 정보
     * SENDLOG      : 이메일 / 문자 발송 결과
     *
     * 이메일과 문자는 가장 최근 SENDLOG를 기준으로 조회한다.
     */
    @Query(
        value = """
            SELECT
                N.NO,
                N.MNO,
                M.MNAME,
                M.EMAIL,
                M.PHONE,
                N.CINO,
                N.ASMNO,
                N.ATITLE,
                N.CONTENT,
                N.PRIORITY,
                N.STATUS,
                N.READYN,
                N.CDATE,

                (
                    SELECT CASE
                             WHEN S.STATUS = 1 THEN 'SENT'
                             ELSE 'FAILED'
                           END
                    FROM SENDLOG S
                    WHERE S.NNO = N.NO
                      AND S.CHANNEL = 'EMAIL'
                      AND S.NO = (
                          SELECT MAX(S2.NO)
                          FROM SENDLOG S2
                          WHERE S2.NNO = N.NO
                            AND S2.CHANNEL = 'EMAIL'
                      )
                ) AS EMAIL_STATUS,

                (
                    SELECT CASE
                             WHEN S.STATUS = 1 THEN 'SENT'
                             ELSE 'FAILED'
                           END
                    FROM SENDLOG S
                    WHERE S.NNO = N.NO
                      AND S.CHANNEL = 'SMS'
                      AND S.NO = (
                          SELECT MAX(S2.NO)
                          FROM SENDLOG S2
                          WHERE S2.NNO = N.NO
                            AND S2.CHANNEL = 'SMS'
                      )
                ) AS SMS_STATUS

            FROM NOTIFICATION N
            LEFT JOIN MEMBER M
                ON N.MNO = M.NO

            ORDER BY N.NO DESC
            """,
        nativeQuery = true
    )
    List<Object[]> findAdminNotifications();
}

