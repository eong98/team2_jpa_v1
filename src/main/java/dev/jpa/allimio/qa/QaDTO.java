package dev.jpa.allimio.qa;

import dev.jpa.allimio.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class QaDTO {

  /**
   * 1:1 문의글 작성 / 수정 요청 DTO
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class QCRequest {
    /** 문의 회원 번호 (FK -> MEMBER.NO) */
    private Long mno;
    /** 문의 유형 (0: 기타, 1: 관제신청, 2: 영상요청, 3: 장비장애) */
    private int type;
    /** 문의 제목 */
    private String title;
    /** 문의 내용 */
    private String content;
    /** 등록 일시 */
    private String cdate;
    /** 문의글 비밀번호 */
    private String pw;
    
    /** 비밀글 여부 (Y/N) */
    @Builder.Default
    private String vmode = "N";
    
    /** 자주묻는 질문(FAQ) 여부 */
    @Builder.Default
    private String isfaq = "N";
    
    /** 첨부파일 등록 여부 */
    @Builder.Default
    private String fileyn = "N";

    public Qa toEntity() {
      return Qa.builder()
          .mno(this.mno)
          .type(this.type)
          .title(this.title)
          .content(this.content)
          .cdate(this.cdate)
          .pw(this.pw)
          .vmode(this.vmode != null ? this.vmode : "N")
          .status(0) // 답변 대기
          .isdel("N")
          .isfaq("N")
          .fileyn(this.fileyn)
          .build();
    }

    /** 수정 시 DTO 내용을 기존 엔티티에 반영 */
    public void applyUpdateTo(Qa qa) {
      qa.updateQuestion(this.title, this.content, this.vmode, this.type, this.pw, this.fileyn);
    }
  }

  /**
   * [관리자] 1:1 문의글 답변 작성/수정 요청 DTO
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QARequest {
    /** 답변자 고유 번호 */
    private Long ano;
    /** 답변 내용 */
    private String answer;
    /** 답변 등록 일시 */
    private String adate;

    /** 엔티티 답변 업데이트 수행 */
    public void applyTo(Qa qa) {
      qa.updateAnswer(this.ano, this.answer, Tool.getDate());
    }
  }

  /**
   * FAQ(자주묻는 질문) 등록 / 수정 요청 DTO
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class FaqCRequest {
    /** 관리자 고유 번호 */
    private Long ano;
    /** 등록자 회원 번호 (FK -> MEMBER.NO) */
    private Long mno;
    /** FAQ 질문 제목 */
    private String title;
    /** 문의 유형 (0: 기타, 1: 관제신청, 2: 영상요청, 3: 장비장애) */
    private int type;
    /** FAQ 질문 내용 */
    private String content;
    /** FAQ 답변 */
    private String answer;
    /** 등록 일시 */
    private String cdate;
    /** 게시글 비밀번호 */
    private String pw;
    /** FAQ 정렬 순서 */
    private int vseq;

    /** 자주묻는 질문(FAQ) 여부 */
    @Builder.Default
    private String isfaq = "Y";

    /** 첨부파일 등록 여부 */
    @Builder.Default
    private String fileyn = "N";

    public Qa toEntity() {
      return Qa.builder()
          .ano(this.ano)
          .mno(null)
          .title(this.title)
          .content(this.content)
          .answer(this.answer)
          .cdate(this.cdate)
          .pw(this.pw)
          .vseq(this.vseq)
          .type(this.type)
          .status(2) // 등록과 동시에 답변완료
          .isdel("N")
          .vmode("N")
          .isfaq("Y")
          .fileyn(this.fileyn)
          .build();
    }

    /** 수정 시 DTO 내용을 기존 엔티티에 반영 */
    public void applyUpdateTo(Qa qa) {
      qa.updateFaq(this.title, this.content, this.answer, this.pw, this.vseq, this.type, this.fileyn);
    }
  }

  /**
   * 문의사항 및 FAQ 상세/목록 응답용 DTO
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class QaResponse {
    /** 문의사항 고유 번호 (PK) */
    private Long no;
    /** 문의 회원 번호 (FK -> MEMBER.NO) */
    private Long mno;
    /** 문의 유형 (0: 기타, 1: 관제신청, 2: 영상요청, 3: 장비장애, 4: FAQ) */
    private int type;
    /** 문의 제목 */
    private String title;
    /** 문의 내용 */
    private String content;
    /** 등록 일시 */
    private String cdate;
    /** 답변 상태 (0: 답변대기, 1: 확인중, 2: 답변완료) */
    private int status;
    /** 답변자 고유 번호 */
    private Long ano;
    /** 답변 내용 */
    private String answer;
    /** 답변 등록 일시 */
    private String adate;
    /** 삭제 여부 (Y/N) */
    private String isdel;
    /** 비밀글 여부 (Y/N) */
    private String vmode;
    /** 자주묻는 질문(FAQ) 정렬 순서 */
    private Integer vseq;
    /** 자주묻는 질문(FAQ) 여부 */
    private String isfaq;
    /** 첨부파일 등록 여부 */
    private String fileyn;
    
    /** 이전글 / 다음글 탐색용 DTO */
    private QaNav prev;
    private QaNav next;

    public static QaResponse fromEntity(Qa entity) {
      return fromEntity(entity, null, null);
    }

    public static QaResponse fromEntity(Qa entity, QaNav prev, QaNav next) {
      return QaResponse.builder()
          .no(entity.getNo())
          .mno(entity.getMno())
          .type(entity.getType())
          .title(entity.getTitle())
          .content(entity.getContent())
          .cdate(entity.getCdate())
          .status(entity.getStatus())
          .ano(entity.getAno())
          .answer(entity.getAnswer())
          .adate(entity.getAdate())
          .isdel(entity.getIsdel())
          .vmode(entity.getVmode())
          .vseq(entity.getVseq())
          .isfaq(entity.getIsfaq())
          .fileyn(entity.getFileyn())
          .prev(prev)
          .next(next)
          .build();
    }
  }

  /**
   * 게시글 삭제 요청 DTO
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class DeleteRequest {
    /** 삭제할 글 번호 */
    private Long no;
    /** 사용자가 입력한 비밀번호 */
    private String pw;
    /** 삭제 일시 */
    private String ddate;
  }

  /**
   * [관리자/사용자] 문의사항 및 FAQ 목록 검색 조건 DTO
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class QaSearchRequest {
    /** 검색어 (제목, 내용 등) */
    private String word;

    /** 문의 유형 (null 허용을 위해 Integer 사용) */
    private Integer type;

    /** 답변 상태 (null 허용을 위해 Integer 사용) */
    private Integer status;

    /** 문의 회원 번호 (FK -> MEMBER.NO) */
    private Long mno;

    /** 관리자 번호 (FK -> MANAGER.NO) */
    private Long ano;
  }

  /**
   * 이전글 / 다음글 정보 DTO
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class QaNav {
    private Long no;
    private String title;
    private String fileyn;
    private String vmode;
    private String cdate;
  }
}