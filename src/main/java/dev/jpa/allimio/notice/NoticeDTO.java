package dev.jpa.allimio.notice;

import dev.jpa.allimio.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NoticeDTO {

  /**
   * 공지사항 작성
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class NCRequest{
    /** 관리자 번호 (FK -> MANAGER.NO) */
    private Long ano;
    /** 공지 유형 (0: 긴급, 1: 중요, 2: 알림, 3: 신규) */
    @Builder.Default
    private int type = 3;
    /** 공지 제목 */
    private String title;
    /** 공지 내용 */
    private String content;
    /** 등록 일시 */
    private String cdate;
    /** 등록글 비밀번호 */
    private String pw;
    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    private String fixyn = "N";
    /** 첨부파일 존재여부 (Y/N) */
    @Builder.Default
    private String fileyn = "N";
    /** 출력 모드(Y/N) */
    @Builder.Default
    private String vmode = "Y";
    /** 출력 순서 */
    @Builder.Default
    private int vseq = 0;
    
    public Notice toEntity() {
      return Notice.builder().ano(this.ano).type(this.type).title(this.title).content(this.content)
          .cdate(Tool.getDate()).pw(this.pw).fixyn(this.fixyn != null ? this.fixyn : "N")
          .fileyn(this.fileyn != null ? this.fileyn : "N").vmode(this.vmode != null ? this.vmode : "Y")
          .isdel("N").build();
    }
    
    // 수정
    public void applyUpdateTo(Notice notice) {
      notice.updateNotice(this.type, this.title, this.content, Tool.getDate(), this.pw, this.fixyn, this.fileyn, this.vmode, this.vseq);
    }
  }
  

  /**
   * 등록 후 글 상세 응답용 DTO
   */
  @Getter
  @AllArgsConstructor
  @Builder
  public static class NoticeResponse {
    /** 공지사항 고유 번호 (PK) */
    private Long no;
    /** 관리자 번호 (FK -> MANAGER.NO) */
    private Long ano;
    /** 공지 유형 (0: 긴급, 1: 중요, 2: 알림, 3: 신규) */
    private int type;
    /** 공지 제목 */
    private String title;
    /** 공지 내용 */
    private String content;
    /** 등록 일시 */
    private String cdate;
    /** 조회수 */
    private int vcnt;
    /** 상단고정 여부 (Y/N) */
    private String fixyn;
    /** 첨부파일 존재여부 (Y/N) */
    private String fileyn;
    /** 출력 모드(Y/N) */
    private String vmode;
    /** 출력 순서 */
    private int vseq;
    /** 삭제 여부 (Y/N) */
    private String isdel;
    /** 삭제 일시 */
    private String ddate;
    
    public static NoticeResponse fromEntity(Notice entity) {
      return NoticeResponse.builder().no(entity.getNo()).ano(entity.getAno()).type(entity.getType())
          .title(entity.getTitle()).content(entity.getContent()).cdate(entity.getCdate())
          .vcnt(entity.getVcnt()).fixyn(entity.getFixyn()).fileyn(entity.getFileyn())
          .vmode(entity.getVmode()).vseq(entity.getVseq()).isdel(entity.getIsdel()).build();
    }
  }
  
  // 삭제 요청용 DTO
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeleteRequest {
    private Long no; // 삭제할 글 번호
    private String pw; // 사용자가 입력한 비밀번호
    private String ddate; // 삭제 일시 저장
  }
  

  /**
   * 목록 검색 조건 DTO
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class NoticeSearchRequest {
    /** 검색어 (제목, 내용 등) */
    private String word;

    /** 공지 유형 (0: 긴급, 1: 중요, 2: 신규, 3:알림 ) */
    private Integer type; // 💡 null 허용을 위해 int 대신 Integer 사용

    /** 상단고정 여부 (Y/N) */
    private String fixyn;
  }
  
  
}
