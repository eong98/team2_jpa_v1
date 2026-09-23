package dev.jpa.allimio.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT(JSON Web Token)의 생성, 파싱, 서명 검증 및 만료 시간 관리를 전담하는 유틸리티 컴포넌트.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // application.properties에서 설정한 비밀키 문자열 주입
    @Value("${jwt.secret}")
    private String secretKeyString;

    // JJWT에서 HMAC-SHA 알고리즘 암호화 및 서명 검증에 사용할 SecretKey 객체
    private SecretKey secretKey;

    // Access Token 유효 시간: 30분 (밀리초 단위)
//    private static final long ACCESS_TOKEN_EXPIRE = 1000L * 60 * 30;
    private static final long ACCESS_TOKEN_EXPIRE = 1000L * 10;
    // Refresh Token 유효 시간: 14일 (밀리초 단위)
    private static final long REFRESH_TOKEN_EXPIRE = 1000L * 60 * 60 * 24 * 14;

    /**
     * 의존성 주입이 완료된 후 실행되는 초기화 메서드.
     * 평문 비밀키 문자열을 UTF-8 바이트 배열로 변환하여 HMAC-SHA용 암호화 키 객체로 변환합니다.
     */
    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * API 인가 처리에 사용할 Access Token 생성.
     *
     * @param no    사용자 식별 번호(PK) - Subject로 등록
     * @param role  사용자 권한 구분 (예: "MEMBER", "MANAGER")
     * @param grade 사용자 등급 (예: 1, 2)
     * @return 서명된 JWT Access Token 문자열
     */
    public String createAccessToken(Long no, String role, Integer grade) {
        // 페이로드(Claims)에 사용자 식별 정보 및 권한 정보 등록
        Claims claims = Jwts.claims().setSubject(String.valueOf(no));
        claims.put("role", role);
        claims.put("grade", grade);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 데이터 페이로드 설정
                .setIssuedAt(now)  // 토큰 발행 시간 (iat)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE)) // 만료 시간 (exp)
                .signWith(secretKey, SignatureAlgorithm.HS256) // 비밀키 및 HS256 알고리즘 서명
                .compact(); // 압축 및 직렬화하여 JWT 문자열 완성
    }

    /**
     * Access Token 재발급 검증에 사용할 Refresh Token 생성.
     * 보안을 위해 Access Token보다 최소한의 정보(식별자, 역할)만 페이로드에 포함합니다.
     *
     * @param no   사용자 식별 번호(PK)
     * @param role 사용자 권한 구분
     * @return 서명된 JWT Refresh Token 문자열
     */
    public String createRefreshToken(Long no, String role) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(no));
        claims.put("role", role);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE)) // 14일 유효
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 만료 시간(밀리초) 반환.
     * 주로 Redis에 Refresh Token 저장 시 TTL(Time-To-Live) 설정용으로 사용됩니다.
     */
    public long getRefreshTokenExpireMillis() {
        return REFRESH_TOKEN_EXPIRE;
    }

    /**
     * JWT 토큰의 유효성을 검사합니다.
     * 변조 여부, 만료 여부, 올바른 포맷인지 확인하여 예외 상황별로 로그를 남깁니다.
     *
     * @param token 검증할 JWT 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            // secretKey를 사용해 서명을 검증하고 토큰 파싱 시도
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | SignatureException e) {
            log.warn("유효하지 않은 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException | MalformedJwtException e) {
            log.warn("잘못된 JWT 토큰 형식입니다.");
        }
        return false;
    }

    /**
     * 토큰 페이로드(Claims) 전체를 파싱하여 반환합니다.
     * 토큰이 만료되어 파싱 중 ExpiredJwtException이 터지더라도,
     * 재발급 로직 등에서 사용자 식별자(Subject)를 조회할 수 있도록 만료된 클레임을 안전하게 반환합니다.
     *
     * @param token 파싱할 JWT 문자열
     * @return 토큰에 저장된 Claims 객체
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * 토큰의 Subject 클레임에서 사용자 식별 번호(PK)를 꺼내 Long 타입으로 변환하여 반환합니다.
     */
    public Long getNo(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * 토큰의 "role" 커스텀 클레임 값(MEMBER, MANAGER 등)을 문자열로 반환합니다.
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * 토큰의 "grade" 커스텀 클레임 값(회원 등급)을 Integer 타입으로 반환합니다.
     */
    public Integer getGrade(String token) {
        return getClaims(token).get("grade", Integer.class);
    }
}