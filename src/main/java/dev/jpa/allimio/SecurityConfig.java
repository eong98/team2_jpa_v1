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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
//1. 비밀번호 암호화 빈 등록 (BCrypt 방식)
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
              
              // 로그인, 회원가입, 비밀번호 찾기/재설정, 토큰 재발급 등 인증 없이 접근할 URL
              .requestMatchers(
                  "/v1/user/login",
                  "/v1/user/save",
                  "/v1/user/check/{id}",
                  "/v1/dbms/login"
              ).permitAll()
              
              // 개발/테스트 단계이므로 우선 모든 요청 허용 (추후 JWT 인증 필터 적용)
              .anyRequest().permitAll()
//              .anyRequest().authenticated()  -> 개발 완료후 전환
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

      config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "*"));
      config.setAllowCredentials(true);
      config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", config);
      return source;
  }

}
