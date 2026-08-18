package dev.jpa.allimio.shopmap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 도면 DTO
 *
 * Controller와 Service 사이에서
 * 매장 도면 데이터를 전달할 때 사용합니다.
 */
@Getter
@Setter
@ToString
public class ShopMapDTO {

  /**
   * 매장 도면 번호(PK)
   */
  private long no;

  /**
   * 매장번호
   *
   * 외래키로 연결하지 않고
   * 매장번호 값만 저장합니다.
   */
  private long sno;

  /**
   * 사용자가 업로드한 원본 파일명
   */
  private String fname;

  /**
   * 서버에 실제 저장된 파일명
   */
  private String fsaved;

  /**
   * 도면 등록일
   */
  private String cdate;


  /**
   * 기본 생성자
   */
  public ShopMapDTO() {

  }


  /**
   * 전체 필드를 받는 생성자
   */
  public ShopMapDTO(long no, long sno,
                    String fname, String fsaved, String cdate) {

    this.no = no;
    this.sno = sno;
    this.fname = fname;
    this.fsaved = fsaved;
    this.cdate = cdate;
  }


  /**
   * DTO 데이터를 ShopMap Entity로 변환합니다.
   *
   * DB에 매장 도면 정보를 저장할 때 사용합니다.
   */
  public ShopMap toEntity() {

    return new ShopMap(
        no,
        sno,
        fname,
        fsaved,
        cdate
    );
  }

}