package dev.jpa.allimio.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Redis를 활용해 JWT Refresh Token의 저장, 조회, 검증 및 폐기(삭제)를 처리하는 저장소 클래스.
 * 인메모리 NoSQL인 Redis에 토큰을 저장하고 TTL(유효 기간)을 설정하여 메모리를 효율적으로 관리합니다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    // 문자열 기반의 Key-Value 연산을 수행하기 위한 Redis 템플릿 주입
    private final StringRedisTemplate redisTemplate;

    // Refresh Token 네임스페이스 구분을 위한 키 접두사
    private static final String PREFIX = "RT:";

    /**
     * Redis에 저장될 고유 키 문자열을 생성하는 헬퍼 메서드.
     * 권한(Role)과 사용자 식별 번호(PK)를 조합하여 사용자 유형 간 PK 중복 충돌을 방지합니다.
     *
     * @param role 사용자 역할 (예: "MEMBER", "MANAGER")
     * @param no   사용자 식별 번호 (PK)
     * @return 포맷팅된 Redis 키 (예: "RT:MEMBER:5", "RT:MANAGER:5")
     */
    private String key(String role, Long no) {
        return PREFIX + role + ":" + no;
    }

    /**
     * 사용자의 Refresh Token을 Redis에 저장하고 자동 만료 시간(TTL)을 설정합니다.
     * 이미 동일한 키가 존재할 경우 새 토큰 값과 만료 시간으로 덮어씁니다.
     *
     * @param role         사용자 역할
     * @param no           사용자 식별 번호 (PK)
     * @param refreshToken 저장할 JWT Refresh Token 문자열
     * @param expireMillis 토큰의 유효 기간 (밀리초 단위)
     */
    public void save(String role, Long no, String refreshToken, long expireMillis) {
        redisTemplate.opsForValue().set(
                key(role, no),
                refreshToken,
                Duration.ofMillis(expireMillis)
        );
    }

    /**
     * 해당 사용자의 역할과 식별 번호로 Redis에 저장된 Refresh Token을 조회합니다.
     * 만료 시간이 지났거나 로그아웃된 경우 null이 반환됩니다.
     *
     * @param role 사용자 역할
     * @param no   사용자 식별 번호 (PK)
     * @return 저장된 Refresh Token 문자열 (존재하지 않으면 null)
     */
    public String find(String role, Long no) {
        return redisTemplate.opsForValue().get(key(role, no));
    }

    /**
     * 해당 사용자의 Refresh Token을 Redis에서 즉시 삭제합니다.
     * 주로 사용자의 로그아웃 요청 시 기존 세션을 무효화하기 위해 호출됩니다.
     *
     * @param role 사용자 역할
     * @param no   사용자 식별 번호 (PK)
     */
    public void delete(String role, Long no) {
        redisTemplate.delete(key(role, no));
    }

    /**
     * 클라이언트가 재발급 요청 시 보낸 Refresh Token이 Redis에 보관된 최신 토큰과 일치하는지 대조합니다.
     * 토큰 탈취 여부 확인 및 이미 폐기된 토큰의 재사용 방지(RTR 검증)에 사용됩니다.
     *
     * @param role         사용자 역할
     * @param no           사용자 식별 번호 (PK)
     * @param refreshToken 클라이언트가 제출한 Refresh Token 문자열
     * @return 저장된 토큰과 일치하면 true, 불일치하거나 토큰이 없으면 false
     */
    public boolean matches(String role, Long no, String refreshToken) {
        String saved = find(role, no);
        return saved != null && saved.equals(refreshToken);
    }
}