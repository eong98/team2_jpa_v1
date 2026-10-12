package dev.jpa.allimio.qa.mail;

import java.util.List;

/** 문의 메일 발송 내역(관리자 /dbms/qamail) 응답 — React components/ts/QaMail.ts와 1:1 */
public class QaMailDTO {

  /**
   * 발송 내역 한 줄
   *
   * @param qaTitle    문의 제목 — 문의글이 삭제됐으면 null (화면에서 "삭제된 게시글"로 표시)
   * @param qaDeleted  문의글이 삭제(소프트 삭제·영구 삭제)됐는지
   * @param memberName 받는 회원 이름 — 비회원이면 null
   * @param canResend  [다시 보내기] 가능 여부 — 실패한 답변 알림이고, 글이 살아 있고, 이후에 성공한 발송이 없을 때
   */
  public record LogRow(
      long no,
      long qno,
      String qaTitle,
      boolean qaDeleted,
      Long mno,
      String memberName,
      String email,
      int stype,
      String title,
      String content,
      String result,
      String msg,
      String cdate,
      boolean canResend) {
  }

  /** 발송 내역 목록 (페이지) */
  public record LogPage(List<LogRow> content, long totalElements, int totalPages, int page, int size) {
  }
}
