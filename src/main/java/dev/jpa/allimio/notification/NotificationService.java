package dev.jpa.allimio.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 회원 이슈 알림 Service
 *
 * Controller와 Repository 사이에서
 * 알림 관련 기능을 처리한다.
 *
 * 기능
 * 1. 회원별 알림 목록 조회
 * 2. 알림 상세 조회
 * 3. 안 읽은 알림 개수 조회
 * 4. 관리자 전체 알림 발송 내역 조회
 * 5. 알림 읽음 처리
 *
 * 알림 생성 및 이메일/SMS 발송은
 * FastAPI(Python)에서 처리한다.
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;


    /**
     * 생성자 주입
     */
    public NotificationService(
            NotificationRepository notificationRepository) {

        this.notificationRepository = notificationRepository;
    }


    /**
     * 회원별 알림 목록 조회
     *
     * 로그인 회원의 MNO를 기준으로
     * 최신 알림부터 조회한다.
     */
    public List<NotificationDTO> getNotifications(Long mno) {

        return notificationRepository
                .findByMnoOrderByCdateDesc(mno)
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }


    /**
     * 알림 상세 조회
     *
     * 알림 번호(NO)와 회원 번호(MNO)를 같이 확인한다.
     * 다른 회원에게 발송된 알림은 조회할 수 없다.
     */
    public NotificationDTO getNotification(Long no, Long mno) {

        Notification notification = notificationRepository
                .findByNoAndMno(no, mno)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 알림을 찾을 수 없습니다."
                        )
                );

        return NotificationDTO.fromEntity(notification);
    }


    /**
     * 회원별 안 읽은 알림 개수 조회
     *
     * READYN = 'N'인 알림 개수를 반환한다.
     */
    public long getUnreadCount(Long mno) {

        return notificationRepository
                .countByMnoAndReadyn(mno, "N");
    }


    /**
     * 관리자 전체 알림 발송 내역 조회
     *
     * NOTIFICATION + MEMBER + SENDLOG 조회 결과를
     * NotificationAdminDTO로 변환한다.
     *
     * SENDLOG 자체 저장은 FastAPI(Python)에서 처리한다.
     */
    public List<NotificationAdminDTO> getAdminNotifications() {

        return notificationRepository
                .findAdminNotifications()
                .stream()
                .map(row -> new NotificationAdminDTO(

                        // 알림 번호
                        row[0] == null
                                ? null
                                : Long.valueOf(row[0].toString()),

                        // 회원 번호
                        row[1] == null
                                ? null
                                : Long.valueOf(row[1].toString()),

                        // 회원 이름
                        row[2] == null
                                ? null
                                : row[2].toString(),

                        // 이메일
                        row[3] == null
                                ? null
                                : row[3].toString(),

                        // 전화번호
                        row[4] == null
                                ? null
                                : row[4].toString(),

                        // CCTV 이슈 번호
                        row[5] == null
                                ? null
                                : Long.valueOf(row[5].toString()),

                        // AI 이슈맵 번호
                        row[6] == null
                                ? null
                                : Long.valueOf(row[6].toString()),

                        // 제목
                        row[7] == null
                                ? null
                                : row[7].toString(),

                        // 내용
                        row[8] == null
                                ? null
                                : row[8].toString(),

                        // 중요도
                        row[9] == null
                                ? null
                                : row[9].toString(),

                        // 전체 상태
                        row[10] == null
                                ? null
                                : row[10].toString(),

                        // 읽음 여부
                        row[11] == null
                                ? null
                                : row[11].toString(),

                        // 등록일
                        row[12] == null
                                ? null
                                : row[12].toString(),

                        // 이메일 발송 상태
                        row[13] == null
                                ? null
                                : row[13].toString(),

                        // 문자 발송 상태
                        row[14] == null
                                ? null
                                : row[14].toString()
                ))
                .toList();
    }


    /**
     * 알림 읽음 처리
     *
     * 알림 상세를 확인했을 때
     * READYN 값을 N -> Y로 변경한다.
     */
    @Transactional
    public void readNotification(Long no, Long mno) {

        Notification notification = notificationRepository
                .findByNoAndMno(no, mno)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 알림을 찾을 수 없습니다."
                        )
                );

        // 이미 읽은 알림이면 다시 수정하지 않는다.
        if ("Y".equals(notification.getReadyn())) {
            return;
        }

        notification.setReadyn("Y");
    }
}