package dev.jpa.allimio.chatbot.session;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/* ---------------------------------------------------------------------
   챗봇 연결용 티켓 발급 (FastAPI가 검증 — FastAPI chatbot/ticket.py)

   FastAPI(AI 서버)는 Spring의 로그인 토큰(HttpOnly 쿠키)을 볼 수 없어서,
   예전에는 화면이 보내는 회원번호를 그대로 믿었습니다.
   Spring이 요청자를 확인한 뒤 짧은 수명의 티켓을 발급하고, FastAPI는 서명만 검증합니다.

     티켓 = HS256 JWT { sub: "m:회원번호" | "g:비회원식별값" | "a:관리자번호" | "server", typ: "chat", exp }
     - 화면  : GET /chat_session/ticket 으로 받아서 FastAPI 호출 시 X-Chat-Ticket 헤더(웹소켓은 ?ticket=)로 전달
     - 서버  : Spring이 FastAPI를 직접 부를 때(상담 제목 요약, 문의 AI 답변)는 "server" 티켓

   두 서버가 같은 비밀키(환경변수 CHAT_TICKET_SECRET, 32자 이상)를 가질 때만 동작합니다.
   키가 없으면 티켓을 발급하지 않고(null) FastAPI도 검사하지 않아 예전처럼 동작합니다.
   로그인 JWT 키(jwt.secret)와는 다른 키를 써야 합니다 (AI 서버에 로그인 키를 주지 않기 위해).
--------------------------------------------------------------------- */
@Component
public class ChatTicketProvider {

  /** 티켓 유효시간 10분 — 화면은 만료 전에 새로 받음 */
  public static final long EXPIRE_MILLIS = 10 * 60_000L;
  public static final String HEADER = "X-Chat-Ticket";

  private static final String TYPE = "chat";
  private static final String SERVER_SUBJECT = "server";

  /** 비밀키가 없으면 null — 티켓 기능 꺼짐 */
  private final SecretKey secretKey;

  public ChatTicketProvider(@Value("${chat.ticket.secret:}") String secret) {
    byte[] keyBytes = secret == null ? new byte[0] : secret.trim().getBytes(StandardCharsets.UTF_8);
    this.secretKey = keyBytes.length >= 32 ? Keys.hmacShaKeyFor(keyBytes) : null;
    System.out.println("-> 챗봇 연결용 티켓: " + (isEnabled() ? "사용" : "미사용 (CHAT_TICKET_SECRET 없음)"));
  }

  public boolean isEnabled() {
    return secretKey != null;
  }

  /** 회원용 티켓 */
  public String issueForMember(Long mno) {
    return issue("m:" + mno);
  }

  /** 비회원(관리자 포함)용 티켓 — 브라우저 식별값 기준 */
  public String issueForGuest(String gno) {
    return issue("g:" + gno);
  }

  /** 관리자 화면용 티켓 — 매뉴얼·옵션메뉴 관리 API(/api/chatbot/manual-doc/*) */
  public String issueForManager(Long ano) {
    return issue("a:" + ano);
  }

  /** Spring 서버가 FastAPI를 직접 부를 때 쓰는 티켓 */
  public String issueForServer() {
    return issue(SERVER_SUBJECT);
  }

  private String issue(String subject) {
    if (!isEnabled()) {
      return null;
    }
    Date now = new Date();
    return Jwts.builder()
        .setSubject(subject)
        .claim("typ", TYPE)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + EXPIRE_MILLIS))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }
}
