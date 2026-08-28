package dev.jpa.allimio.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 이슈 알림 Entity
 *
 * CCTV 이슈 발생 시 알림 수신 대상 회원별로 한 건씩 저장한다.
 *
 * 점주  : 본인이 소유한 매장의 이슈 알림 수신
 * 직원  : 본인이 소속된 매장의 이슈 알림 수신
 */
@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
    name = "notification_seq_generator",
    sequenceName = "SEQ_NOTIFICATION_NO",
    allocationSize = 1
)
public class Notification {

    /**
     * 알림 번호
     */
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "notification_seq_generator"
    )
    @Column(name = "NO")
    private Long no;


    /**
     * CCTV 이슈 번호
     */
    @Column(name = "CINO", nullable = false)
    private Long cino;



    /**
     * 알림을 받는 회원 번호
     * MEMBER.NO
     */
    @Column(name = "MNO", nullable = false)
    private Long mno;


    /**
     * AI 알림  도면 번호
     * 해당 기능이 없는 경우 NULL
     */
    @Column(name = "AIMAPNO")
    private Long aimapno;


    /**
     * 알림 이슈 음성 번호
     * 음성 기능을 사용하지 않는 경우 NULL
     */
    @Column(name = "AUDIONO")
    private Long audiono;


    /**
     * 알림 제목
     */
    @Column(name = "ATITLE", nullable = false, length = 200)
    private String atitle;


    /**
     * 알림 내용
     */
    @Column(name = "CONTENT", length = 2000)
    private String content;


    /**
     * 알림 중요도
     * LOW / NORMAL / HIGH / EMERGENCY
     */
    @Builder.Default
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private String priority = "NORMAL";


    /**
     * 알림 처리 상태
     * READY / SENDING / SENT / FAILED / CANCELLED
     */
    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "READY";


    /**
     * 알림 생성 일시
     */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;


    /**
     * 번역 언어
     */
    @Column(name = "LANG", length = 10)
    private String lang;


    /**
     * 번역된 알림 제목
     */
    @Column(name = "LITTLE", length = 500)
    private String little;


    /**
     * 번역된 알림 내용
     */
    @Column(name = "FIELD", length = 500)
    private String field;


    /**
     * 웹 알림 읽음 여부
     * N : 안 읽음
     * Y : 읽음
     */
    @Builder.Default
    @Column(name = "READYN", nullable = false, length = 1)
    private String readyn = "N";
}