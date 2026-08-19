package dev.jpa.allimio.shopmap;

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
 * 매장 도면 Entity
 *
 * SHOPMAP 테이블과 연결됩니다.
 * 매장번호(sno)는 외래키로 사용하지 않고
 * 일반 숫자 값으로 저장합니다.
 */
@Entity
@Table(name = "SHOPMAP")
@Getter
@Setter
@ToString
public class ShopMap {

  /**
   * 매장 도면 번호(PK)
   */
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "shopmap_seq_use"
  )
  @SequenceGenerator(
      name = "shopmap_seq_use",
      sequenceName = "SEQ_SHOPMAP_NO",
      allocationSize = 1
  )
  private long no;

  /**
   * 매장번호
   */
  private long sno;

  /**
   * 원본 파일명
   */
  private String fname;

  /**
   * 서버 저장 파일명
   */
  private String fsaved;

  /**
   * 등록일
   */
  private String cdate;

  /**
   * 기본 생성자
   */
  public ShopMap() {
  }

  /**
   * 전체 필드 생성자
   */
  public ShopMap(long no, long sno,
                 String fname, String fsaved, String cdate) {

    this.no = no;
    this.sno = sno;
    this.fname = fname;
    this.fsaved = fsaved;
    this.cdate = cdate;
  }
}