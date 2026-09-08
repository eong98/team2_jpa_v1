package dev.jpa.allimio.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;


/**
 * 회원 이슈 알림 Controller
 *
 * React에서 들어오는 알림 관련 요청을 처리한다.
 *
 * 기능
 * 1. 회원별 알림 목록 조회
 * 2. 알림 상세 조회
 * 3. 안 읽은 알림 개수 조회
 * 4. 알림 읽음 처리
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 생성자 주입
     */
    public NotificationController(NotificationService notificationService ) {
        this.notificationService = notificationService;
    }

    /**
     * 관리자 전체 알림 발송 내역 조회
     *
     * GET /api/notifications/admin
     */
    @GetMapping("/admin")
    public ResponseEntity<List<NotificationAdminDTO>> getAdminNotifications() {

        List<NotificationAdminDTO> notifications =
                notificationService.getAdminNotifications();

        return ResponseEntity.ok(notifications);
    }

    /**
     * 회원별 알림 목록 조회
     *
     * GET /api/notifications/member/{mno}
     *
     * 예)
     * GET /api/notifications/member/10
     */
    @GetMapping("/member/{mno}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @PathVariable("mno") Long mno) {

        List<NotificationDTO> notifications =
                notificationService.getNotifications(mno);

        return ResponseEntity.ok(notifications);
    }


    /**
     * 알림 상세 조회
     *
     * 알림 번호와 회원 번호를 같이 확인하여
     * 해당 회원에게 발송된 알림만 조회한다.
     *
     * GET /api/notifications/{no}/member/{mno}
     */
    @GetMapping("/{no}/member/{mno}")
    public ResponseEntity<NotificationDTO> getNotification(
            @PathVariable("no") Long no,
            @PathVariable("mno") Long mno) {

        NotificationDTO notification =
                notificationService.getNotification(no, mno);

        return ResponseEntity.ok(notification);
    }


    /**
     * 회원별 안 읽은 알림 개수 조회
     *
     * GET /api/notifications/member/{mno}/unread-count
     */
    @GetMapping("/member/{mno}/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable("mno") Long mno) {

        long count = notificationService.getUnreadCount(mno);

        return ResponseEntity.ok(count);
    }


    /**
     * 알림 읽음 처리
     *
     * READYN 값을 N -> Y로 변경한다.
     *
     * PATCH /api/notifications/{no}/member/{mno}/read
     */
    @PatchMapping("/{no}/member/{mno}/read")
    public ResponseEntity<Void> readNotification(
            @PathVariable("no") Long no,
            @PathVariable("mno") Long mno) {

        notificationService.readNotification(no, mno);

        return ResponseEntity.noContent().build();
    }
  
    
    /**
     * 알림 이메일 발송
     *
     * POST /api/notifications/{no}/email
     */
    @PostMapping("/{no}/email")
    public ResponseEntity<String> sendNotificationMail(
            @PathVariable("no") Long no) {

        notificationService.sendNotificationMail(no);

        return ResponseEntity.ok("메일 발송 완료");
    }
    
    
}