package dev.jpa.allimio.qa;

import lombok.Getter;

/**
 * 문의사항 접근 오류 — 컨트롤러에서 {status, success:false, code, message}로 응답합니다.
 *
 * 비회원 경로(/qa/guest/**)는 401/403을 쓰지 않습니다.
 * (React axios 인터셉터가 401/403이면 로그인 토큰 재발급을 시도하고, 실패하면 로그인 화면으로 보내기 때문)
 */
@Getter
public class QaAccessException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** HTTP 상태 코드 */
  private final int status;
  /** 화면 분기용 코드 (NOT_FOUND, FORBIDDEN, PW_MISMATCH, LOCKED, TOKEN_REQUIRED) */
  private final String code;

  public QaAccessException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public static QaAccessException notFound() {
    return new QaAccessException(404, "NOT_FOUND", "존재하지 않거나 삭제된 게시글입니다.");
  }
}
