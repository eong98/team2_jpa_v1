package dev.jpa.allimio.shopmember;

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

/**
 * 소속매장(직원-매장 배정) Entity
 *
 * SHOP_MEMBER 테이블과 연결됩니다.
 * 점주(grade == 10)는 SHOP.MNO로 본인 소유 매장을 바로 찾지만, 일반 직원
 * (grade 6~9)은 매장을 소유하지 않으므로 이 테이블로 "어느 매장 소속인지"를
 * 매핑합니다.
 *
 * - SNO: 매장번호 (FK -> SHOP.NO)
 * - MNO: 회원번호/직원번호 (FK -> MEMBER.NO)
 *
 * 로그인한 직원의 매장선택 목록(사이드바 매장 스위처, /user/shop 목록 등)은
 * 이 테이블에서 MNO로 SNO 목록을 조회해 구성합니다.
 */
@Entity
@Table(name = "SHOP_MEMBER")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopMember {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_member_seq_use")
  @SequenceGenerator(name = "shop_member_seq_use", sequenceName = "SHOP_MEMBER_SEQ", allocationSize = 1)
  private Long no;

  /** 매장번호 (FK -> SHOP.NO) */
  private Long sno;

  /** 회원번호 / 직원번호 (FK -> MEMBER.NO) */
  private Long mno;

  /** 등록일시 */
  private String cdate;
}
