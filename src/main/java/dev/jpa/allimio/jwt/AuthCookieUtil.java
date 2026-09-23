package dev.jpa.allimio.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 인증에 사용하는 JWT Cookie를 생성하는 유틸리티 클래스.
 *
 * 현재는
 * 1. Access Token
 * 2. Refresh Token
 *
 * 두 개의 Cookie를 관리한다.
 */
public class AuthCookieUtil {

    // Access Token을 저장할 Cookie 이름
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    // Refresh Token을 저장할 Cookie 이름
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /**
     * 유틸리티 클래스이므로 객체를 직접 생성하지 못하도록
     * 생성자를 private으로 설정.
     */
    private AuthCookieUtil() {
    }

    /**
     * Access Token을 HttpOnly Cookie로 만들어 응답에 추가.
     *
     * @param response HTTP 응답 객체
     * @param accessToken 저장할 Access Token
     * @param maxAge Cookie 유지 시간(초)
     */
    public static void addAccessTokenCookie(
            HttpServletResponse response,
            String accessToken,
            int maxAge) {

        // Access Token Cookie 생성
        Cookie cookie = new Cookie(
                ACCESS_TOKEN_COOKIE,
                accessToken
        );

        // JavaScript에서 Cookie 값을 읽지 못하도록 설정
        // → XSS로 Token이 탈취되는 위험을 줄인다.
        cookie.setHttpOnly(true);

        // HTTPS 연결에서만 Cookie를 전송하도록 설정 -> HTTPS 배포시 TRUE로
        cookie.setSecure(false);

        // 사이트 전체 경로에서 Cookie를 사용할 수 있도록 설정
        cookie.setPath("/");

        // Cookie 유지 시간을 초 단위로 설정
        cookie.setMaxAge(maxAge);

        // HTTP 응답에 Cookie 추가
        response.addCookie(cookie);
    }

    /**
     * Refresh Token을 HttpOnly Cookie로 만들어 응답에 추가한다.
     *
     * @param response HTTP 응답 객체
     * @param refreshToken 저장할 Refresh Token
     * @param maxAge Cookie 유지 시간(초)
     */
    public static void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            int maxAge) {

        // Refresh Token Cookie 생성
        Cookie cookie = new Cookie(
                REFRESH_TOKEN_COOKIE,
                refreshToken
        );

        // JavaScript에서 Refresh Token을 읽지 못하도록 설정
        cookie.setHttpOnly(true);

        // HTTPS 연결에서만 Cookie를 전송하도록 설정 -> HTTPS 배포시 TRUE로
        cookie.setSecure(false);

        // 사이트 전체 경로에서 Cookie를 사용할 수 있도록 설정
        cookie.setPath("/");

        // Cookie 유지 시간을 초 단위로 설정
        cookie.setMaxAge(maxAge);

        // HTTP 응답에 Cookie 추가
        response.addCookie(cookie);
    }
}