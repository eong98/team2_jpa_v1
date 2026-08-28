package dev.jpa.allimio.notification;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 이슈 알림 DTO
 *
 * 알림 목록 및 상세 조회,
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    // 알림 번호
    private Long no;

    // CCTV 이슈 번호
    private Long cino;

    // 알림 수신 회원 번호
    private Long mno;

    // AI 이슈 위치 도면 번호
    private Long aimapno;

    // 이슈 음성 번호
    private Long audiono;

    // 알림 제목
    private String atitle;

    // 알림 내용
    private String content;

    // 중요도
    // LOW / NORMAL / HIGH / EMERGENCY
    private String priority;

    // 알림 처리 상태
    // READY / SENDING / SENT / FAILED / CANCELLED
    private String status;

    // 알림 생성 일시
    private String cdate;

    // 번역 언어
    private String lang;

    // 번역 제목
    private String little;

    // 번역 내용
    private String field;

    // 읽음 여부
    // Y : 읽음 / N : 안 읽음
    private String readyn;


    /**
     * Entity -> DTO 변환
     */
    public static NotificationDTO fromEntity(Notification notification) {

        return NotificationDTO.builder()
                .no(notification.getNo())
                .cino(notification.getCino())
                .mno(notification.getMno())
                .aimapno(notification.getAimapno())
                .audiono(notification.getAudiono())
                .atitle(notification.getAtitle())
                .content(notification.getContent())
                .priority(notification.getPriority())
                .status(notification.getStatus())
                .cdate(notification.getCdate())
                .lang(notification.getLang())
                .little(notification.getLittle())
                .field(notification.getField())
                .readyn(notification.getReadyn())
                .build();
    }
}