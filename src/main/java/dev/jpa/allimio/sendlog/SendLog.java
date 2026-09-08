package dev.jpa.allimio.sendlog;

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
 * 알림 발송 로그 Entity
 *
 * NOTIFICATION 알림의 실제 발송 결과를 기록한다.
 *
 * 발송 채널:
 * - EMAIL : 이메일 발송
 * - SMS   : 문자 발송
 *
 * 발송 상태:
 * - 1 : 발송 성공
 * - 2 : 발송 실패
 *
 * 실패한 경우 MESSAGE에 실패 사유를 저장한다.
 */
@Entity
@Table(name = "SENDLOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
    name = "sendlog_seq_generator",
    sequenceName = "SEQ_SENDLOG_NO",
    allocationSize = 1
)
public class SendLog {


    /**
     * 발송 로그 번호
     */
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "sendlog_seq_generator"
    )
    @Column(name = "NO")
    private Long no;


    /**
     * 알림 번호
     *
     * FK -> NOTIFICATION.NO
     */
    @Column(
        name = "NNO",
        nullable = false
    )
    private Long nno;


    /**
     * 발송 채널
     *
     * EMAIL : 이메일
     * SMS   : 문자
     */
    @Column(
        name = "CHANNEL",
        nullable = false,
        length = 10
    )
    private String channel;


    /**
     * 발송 상태
     *
     * 1 : 발송 성공
     * 2 : 발송 실패
     */
    @Column(
        name = "STATUS",
        nullable = false
    )
    private Integer status;


    /**
     * 발송 결과 메시지
     *
     * 성공:
     * - 이메일 발송 완료
     * - 문자 발송 완료
     *
     * 실패:
     * - 이메일 주소 없음
     * - SMTP 오류
     * - 문자 API 호출 실패
     * 등
     */
    @Column(
        name = "MESSAGE",
        length = 1000
    )
    private String message;


    /**
     * 발송 로그 등록일시
     *
     * DB:
     * VARCHAR2(30)
     */
    @Column(
        name = "CDATE",
        nullable = false,
        length = 30
    )
    private String cdate;
}