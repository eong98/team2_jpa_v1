package dev.jpa.allimio.attach;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachDTO {

  /** 첨부파일 번호 (PK) */
  private Long no;
  /** 메뉴 / 카테고리 번호 */
  private long tno;
  /** 게시판 테이블명 / 폴더명 */
  private String tname = "";
  /** 게시글 번호 */
  private long bno;
  /** 파일 구분 (0: 이미지, 1: 일반 파일) */
  @Builder.Default
  private int type = 1;
  /** 원본 파일명 */
  private String name = "";
  /** 파일 크기 (Byte) */
  private long fsize = 0;
  /** 서버에 실제 저장된 파일명 */
  private String sname = "";
  /** 이미지 미리보기 (썸네일) 파일명 */
  private String thumb = "";
  /** 저장 경로 (상대 경로: /images 또는 /files) */
  private String purl = "";
  /** 등록 날짜 */
  private String cdate = "";

  // 파일 업로드 관련
  // -----------------------------------------------------------------------------------
  /**
  업로드 파일 목록
  <input type='file' class="form-control" name='files' id='files' multiple>
  */
  @Builder.Default
  private List<MultipartFile> files = null;


  /**
   * DTO(Java) -> Entity(DBMS)
   * @return Attach
   */
  public Attach toEntity() {
    return Attach.builder()
        .no(this.no)
        .tno(this.tno)
        .tname(this.tname)
        .bno(this.bno)
        .type(this.type)
        .name(this.name)
        .fsize(this.fsize)
        .sname(this.sname)
        .thumb(this.thumb)
        .purl(this.purl)
        .cdate(this.cdate)
        .build();
  }

  /**
   * Entity(DBMS) -> DTO(Java)
   * @param attach
   * @return AttachDTO
   */
  public static AttachDTO fromEntity(Attach attach) {
    if (attach == null) return null;

    return AttachDTO.builder()
        .no(attach.getNo())
        .tno(attach.getTno())
        .tname(attach.getTname())
        .bno(attach.getBno())
        .type(attach.getType())
        .name(attach.getName())
        .fsize(attach.getFsize())
        .sname(attach.getSname())
        .thumb(attach.getThumb())
        .purl(attach.getPurl())
        .cdate(attach.getCdate())
        .build();
  }

}