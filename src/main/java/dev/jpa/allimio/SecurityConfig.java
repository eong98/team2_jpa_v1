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
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
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
                  "/api/chatbot/**",
                  "/shop_plan/list",
                  "/shop_survey/public/**",
                  "/v1/user/find/**"
              ).permitAll()
              
           // Swagger / OpenAPI 관련 경로 전체 허용
              .requestMatchers(
                  "/swagger-ui/**",
                  "/swagger-ui.html",
                  "/api-docs/**",
                  "/swagger-resources/**",
                  "/webjars/**",
                  // 서버 오류 페이지 — 막혀 있으면 허용된 API에서 난 오류(500)도 전부 401로 보여 원인을 알 수 없음
                  "/error"
              ).permitAll()

              // 삭제된 공지·문의 목록/영구 삭제는 관리자만 — 아래 GET /notice/{no} 비회원 허용보다 먼저 와야 함
              .requestMatchers("/notice/deleted", "/notice/deleted/**", "/qa/deleted", "/qa/deleted/**").hasRole("MANAGER")

              // 비회원 고객센터(/board) — 문의 목록/상세/등록/수정/삭제, 첨부파일 보기
              // (비회원 비밀글 열람·수정·삭제는 POST /qa/guest/{no}/verify로 받은 임시 토큰으로 서버에서 검증)
              .requestMatchers(HttpMethod.GET,
                  "/qa/list", "/qa/guest/list", "/qa/faq", "/qa/guest/{no}",
                  "/notice/list", "/notice/{no}",
                  "/attach/read/{no}", "/attach/list/{bno}",
                  // 업로드 전 저장 경로 확인 — 막혀 있으면 비회원 문의 첨부 시 401 → 로그인 화면으로 튕김
                  "/attach/check",
                  // 첨부 이미지 파일 자체 (WebMvcConfiguration 정적 경로) — 비회원 FAQ·공지 이미지가 401로 안 보이던 문제
                  "/attach/storage/**").permitAll()
              .requestMatchers(HttpMethod.POST, "/qa", "/qa/guest/{no}/verify", "/attach/create").permitAll()
              .requestMatchers(HttpMethod.PUT, "/qa/guest/{no}").permitAll()
              .requestMatchers(HttpMethod.DELETE, "/qa/guest/{no}", "/attach/delete_by_bno/{bno}", "/attach/delete/{no}").permitAll()

              // 문의 답변·FAQ 등록/수정·관리자 삭제(DELETE /qa)는 관리자만
              // (회원 상세·수정·삭제 /qa/{no}, 내 문의 /qa/my/**는 아래 anyRequest().authenticated() + 컨트롤러에서 본인 확인)
              .requestMatchers(HttpMethod.PUT, "/qa/reply/{no}", "/qa/faq/{no}").hasRole("MANAGER")
              .requestMatchers(HttpMethod.POST, "/qa/faq").hasRole("MANAGER")
              .requestMatchers(HttpMethod.DELETE, "/qa").hasRole("MANAGER")

              // 관리자 권한만 접근 가능
              .requestMatchers(
                  "/v1/dbms/**",                // 관리자 메뉴 전체
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
