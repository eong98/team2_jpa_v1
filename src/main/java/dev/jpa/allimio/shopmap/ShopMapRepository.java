package dev.jpa.allimio.shopmap;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jpa.allimio.shop.Shop;

/**
 * 매장 도면 Repository
 *
 * SHOPMAP 테이블의 데이터를
 * 조회, 등록, 수정, 삭제할 때 사용합니다.
 */
@Repository
public interface ShopMapRepository extends JpaRepository<ShopMap, Long> {

  /**
   * 매장번호(SNO)로 도면 조회
   *
   * 매장당 도면은 1개만 관리합니다.
   *
   * @param sno 매장번호
   * @return 해당 매장의 도면
   */
  Optional<ShopMap> findBySno(long sno);


  /**
   * 관리자 매장 + 매장 도면 LEFT JOIN 조회
   *
   * 매장명 또는 주소로 매장을 검색하고
   * 해당 매장의 도면 등록 여부를 함께 조회합니다.
   *
   * SHOP을 기준으로 SHOPMAP을 LEFT JOIN하기 때문에
   * 도면이 등록되지 않은 매장도 목록에 표시됩니다.
   *
   * 검색 대상:
   * - 매장명 : SHOP.TITLE
   * - 주소   : SHOP.ADDRESS
   * - 상세주소 : SHOP.ADDRESS2
   *
   * 도면이 없는 경우
   * shopmapno, fname, fsaved, shopmapCdate는 null입니다.
   *
   * JOIN 조건:
   * SHOP.NO = SHOPMAP.SNO
   *
   * @param keyword 매장명 또는 주소 검색어
   * @return 매장 + 도면 정보 목록
   */
  @Query("""
      SELECT new dev.jpa.allimio.shop.Shop(
          s.no,
          s.mno,
          s.title,
          s.address,
          s.address2,
          sm.no,
          sm.fname,
          sm.fsaved,
          sm.cdate
      )
      FROM Shop s
      LEFT JOIN ShopMap sm
          ON s.no = sm.sno
      WHERE (
          :keyword IS NULL
          OR :keyword = ''
          OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(s.address2) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      ORDER BY s.no DESC
      """)
  List<Shop> findShopMapJoin(@Param("keyword") String keyword);

}