package dev.jpa.allimio.aiissuemap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 이슈 도면 Entity
 *
 * AIISSUEMAP 테이블과 연결됩니다.
 */
@Entity
@Table(name = "AIISSUEMAP")
@Getter
@Setter
@ToString
public class AiIssueMap {

  /**
   * AI 이슈 도면 번호(PK)
   */
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "aiissuemap_seq_use"
  )
  @SequenceGenerator(
      name = "aiissuemap_seq_use",
      sequenceName = "SEQ_AIISSUEMAP_NO",
      allocationSize = 1
  )
  private long aiissueMapno;

  /**
   * 회원번호
   */
  private long mno;

  /**
   * 원본 매장 도면 번호
   */
  private long shopmapno;

  /**
   * 이슈 X 좌표
   */
  private Double xpos;

  /**
   * 이슈 Y 좌표
   */
  private Double ypos;

  /**
   * 이슈 표시 색상
   */
  private String color;

  /**
   * AI 생성 이미지 저장 파일명
   */
  private String fsaved;

  /**
   * 이미지 생성 상태
   *
   * 0 : 생성 대기
   * 1 : 생성 완료
   * 2 : 생성 실패
   */
  private int status;

  /**
   * 이미지 생성 오류 내용
   */
  private String err;

  /**
   * 등록일
   */
  private String cdate;

  public AiIssueMap() {
  }

  public AiIssueMap(
      long aiissueMapno,
      long mno,
      long shopmapno,
      Double xpos,
      Double ypos,
      String color,
      String fsaved,
      int status,
      String err,
      String cdate) {

    this.aiissueMapno = aiissueMapno;
    this.mno = mno;
    this.shopmapno = shopmapno;
    this.xpos = xpos;
    this.ypos = ypos;
    this.color = color;
    this.fsaved = fsaved;
    this.status = status;
    this.err = err;
    this.cdate = cdate;
  }
}