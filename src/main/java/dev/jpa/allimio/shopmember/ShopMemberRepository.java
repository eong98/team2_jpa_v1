package dev.jpa.allimio.shopmember;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 소속매장(직원-매장 배정) Repository
 *
 * SHOP_MEMBER 테이블 조회 전용입니다.
 */
@Repository
public interface ShopMemberRepository extends JpaRepository<ShopMember, Long> {

  /**
   * 회원번호(직원번호)로 배정된 매장번호(SNO) 목록을 조회합니다.
   *
   * 로그인 회원이 일반 직원(grade 6~9)일 때, 매장선택(사이드바 매장 스위처,
   * /user/shop 목록 등)에서 "내가 소속된 매장" 목록을 만드는 데 사용합니다.
   *
   * @param mno 회원번호(직원번호)
   * @return 배정된 매장번호 목록
   */
  @Query("SELECT sm.sno FROM ShopMember sm WHERE sm.mno = :mno")
  List<Long> findSnoListByMno(@Param("mno") long mno);

  /** 매장번호 기준 소속 직원 목록 조회 (매장 관리 화면에서 직원 배정 현황 확인용) */
  List<ShopMember> findBySno(long sno);

  /** 회원번호 기준 소속 매장 배정 목록 조회 */
  List<ShopMember> findByMno(long mno);

  /** 이미 같은 매장에 배정되어 있는지 여부 (중복 배정 방지용) */
  boolean existsBySnoAndMno(long sno, long mno);
}
