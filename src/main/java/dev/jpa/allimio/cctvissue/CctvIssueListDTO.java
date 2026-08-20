package dev.jpa.allimio.cctvissue;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 목록(/cctv_issue/search) 조회 전용 DTO.
 *
 * hasAttach: ATTACH 테이블(TNAME='CCTV_ISSUE', BNO=이슈번호)에 등록된 첨부파일이
 * 하나라도 있는지 여부입니다. 목록에서 "보기"를 누르기 전에 바로 첨부 유무를 보여주기 위한 값으로,
 * 행마다 별도 쿼리(N+1)를 날리지 않도록 CctvIssueRepository의 search/searchByShop 쿼리 안에서
 * EXISTS 서브쿼리로 함께(단일 쿼리) 계산해서 채웁니다.
 */
@Getter
@Setter
@ToString
public class CctvIssueListDTO {

  private long no;
  private long cno;
  private Long mno; // 처리담당자, nullable
  private String code;
  private int state;
  private String comnet;
  private String reliability;
  private String pdate;
  private String noticeyn;
  private String cdate;
  private boolean hasAttach;

  public CctvIssueListDTO() {
  }

  public CctvIssueListDTO(long no, long cno, Long mno, String code, int state, String comnet,
      String reliability, String pdate, String noticeyn, String cdate, boolean hasAttach) {
    this.no = no;
    this.cno = cno;
    this.mno = mno;
    this.code = code;
    this.state = state;
    this.comnet = comnet;
    this.reliability = reliability;
    this.pdate = pdate;
    this.noticeyn = noticeyn;
    this.cdate = cdate;
    this.hasAttach = hasAttach;
  }
}