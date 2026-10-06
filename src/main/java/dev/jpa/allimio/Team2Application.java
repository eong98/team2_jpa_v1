package dev.jpa.allimio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableScheduling — @Scheduled 배치를 실행하려면 반드시 필요합니다(없으면 오류 없이 실행만 안 됨).
 *   - ShopOrderService: 구독 만료 처리 (매일 0시)
 *   - ChatSessionScheduler: 방치된 챗봇 상담 자동 종료 (5분마다)
 */
@SpringBootApplication
@EnableScheduling
public class Team2Application {

    public static void main(String[] args) {
        SpringApplication.run(Team2Application.class, args);
    }

}
 