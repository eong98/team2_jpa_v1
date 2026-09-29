package dev.jpa.allimio;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import dev.jpa.allimio.jwt.JwtAuthenticationFilter;
import dev.jpa.allimio.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
//1. 비밀번호 암호화 빈 등록 (BCrypt 방식)
  
  private final JwtTokenProvider jwtTokenProvider;
  
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }

  // 2. HTTP 보안 체인 설정
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          // CORS 설정 연동
          .cors(Customizer.withDefaults())
          
          // CSRF 비활성화 (JWT/REST API 통신 방식 사용 시 비활성화)
          .csrf(AbstractHttpConfigurer::disable)
          
          // 세션 미사용 (Stateless 모드)
          .sessionManagement(session -> 
              session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          )
          
          // 요청 URL별 권한 설정
          .authorizeHttpRequests(auth -> auth
              // 브라우저의 OPTIONS 프리플라이트 요청 전부 허용
              .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
              
              // 로그인, 회원가입, 비밀번호 기/재설정, 토큰 재발급 등 인증 없이 접근할 URL
              .requestMatchers(
                  "/v1/user/login",
                  "/v1/user/save",
                  "/v1/user/check/{id}",
                  "/v1/dbms/login",
                  "/v1/user/find-id",
                  "/v1/user/reset/**",
                  "/auth/reissue",
                  "/auth/logout",      
                  "/chat_session/**",
                  "/chat_menu/**",
                  "/chat_log/**",
                  "/api/chatbot/**"
              ).permitAll()
              
              // 관리자 권한만 접근 가능
              .requestMatchers(
                  "/v1/dbms/**",                // 관리자 메뉴 전체
                  "/v1/user/find",               // 전체 회원 목록
                  "/v1/user/update/manager/**",  // 관리자가 회원 수정
                  "/v1/user/ban/**",             // 강제 탈퇴
                  "/history/**"                  // 로그인/수정 이력
                  ).hasRole("MANAGER")
              
              // 각 메뉴들의 등록/수정/삭제는 관리자만  -> 임시로 넣어둠 추후 수정
              .requestMatchers(HttpMethod.POST,   "/notice/**", "/inmenu/**", "/shopmenu/**", "/cctv_issue_code/**", "/shop_plan/**").hasRole("MANAGER")
              .requestMatchers(HttpMethod.PUT,    "/notice/**", "/inmenu/**", "/shopmenu/**", "/cctv_issue_code/**", "/shop_plan/**").hasRole("MANAGER")
              .requestMatchers(HttpMethod.DELETE, "/notice/**", "/inmenu/**", "/shopmenu/**", "/cctv_issue_code/**", "/shop_plan/**").hasRole("MANAGER")
              
              // 회원 권한만 접근 가능
              .requestMatchers(
                  "/v1/user/**" 
                  ).hasRole("MEMBER")
              
              
              // 개발/테스트 단계이므로 우선 모든 요청 허용 (추후 JWT 인증 필터 적용)
//              .anyRequest().permitAll()
              .anyRequest().authenticated()  // -> 개발 완료후 전환
          )
          .exceptionHandling(ex -> ex
              .authenticationEntryPoint((req, res, e) ->
                  res.sendError(HttpServletResponse.SC_UNAUTHORIZED))   // 토큰 없음/만료 → 401
              .accessDeniedHandler((req, res, e) ->
                  res.sendError(HttpServletResponse.SC_FORBIDDEN))      // role(권한) 불일치 → 403
          )
          
          .addFilterBefore(
              new JwtAuthenticationFilter(jwtTokenProvider), 
              UsernamePasswordAuthenticationFilter.class
              );

      return http.build();
  }

  // 3. React와의 통신을 위한 CORS 상세 설정
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration config = new CorsConfiguration();
      
      // React 접속 출처 허용
      config.setAllowedOriginPatterns(List.of(
          "*"
          // 실제 배포시 변경
//          "https://my-service.com",
//          "https://admin.my-service.com"
          ));
      
      config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

      config.setAllowedHeaders(Arrays.asList( "Cache-Control", "Content-Type", "*"));
      config.setAllowCredentials(true);

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", config);
      return source;
  }

}
