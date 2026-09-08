package dev.jpa.allimio.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.tool.MailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dev.jpa.allimio.sendlog.SendLog;
import dev.jpa.allimio.sendlog.SendLogRepository;

/**
 * 회원 이슈 알림 Service
 *
 * Controller와 Repository 사이에서
 * 알림 관련 실제 기능을 처리한다.
 *
 * 기능
 * 1. 회원별 알림 목록 조회
 * 2. 알림 상세 조회
 * 3. 안 읽은 알림 개수 조회
 * 4. 알림 읽음 처리
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final MailService mailService;
    private final SendLogRepository sendLogRepository;

    /**
     * 생성자 주입
     */
    public NotificationService(
            NotificationRepository notificationRepository,
            MemberRepository memberRepository,
            MailService mailService,
            SendLogRepository sendLogRepository) {
        this.notificationRepository = notificationRepository;
        this.memberRepository = memberRepository;
        this.mailService = mailService;
        this.sendLogRepository = sendLogRepository;
        
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
    
    
    /**
     * 알림 이메일 발송
     */
    @Transactional
    public void sendNotificationMail(Long notificationNo) {

        // 1. 알림 조회
        Notification notification = notificationRepository
                .findById(notificationNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 알림을 찾을 수 없습니다."
                        )
                );

        // 2. 회원 조회
        Member member = memberRepository
                .findById(notification.getMno())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "알림 수신 회원을 찾을 수 없습니다."
                        )
                );

        // 3. 이메일 확인
        String email = member.getEmail();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "회원 이메일이 등록되어 있지 않습니다."
            );
        }

        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try {

            // 발송 중
            notification.setStatus("SENDING");

            // ----------------------------------------
            // 이슈 위치 이미지 URL 생성
            // ----------------------------------------
            // NOTIFICATION.ASMNO에 연결된
            // AIISSUEMAP.FSAVED 값을 조회한다.
            //
            // 도면이 없는 경우에는 ASMNO가 NULL이므로
            // 이미지 URL도 NULL 상태로 유지한다.
            // ----------------------------------------
            String imageUrl = null;

            Long asmno = notification.getAimapno();

            if (asmno != null) {

                // AIISSUEMAP.NO = ASMNO 조건으로
                // 생성된 이슈 이미지 파일명 조회
                String fsaved = notificationRepository
                        .findFsavedByAsmno(asmno)
                        .orElse(null);

                // 실제 파일명이 존재하는 경우에만
                // FastAPI 이미지 조회 URL 생성
                if (fsaved != null && !fsaved.isBlank()) {
                    imageUrl =
                            "http://10.1.205.118:11200/api/shopmap/image/"
                            + fsaved;
                }
            }
             
            // 이메일 발송
            mailService.sendNotificationMail(
                email,
                notification.getAtitle(),
                notification.getContent(),
                imageUrl
            );

            // 5. 성공
            notification.setStatus("SENT");

            // 6. 성공 로그 저장
            SendLog sendLog = SendLog.builder()
                    .nno(notification.getNo())
                    .channel("EMAIL")
                    .status(1)
                    .message("이메일 발송 성공")
                    .cdate(now)
                    .build();

            sendLogRepository.save(sendLog);

        } catch (Exception e) {

            // 실패 상태
            notification.setStatus("FAILED");

            // 실패 로그 저장
            SendLog sendLog = SendLog.builder()
                    .nno(notification.getNo())
                    .channel("EMAIL")
                    .status(0)
                    .message(e.getMessage())
                    .cdate(now)
                    .build();

            sendLogRepository.save(sendLog);

            throw e;
        }
    }
}