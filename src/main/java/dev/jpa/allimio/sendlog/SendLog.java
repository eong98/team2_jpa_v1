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
 * 이메일 또는 웹 알림 발송 결과를 기록한다.
 *
 * 현재는 이메일 발송 성공/실패 기록에 주로 사용한다.
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
    sequenceName = "SENDLOG_SEQ",
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
     * 발송 대상 알림 번호
     *
     * NOTIFICATION.NO 값
     */
    @Column(name = "NNO", nullable = false)
    private Long nno;


    /**
     * 발송 채널
     *
     * EMAIL : 이메일 발송
     * WEB   : 웹 알림
     */
    @Column(name = "CHANNEL", nullable = false, length = 10)
    private String channel;


    /**
     * 발송 상태
     *
     * 1 : 발송 성공
     * 0 : 발송 실패
     */
    @Column(name = "STATUS", nullable = false)
    private Integer status;


    /**
     * 발송 결과 메시지
     *
     * 성공 시 성공 메시지,
     * 실패 시 오류 내용을 저장한다.
     */
    @Column(name = "MESSAGE", length = 1000)
    private String message;


    /**
     * 발송 로그 등록일
     *
     * 현재 SQL에서 VARCHAR2(30)으로 정의되어 있어
     * String으로 매핑한다.
     */
    @Column(name = "CDATE", nullable = false, length = 30)
    private String cdate;
}