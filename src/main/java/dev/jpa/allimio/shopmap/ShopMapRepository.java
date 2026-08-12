package dev.jpa.allimio.shopmap;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 매장 도면 Repository
 *
 * SHOPMAP 테이블의 데이터를
 * 조회, 등록, 수정, 삭제할 때 사용합니다.
 */
@Repository
public interface ShopMapRepository extends JpaRepository<ShopMap, Long> {

  /**
   * 매장번호로 도면을 조회합니다.
   *
   * 매장당 도면은 1개만 관리하기 때문에
   * 하나의 ShopMap을 반환합니다.
   *
   * @param no 매장번호
   * @return 해당 매장의 도면
   */
  Optional<ShopMap> findByNo(long no);

}