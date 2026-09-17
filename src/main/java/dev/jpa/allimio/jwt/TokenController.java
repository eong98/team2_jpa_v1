package dev.jpa.allimio.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public record ReissueReq(String refreshToken) {}
    public record TokenRes(String accessToken, String refreshToken) {}

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody ReissueReq req) {
        if (!jwtTokenProvider.validateToken(req.refreshToken())) {
            return ResponseEntity.status(401).body("리프레시 토큰이 유효하지 않습니다.");
        }

        Long no = jwtTokenProvider.getNo(req.refreshToken());
        String role = jwtTokenProvider.getRole(req.refreshToken());

        // Redis에 저장된 값과 실제로 일치하는지 확인 (탈취/재사용 방지)
        if (!refreshTokenRedisRepository.matches(role, no, req.refreshToken())) {
            return ResponseEntity.status(401).body("이미 폐기되었거나 알 수 없는 토큰입니다.");
        }

        // grade는 재발급 시점에 DB에서 다시 조회하는 게 안전(등급 변경 반영)
        Integer grade = jwtTokenProvider.getGrade(req.refreshToken()); // 필요시 DB 재조회로 교체

        String newAccessToken = jwtTokenProvider.createAccessToken(no, role, grade);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(no, role);

        // Refresh Token Rotation: 재발급마다 새로 교체 저장 (탈취 대응)
        refreshTokenRedisRepository.save(role, no, newRefreshToken, jwtTokenProvider.getRefreshTokenExpireMillis());

        return ResponseEntity.ok(new TokenRes(newAccessToken, newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody ReissueReq req) {
        Long no = jwtTokenProvider.getNo(req.refreshToken());
        String role = jwtTokenProvider.getRole(req.refreshToken());
        refreshTokenRedisRepository.delete(role, no);
        return ResponseEntity.ok().build();
    }
}