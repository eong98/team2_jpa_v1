package dev.jpa.allimio.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 클라이언트의 모든 HTTP 요청을 가로채 JWT 토큰의 유효성을 검사하고,
 * 유효한 토큰일 경우 Spring Security 컨텍스트에 인증 정보(Authentication)를 등록하는 커스텀 필터.
 * 
 * OncePerRequestFilter를 상속받아 한 번의 요청당 딱 한 번만 실행되는 것을 보장합니다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 필터의 핵심 로직을 수행하는 메서드.
     * 요청 헤더에서 JWT를 추출하고, 검증 성공 시 SecurityContext에 사용자 인증 정보를 세팅합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더(Authorization)에서 "Bearer " 접두사를 제거한 순수 JWT 토큰 추출
        String token = resolveToken(request);
        
        // 2. 토큰이 존재하고 유효성 검사(위변조, 만료 여부 등)를 통과한 경우에만 인증 처리 진행
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰 페이로드(Claims)에서 사용자 식별 번호(PK), 역할(Role), 회원 등급(Grade) 추출
            Long no = jwtTokenProvider.getNo(token);
            String role = jwtTokenProvider.getRole(token);   // "MEMBER" | "MANAGER"
            Integer grade = jwtTokenProvider.getGrade(token);

            // 3. 인가(Authorization) 처리를 위한 권한 목록 구성
            // Spring Security 규격에 맞춰 역할에는 "ROLE_" 접두사를, 등급에는 "GRADE_" 접두사를 부여
            // 예: hasRole('MEMBER'), hasAuthority('GRADE_1') 등으로 세분화된 접근 제어 가능
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role),
                    new SimpleGrantedAuthority("GRADE_" + grade)
            );

            // 4. 인증 토큰 객체 생성
            // principal: 사용자 식별 번호(no) 전달 (이후 Controller에서 @AuthenticationPrincipal 등으로 참조)
            // credentials: 이미 JWT로 검증을 마쳤으므로 비밀번호 같은 민감 정보는 null 처리
            // authorities: 부여된 권한 목록
            var authentication = new UsernamePasswordAuthenticationToken(no, null, authorities);

            // 5. SecurityContextHolder에 인증 정보를 등록하여 현재 쓰레드 내에서 인증된 사용자로 처리
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. 다음 필터 또는 서블릿(Controller)으로 요청 및 응답 전달
        // (토큰이 없거나 유효하지 않아도 필터 체인을 계속 진행하며, 인증 실패 처리는 후속 시큐리티 엔트리포인트에서 담당)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT를 파싱해 순수 토큰 문자열만 반환하는 헬퍼 메서드.
     * 
     * @param request HTTP 요청 객체
     * @return "Bearer "를 제거한 JWT 문자열 (헤더가 없거나 형식이 맞지 않으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        // "Authorization" 헤더 값 조회 (형식: "Bearer eyJhbGciOi...")
        String bearer = request.getHeader("Authorization");

        // 표준 Bearer 스키마를 준수하는지 확인 후 접두사(7글자)를 잘라내고 토큰 본문만 추출
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}