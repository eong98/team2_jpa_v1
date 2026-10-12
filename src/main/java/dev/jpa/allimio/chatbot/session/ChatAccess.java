package dev.jpa.allimio.chatbot.session;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   챗봇 상담 요청자 확인 (ChatSessionCont, ChatLogCont 공용)

   챗봇 API는 비회원도 쓰기 때문에 로그인 없이도 통과됩니다(SecurityConfig permitAll).
   그래서 "누구의 상담인가"는 여기서 확인합니다.

   - 회원  : 로그인 토큰(JWT)의 회원번호만 사용 — 화면이 보낸 mno 값은 믿지 않음
             (예전엔 주소의 mno 번호만 바꾸면 다른 회원의 상담 목록이 보였음)
   - 비회원: 브라우저 식별값(gno, 화면이 localStorage에 만든 무작위 UUID)
   - 관리자: 회원이 아니므로 비회원처럼 gno로 처리
             (관리자번호를 회원번호 자리에 넣으면 번호가 같은 회원의 상담과 섞임)

   화면이 mno를 보내는 것은 "나는 로그인한 회원"이라는 알림 용도입니다.
   알림은 있는데 유효한 회원 토큰이 없으면(액세스 토큰 만료 등) 401을 돌려줘서
   화면(axios 인터셉터)이 토큰을 재발급받고 다시 요청하게 합니다.
   이렇게 하지 않으면 토큰이 만료된 회원이 조용히 비회원으로 처리됩니다.
--------------------------------------------------------------------- */
@Component
@RequiredArgsConstructor
public class ChatAccess {

  private final ChatSessionRepository chatSessionRepository;

  /** 회원 로그인이면 토큰의 회원번호, 아니면 null (관리자·비회원) */
  public Long memberNoOrNull(Authentication auth) {
    boolean isMember = auth != null && auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_MEMBER"::equals);
    if (isMember && auth.getPrincipal() instanceof Long no) {
      return no;
    }
    return null;
  }

  /**
   * 요청자의 회원번호 — 토큰에서 꺼냄 (비회원·관리자는 null → gno로 식별)
   * @param claimedMno 화면이 보낸 mno (값은 쓰지 않고 "로그인 상태라고 알림"으로만 봄)
   */
  public Long resolveMemberNo(Authentication auth, Long claimedMno) {
    Long tokenMno = memberNoOrNull(auth);
    if (tokenMno == null && claimedMno != null) {
      // 로그인했다고 알렸는데 회원 토큰이 없음 → 401 (화면이 토큰 재발급 후 재시도)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다.");
    }
    return tokenMno;
  }

  /**
   * 상담방 접근 확인 — 회원 소유 상담은 그 회원의 토큰이 있어야 함
   * (비회원 상담은 추측할 수 없는 상담방 번호(UUID) 자체가 열쇠)
   * 없는 상담이면 통과 — 뒤에서 서비스가 "존재하지 않는 세션"으로 처리
   */
  public void assertSessionAccess(String sno, Authentication auth) {
    if (sno == null) {
      return;
    }
    ChatSession session = chatSessionRepository.findById(sno).orElse(null);
    if (session == null || session.getMno() == null) {
      return;
    }
    Long tokenMno = memberNoOrNull(auth);
    if (tokenMno == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    if (!tokenMno.equals(session.getMno())) {
      // 남의 상담 — 있는지 없는지도 알려주지 않음
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상담입니다.");
    }
  }
}
