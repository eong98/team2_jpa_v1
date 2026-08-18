package dev.jpa.allimio.qa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@ToString
public class Qa {
  /** 문의사항 고유 번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qa_sea_use")
  @SequenceGenerator(name = "qa_sea_use", sequenceName = "QA_SEQ", allocationSize = 1)
  private Long no;

  /** 문의 회원 번호 (FK -> MEMBER.NO) */
  @Column(name = "MNO", nullable = true)
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

  /** 답변 상태 (0: 답변대기, 1: 확인중, 2: 답변완료) */
  @Builder.Default
  private int status = 0;

  /** 비밀글 여부 (Y/N) */
  @Builder.Default
  private String vmode = "N";

  /** 자주묻는 질문(FAQ) 정렬 순서 */
  private Integer vseq;
  
  @Builder.Default
  /** 자주묻는 질문(FAQ) 여부*/
  private String isfaq = "N";
  
  @Builder.Default
  /** 첨부파일 등록 여부*/
  private String fileyn = "N";

  /** 답변자 고유 번호 */
  private Long ano;

  /** 답변 내용 */
  private String answer;

  /** 답변 등록 일시 */
  private String adate;

  /** 삭제 여부 (Y/N) */
  @Builder.Default
  private String isdel = "N";

  /** 삭제 일시 */
  private String ddate;

  public Qa() {

  }

  /**
   * build를 위한 생성자
   * 
   * @param no
   * @param mno
   * @param type
   * @param title
   * @param content
   * @param cdate
   * @param pw
   * @param status
   * @param isdel
   * @param vmode
   * @param vseq
   * @param ano
   * @param answer
   * @param adate
   * @param fileyn
   */
  @Builder
  public Qa(Long no, Long mno, int type, String title, String content, String cdate, String pw, int status,
      String isdel, String vmode, int vseq, String isfaq, Long ano, String answer, String adate, String ddate, String fileyn) {
    this.no = no;
    this.mno = mno;
    this.type = type;
    this.title = title;
    this.content = content;
    this.cdate = cdate;
    this.pw = pw;
    this.status = status;
    this.isdel = isdel;
    this.vmode = vmode;
    this.vseq = vseq;
    this.isfaq = isfaq;
    this.ano = ano;
    this.answer = answer;
    this.adate = adate;
    this.ddate = ddate;
    this.fileyn = fileyn;
  }

  // ==========================================
  // ⭐ 문의글 수정 전용
  // ==========================================
  /**
   * 문의글 작성자가 수정할 때 호출
   */
  public void updateQuestion(String title, String content, String vmode, int type, String pw, String fileyn) {
    this.title = title;
    this.content = content;
    this.vmode = vmode;
    this.type = type;
    this.fileyn = fileyn;
    if (pw != null && !pw.isBlank()) {
      this.pw = pw; // 공백이나 빈값이 아닌 새 비밀번호가 입력되었을 때만 업데이트
    }
  }
  
  // ==========================================
  // ⭐ FAQ 수정 전용
  // ==========================================
  /**
   * FAQ 수정할 때 호출
   */
  public void updateFaq(String title, String content, String answer, String pw, int vseq, int type, String fileyn) {
    this.title = title;
    this.content = content;
    this.answer = answer;
    if (pw != null && !pw.isBlank()) {
      this.pw = pw; // 공백이나 빈값이 아닌 새 비밀번호가 입력되었을 때만 업데이트
    }
    this.vseq = vseq;
    this.type = type;
    this.fileyn = fileyn;
  }

  // ==========================================
  // ⭐ 답변 작성/수정 전용
  // ==========================================
  /**
   * 관리자가 문의글에 답변을 등록하거나 수정할 때 호출
   */
  public void updateAnswer(Long ano, String answer, String adate) {
    this.ano = ano;
    this.answer = answer;
    this.adate = adate;
    this.status = 2; // 답변 작성 시 상태값을 '답변완료(2)'로 자동 변경!
  }
  
  /**
   * 관리자가 회원 문의글을 확인하면 답변상태 확인중으로 변경
   * @param status
   */
  public void setStatus(int status) {
      this.status = status;
  }

  // ==========================================
  // ⭐ 문의글 소프트 삭제 메서드
  // ==========================================
  /**
   * 문의글 소프트 삭제 (삭제 플래그 + 삭제 일시 기록)
   */
  public void delete(String ddate) {
    this.isdel = "Y";
    this.ddate = ddate; // 삭제 시점 기록
  }
  
  /**
   * 비밀번호 일치 여부 검증 메서드
   * @param pw
   * @return
   */
  public boolean matchPw(String pw) {
    return this.pw != null && this.pw.equals(pw);
  }

}
