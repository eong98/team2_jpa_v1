package dev.jpa.allimio.attach;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import dev.jpa.allimio.qa.Qa;
import dev.jpa.allimio.qa.QaGuestTokenProvider;
import dev.jpa.allimio.qa.QaRepository;
import lombok.RequiredArgsConstructor;

/* ---------------------------------------------------------------------
   첨부파일 등록·삭제 권한 확인

   첨부 등록·삭제 API는 비회원 문의 때문에 로그인 없이 호출할 수 있게 열려 있습니다.
   예전에는 그 글의 주인인지 확인하지 않아서, 주소만 알면 누구나 남의 글 첨부를 지울 수 있었습니다.

   게시판별 규칙
   - 관리자            : 전부 가능
   - 공지(NOTICE)      : 관리자만
   - 문의(QA)
       FAQ             : 관리자만
       회원 글         : 그 글을 쓴 회원 (로그인 토큰의 회원번호)
       비회원 글       : 그 글의 임시 토큰(X-Qa-Token — 비밀번호 확인 또는 글 등록 직후 발급)을 가진 사람
       없는 글         : 관리자만 (영구 삭제 뒤 정리 등)
   - 그 외(CCTV 이슈, 고객설문 등): 담당 기능의 규칙을 몰라 예전처럼 둠

   글을 삭제한 직후(소프트 삭제 상태)에도 작성자 본인은 첨부를 정리할 수 있습니다 — 삭제 여부는 보지 않음.
   비회원 경로는 401/403을 쓰지 않습니다(화면의 axios가 로그인 화면으로 보내기 때문) → 로그인하지 않았으면 400.
--------------------------------------------------------------------- */
@Component
@RequiredArgsConstructor
public class AttachAccess {

  private final AttachRepository attachRepository;
  private final QaRepository qaRepository;
  private final QaGuestTokenProvider guestTokenProvider;

  /**
   * 이 글(tname + bno)의 첨부를 등록·삭제할 수 있는지 — 안 되면 예외
   *
   * @param qaToken 비회원 문의글 임시 토큰 (X-Qa-Token 헤더, 없으면 null)
   */
  public void assertCanManage(Authentication auth, String qaToken, String tname, Long bno) {
    if (hasRole(auth, "ROLE_MANAGER") || bno == null) {
      return;
    }
    String board = resolveBoard(tname, bno);
    if (board == null) {
      return; // 어느 게시판인지 알 수 없음 — 서비스도 아무것도 하지 않는 경우
    }

    if ("NOTICE".equals(board)) {
      throw denied(auth);
    }
    if ("QA".equals(board)) {
      Qa qa = qaRepository.findById(bno).orElse(null);
      if (qa == null || "Y".equals(qa.getIsfaq())) {
        throw denied(auth);
      }
      boolean isOwner;
      if (qa.getMno() != null) {
        isOwner = hasRole(auth, "ROLE_MEMBER") && qa.getMno().equals(auth.getPrincipal());
      } else {
        isOwner = guestTokenProvider.isValid(qaToken, bno);
      }
      if (!isOwner) {
        throw denied(auth);
      }
    }
    // 그 외 게시판은 예전처럼 통과
  }

  /** 첨부 번호로 — 그 첨부가 달린 글 기준으로 확인 (없는 첨부는 통과: 서비스가 0건 처리) */
  public void assertCanManage(Authentication auth, String qaToken, Long attachNo) {
    if (attachNo == null) {
      return;
    }
    attachRepository.findById(attachNo)
        .ifPresent(attach -> assertCanManage(auth, qaToken, attach.getTname(), attach.getBno()));
  }

  /** 게시판 이름(대문자) — tname이 없으면 이 글번호의 첨부가 한 게시판에만 있을 때 그 게시판 */
  private String resolveBoard(String tname, Long bno) {
    if (tname != null && !tname.isBlank()) {
      return tname.trim().toUpperCase();
    }
    List<Long> tnos = attachRepository.findTnosByBno(bno);
    if (tnos.size() != 1) {
      return null;
    }
    return attachRepository.findByTnoAndBno(tnos.get(0), bno).stream()
        .map(Attach::getTname)
        .filter(name -> name != null && !name.isBlank())
        .map(name -> name.trim().toUpperCase())
        .findFirst()
        .orElse(null);
  }

  private ResponseStatusException denied(Authentication auth) {
    boolean loggedIn = hasRole(auth, "ROLE_MEMBER") || hasRole(auth, "ROLE_MANAGER");
    return new ResponseStatusException(
        loggedIn ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST,
        "이 글의 첨부파일을 변경할 권한이 없습니다.");
  }

  private boolean hasRole(Authentication auth, String role) {
    return auth != null && auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
  }
}
