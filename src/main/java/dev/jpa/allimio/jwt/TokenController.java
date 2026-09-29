package dev.jpa.allimio.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(
        HttpServletRequest request,
        HttpServletResponse response){

        // Refresh Token 추출
        String refreshToken = null;

        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if(AuthCookieUtil.REFRESH_TOKEN_COOKIE.equals(cookie.getName())){
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Refresh Token이 없는 경우
        if(refreshToken == null){
            return ResponseEntity.status(401).body("리프레시 토큰이 없습니다.");
        }

        // Refresh Token 유효성 검사
        if(!jwtTokenProvider.validateToken(refreshToken)){
            return ResponseEntity.status(401).body("리프레시 토큰이 유효하지 않습니다.");
        }

        Long no = jwtTokenProvider.getNo(refreshToken);
        String role = jwtTokenProvider.getRole(refreshToken);
        Integer grade = jwtTokenProvider.getGrade(refreshToken);

        // Redis에 저장된 값과 실제로 일치하는지 확인
        if(!refreshTokenRedisRepository.matches(role, no, refreshToken)){
            return ResponseEntity.status(401).body("이미 폐기되었거나 알 수 없는 토큰입니다.");
        }

        // 새로운 Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(no, role, grade);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(no, role, grade);

        // Refresh Token Rotation
        refreshTokenRedisRepository.save(role, no, newRefreshToken, jwtTokenProvider.getRefreshTokenExpireMillis());

        // 새로운 Token을 HttpOnly Cookie에 저장
        AuthCookieUtil.addAccessTokenCookie(response, newAccessToken, 10);
        AuthCookieUtil.addRefreshTokenCookie(response, newRefreshToken, 60 * 60 * 24 * 14);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
        HttpServletRequest request,
        HttpServletResponse response){

        // Refresh Token 추출
        String refreshToken = null;

        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if(AuthCookieUtil.REFRESH_TOKEN_COOKIE.equals(cookie.getName())){
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Refresh Token이 있는 경우 Redis에서 삭제
        if(refreshToken != null && jwtTokenProvider.validateToken(refreshToken)){
            Long no = jwtTokenProvider.getNo(refreshToken);
            String role = jwtTokenProvider.getRole(refreshToken);

            refreshTokenRedisRepository.delete(role, no);
        }

        // Cookie 삭제
        AuthCookieUtil.addAccessTokenCookie(response, "", 0);
        AuthCookieUtil.addRefreshTokenCookie(response, "", 0);

        return ResponseEntity.ok().build();
    }
}