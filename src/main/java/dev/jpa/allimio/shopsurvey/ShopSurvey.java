package dev.jpa.allimio.shopsurvey;

import dev.jpa.allimio.shop.Shop;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문조사 Entity
 *
 * SHOP_SURVEY 테이블과 연결됩니다.
 * 기존 SURVEY 테이블(survey 패키지)과는 별개의 기능입니다.
 *
 * - STATUS: DRAFT(작성중) / OPEN(진행중) / CLOSED(종료) / DELETE(삭제)
 * - DRAFT: 임시저장 폼 JSON. 게시(OPEN) 후 NULL
 * - QRID: 고객 접속용 UUID. QR코드 URL(/survey/{qrid})에 사용
 */
@Entity
@Table(name = "SHOP_SURVEY")
@Getter
@Setter
@ToString(exclude = "shop") // 순환참조방지
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSurvey {

  /** 설문번호 (PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_survey_seq_use")
  @SequenceGenerator(name = "shop_survey_seq_use", sequenceName = "SHOP_SURVEY_SEQ", allocationSize = 1)
  private Long no;

  /** 매장 (FK -> SHOP.NO) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sno")
  private Shop shop;

  /** 설문제목 */
  private String title;

  /** 설문설명 */
  private String description;

  /** 상태 DRAFT/OPEN/CLOSED/DELETE */
  private String status;

  /** QR코드 접속용 UUID */
  private String qrid;

  /** 수정일 */
  private String udate;

  /** 등록일 */
  private String cdate;

  /** 임시저장 JSON */
  @Lob
  private String draft;
}
