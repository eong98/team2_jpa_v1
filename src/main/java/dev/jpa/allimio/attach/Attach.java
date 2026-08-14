package dev.jpa.allimio.attach;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Attach {
  /** 첨부파일 번호 (PK)   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attach_seq_use")
  @SequenceGenerator(name = "attach_seq_use", sequenceName = "ATTACH_SEQ", allocationSize = 1)
  private Long no;

  /*** 등록 관리자메뉴/테이블 번호 (FK)   */
  private Long tno;
  
  /*** 등록 테이블이름 (폴더명)   */
  private String tname;

  /*** 등록 게시글 PK 번호   */
  private Long bno;

  /*** 파일 종류 구분 (0: IMAGE, 1: FILE)   */
  private Integer type;

  /*** 원본 파일명   */
  private String name;

  /*** 파일 크기 (BYTE)   */
  @Builder.Default
  private Long fsize = 0L;

  /*** 서버 저장 파일명   */
  private String sname;
  
  /** 이미지 썸네일 파일명 */
  private String thumb;
  
  /*** 상대 저장 경로   */
  private String purl;

  /*** 등록일시 (yyyy-MM-dd HH:mm:ss)   */
  private String cdate;
}