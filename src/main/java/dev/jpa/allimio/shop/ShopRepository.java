package dev.jpa.allimio.shop;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, Long> {

  /**
   * 매장 목록 검색용 (/user/shop). 로그인한 회원(mno) 소유 매장만 대상으로 함.
   * keyword는 선택 사항(null이면 조건 무시).
   * @param mno 회원번호 (필수, 세션의 로그인 회원 기준)
   * @param keyword 매장명(title)/주소(address)/상세주소(address2) 포함 검색
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT s FROM Shop s
      WHERE s.mno = :mno
        AND (:keyword IS NULL
             OR s.title LIKE CONCAT('%', :keyword, '%')
             OR s.address LIKE CONCAT('%', :keyword, '%')
             OR s.address2 LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<Shop> search(
      @Param("mno") long mno,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  /**
   * 관리자 매장 목록 검색용 (/dbms/shop). mno 상관없이 전체 매장 대상.
   * mno를 넘기면 해당 회원 소유 매장만, 안 넘기면(null) 전체 조회.
   * keyword는 선택 사항(null이면 조건 무시).
   * @param mno 회원번호 (선택 사항, 특정 회원 소유 매장만 보고 싶을 때만 사용)
   * @param keyword 매장명(title)/주소(address)/상세주소(address2) 포함 검색
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT s FROM Shop s
      WHERE (:mno IS NULL OR s.mno = :mno)
        AND (:keyword IS NULL
             OR s.title LIKE CONCAT('%', :keyword, '%')
             OR s.address LIKE CONCAT('%', :keyword, '%')
             OR s.address2 LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<Shop> searchAdmin(
      @Param("mno") Long mno,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  /**
   * 매장번호(SNO) 목록 기준 검색용 (/user/shop, 직원(grade 6~9) 로그인).
   * 점주처럼 SHOP.MNO로 바로 못 찾고, SHOP_MEMBER에서 조회해온 소속 매장번호
   * 목록(snoList)으로 걸러야 하는 경우에 사용합니다. (ShopService.searchForUser)
   * keyword는 선택 사항(null이면 조건 무시).
   * @param snoList SHOP_MEMBER에서 로그인 회원(mno)로 조회한 소속 매장번호 목록
   * @param keyword 매장명(title)/주소(address)/상세주소(address2) 포함 검색
   * @param pageable 페이지 번호/사이즈/정렬
   */
  @Query("""
      SELECT s FROM Shop s
      WHERE s.no IN :snoList
        AND (:keyword IS NULL
             OR s.title LIKE CONCAT('%', :keyword, '%')
             OR s.address LIKE CONCAT('%', :keyword, '%')
             OR s.address2 LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<Shop> searchByShopNos(
      @Param("snoList") List<Long> snoList,
      @Param("keyword") String keyword,
      Pageable pageable
  );

}