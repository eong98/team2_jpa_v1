package dev.jpa.allimio.shopsurvey;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jpa.allimio.shop.Shop;

/**
 * 매장 설문조사 Repository
 *
 * 점주 권한 확인은 SHOP_SURVEY → SHOP JOIN 후 SHOP.MNO로 합니다.
 */
@Repository
public interface ShopSurveyRepository extends JpaRepository<ShopSurvey, Long> {

  /**
   * 로그인 점주 소유 매장인지 확인하며 매장 조회
   *
   * @param sno 매장번호
   * @param mno 로그인 회원번호
   */
  @Query("SELECT sh FROM Shop sh WHERE sh.no = :sno AND sh.mno = :mno")
  Optional<Shop> findOwnedShop(@Param("sno") Long sno, @Param("mno") Long mno);

  /**
   * 로그인 점주 소유 설문 단건 조회 (삭제 제외)
   * SHOP을 JOIN FETCH 해서 소유 여부를 한 번에 확인합니다.
   */
  @Query("SELECT s FROM ShopSurvey s " +
         "JOIN FETCH s.shop sh " +
         "WHERE s.no = :svno AND sh.mno = :mno AND s.status <> 'DELETE'")
  Optional<ShopSurvey> findOwned(@Param("svno") Long svno, @Param("mno") Long mno);

  /**
   * QR 토큰으로 설문 조회 (고객 화면용, 매장명 표시를 위해 SHOP JOIN FETCH)
   */
  @Query("SELECT s FROM ShopSurvey s " +
         "JOIN FETCH s.shop sh " +
         "WHERE s.qrid = :qrid")
  Optional<ShopSurvey> findByQridWithShop(@Param("qrid") String qrid);

  /** QR 토큰 중복 검사 */
  boolean existsByQrid(String qrid);

  /**
   * 매장별 설문 목록 (삭제 제외, 문항 수/응답 수 포함, 페이징)
   *
   * @param sno    매장번호
   * @param mno    로그인 회원번호 (점주 확인)
   * @param status 상태 필터 (null 또는 ''이면 전체)
   */
  @Query(value = "SELECT new dev.jpa.allimio.shopsurvey.ShopSurveyListDTO(" +
                 "  s.no, s.title, s.description, s.status, s.qrid, s.cdate, s.udate, " +
                 "  (SELECT COUNT(q) FROM ShopSurveyQuestion q JOIN q.survey qs WHERE qs.no = s.no), " +
                 "  (SELECT COUNT(r) FROM ShopSurveyResponse r JOIN r.survey rs WHERE rs.no = s.no), " +
                 "  s.aiyn) " +
                 "FROM ShopSurvey s " +
                 "JOIN s.shop sh " +
                 "WHERE sh.no = :sno AND sh.mno = :mno " +
                 "AND s.status <> 'DELETE' " +
                 "AND (:status IS NULL OR :status = '' OR s.status = :status) " +
                 "ORDER BY s.no DESC",
         countQuery = "SELECT COUNT(s) FROM ShopSurvey s " +
                      "JOIN s.shop sh " +
                      "WHERE sh.no = :sno AND sh.mno = :mno " +
                      "AND s.status <> 'DELETE' " +
                      "AND (:status IS NULL OR :status = '' OR s.status = :status)")
  Page<ShopSurveyListDTO> findListBySno(
      @Param("sno") Long sno,
      @Param("mno") Long mno,
      @Param("status") String status,
      Pageable pageable);
}
