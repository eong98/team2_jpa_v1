package dev.jpa.allimio.notice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString
public class Notice {
  /** 공지사항 고유 번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq_use")
  @SequenceGenerator(name = "notice_seq_use", sequenceName = "NOTICE_SEQ", allocationSize = 1)
  private Long no;

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

  /** 조회수 */
  @Builder.Default
  private int vcnt = 0;

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

  /** 삭제 여부 (Y/N) */
  @Builder.Default
  private String isdel = "N";

  /** 삭제 일시 */
  private String ddate;

  public Notice() {

  }

  /**
   * build를 위한 생성자
   * 
   * @param no
   * @param ano
   * @param type
   * @param title
   * @param content
   * @param cdate
   * @param pw
   * @param vcnt
   * @param fixyn
   * @param fileyn
   * @param vmode
   * @param vseq
   * @param isdel
   * @param ddate
   */
  @Builder
  public Notice(Long no, Long ano, int type, String title, String content, String cdate, String pw, int vcnt,
      String fixyn, String fileyn, String vmode, int vseq, String isdel, String ddate) {
    this.no = no;
    this.ano = ano;
    this.type = type;
    this.title = title;
    this.content = content;
    this.cdate = cdate;
    this.pw = pw;
    this.vcnt = vcnt;
    this.fixyn = fixyn;
    this.fileyn = fileyn;
    this.vmode = vmode;
    this.vseq = vseq;
    this.isdel = isdel;
    this.ddate = ddate;
  }

  // ==========================================
  // ⭐ 수정 전용
  // ==========================================
  /**
   * @param type
   * @param title
   * @param content
   * @param cdate
   * @param pw
   * @param fixyn
   * @param fileyn
   * @param vmode
   * @param vseq
   */
  public void updateNotice(int type, String title, String content, String cdate, String pw, String fixyn, String fileyn,
      String vmode, int vseq) {
    this.type = type;
    this.title = title;
    this.content = content;
    this.cdate = cdate;
    if (pw != null && !pw.isBlank()) {
      this.pw = pw;
    }
    this.fixyn = fixyn;
    this.fileyn = fileyn;
    this.vmode = vmode;
    this.vseq = vseq;
  }

//==========================================
  // ⭐ 상단 고정 상태 변경 전용
  // ==========================================
  /**
   * 목록 또는 관리자 페이지에서 상단고정(Y/N) 상태만 토글할 때 호출
   */
  public void changeTopFix(String fixyn) {
    this.fixyn = fixyn;
  }

//==========================================
// ⭐ 첨부파일 존재 여부 변경 전용
// ==========================================
  /**
   * 파일 첨부/삭제 시 상태 업데이트
   */
  public void updateFileyn(String fileyn) {
    this.fileyn = fileyn;
  }

//==========================================
// ⭐ 조회수 증가 메서드
// ==========================================
  /**
   * 상세 페이지 조회 시 조회수 1 증가
   */
  public void increaseVcnt() {
    this.vcnt += 1;
  }

  // ==========================================
  // ⭐ 소프트 삭제 메서드
  // ==========================================
  /**
   * 소프트 삭제 (삭제 플래그 + 삭제 일시 기록)
   */
  public void delete(String ddate) {
    this.isdel = "Y";
    this.ddate = ddate; // 삭제 시점 기록
  }

  /**
   * 비밀번호 일치 여부 검증 메서드
   * 
   * @param pw
   * @return
   */
  public boolean matchPw(String pw) {
    return this.pw != null && this.pw.equals(pw);
  }

}
