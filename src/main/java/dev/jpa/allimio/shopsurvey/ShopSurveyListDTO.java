package dev.jpa.allimio.shopsurvey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 매장 설문 목록 DTO
 *
 * ShopSurveyRepository.findListBySno()의 JPQL 생성자 조회 결과를 담습니다.
 * 생성자 파라미터 순서가 JPQL SELECT 순서와 같아야 합니다.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ShopSurveyListDTO {

  /** 설문번호 */
  private Long no;

  /** 설문제목 */
  private String title;

  /** 설문설명 */
  private String description;

  /** 상태 DRAFT/OPEN/CLOSED */
  private String status;

  /** QR코드 접속용 UUID */
  private String qrid;

  /** 등록일 */
  private String cdate;

  /** 수정일 */
  private String udate;

  /** 문항 수 (DRAFT는 문항이 JSON에만 있어서 0) */
  private Long questionCount;

  /** 응답 수 */
  private Long responseCount;
}
