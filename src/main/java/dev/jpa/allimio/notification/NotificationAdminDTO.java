package dev.jpa.allimio.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 관리자 알림 발송 내역 조회 DTO
 *
 * NOTIFICATION
 * + MEMBER
 * + SENDLOG
 * 정보를 관리자 화면에 전달한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAdminDTO {

    /** 알림 번호 */
    private Long no;

    /** 수신 회원 번호 */
    private Long mno;

    /** 수신 회원명 */
    private String memberName;

    /** 이메일 */
    private String email;

    /** 전화번호 */
    private String phone;

    /** CCTV 이슈 번호 */
    private Long cino;

    /** 이슈맵 번호 */
    private Long aimapno;

    /** 알림 제목 */
    private String title;

    /** 알림 내용 */
    private String content;

    /** 중요도 */
    private String priority;

    /** 알림 전체 상태 */
    private String status;

    /** 회원 확인 여부 */
    private String readyn;

    /** 알림 생성일 */
    private String cdate;

    /** 이메일 발송 상태 */
    private String emailStatus;

    /** 문자 발송 상태 */
    private String smsStatus;
}