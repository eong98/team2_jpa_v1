package dev.jpa.allimio.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.cctv.CctvRepository;
import dev.jpa.allimio.shopmember.ShopMemberRepository;
import dev.jpa.allimio.tool.Tool;


@Service
public class ShopService {
  @Autowired
  ShopRepository shopRepository;

  @Autowired
  CctvRepository cctvRepository;

  @Autowired
  ShopMemberRepository shopMemberRepository;

  public ShopService() {

  }

  /**
   * 매장 목록 검색 (/user/shop) - mno(로그인 회원) 소유 매장만 대상.
   * grade를 모르는 옛 호출부와의 하위 호환을 위해 남겨둠 (내부적으로 searchForUser 위임).
   */
  public Page<Shop> search(long mno, String keyword, Pageable pageable) {
    return shopRepository.search(mno, keyword, pageable);
  }

  /**
   * 로그인 회원 기준 매장 목록 검색 (/user/shop, 사이드바 매장선택 등에서 공용으로 사용).
   *
   * - grade == 10 (점주): SHOP.MNO가 로그인 회원번호(mno)와 같은 매장, 즉 본인 소유 매장만 조회.
   * - grade 6~9 (일반 직원): 매장을 소유하지 않으므로, SHOP_MEMBER 테이블에서 로그인
   *   회원번호(mno)로 배정된 매장번호(SNO) 목록을 먼저 조회한 뒤 그 매장들만 조회.
   *   배정된 매장이 하나도 없으면 빈 페이지를 반환.
   * - 그 외(grade가 없거나 관리자 등): 기존 방식과 동일하게 SHOP.MNO 기준(하위 호환).
   *
   * @param mno   로그인 회원번호
   * @param grade 로그인 회원 등급 (10: 점주, 6~9: 직원). null이면 기존 방식으로 동작.
   * @param keyword 매장명/주소/상세주소 포함 검색
   * @param pageable 페이지 번호/사이즈/정렬
   */
  public Page<Shop> searchForUser(long mno, Integer grade, String keyword, Pageable pageable) {
    boolean isStaff = grade != null && grade >= 6 && grade <= 9;

    if (isStaff) {
      List<Long> snoList = shopMemberRepository.findSnoListByMno(mno);

      if (snoList.isEmpty()) {
        return Page.empty(pageable);
      }

      return shopRepository.searchByShopNos(snoList, keyword, pageable);
    }

    // grade == 10(점주) 또는 grade 미전달(하위 호환) : SHOP.MNO(소유) 기준
    return shopRepository.search(mno, keyword, pageable);
  }

  /**
   * 매장 목록에 CCTV 등록 대수(cctvCount)를 붙여서 반환 (/user/shop 카드에 표시용).
   * 매장(sno)마다 CCTV 테이블에서 COUNT(SNO)로 계산합니다.
   */
  public List<ShopWithCctvCount> attachCctvCount(List<Shop> shops) {
    List<ShopWithCctvCount> list = new ArrayList<>();

    for (Shop shop : shops) {
      long cctvCount = cctvRepository.countBySno(shop.getNo());
      list.add(new ShopWithCctvCount(shop, cctvCount));
    }

    return list;
  }

  /**
   * 관리자 매장 목록 검색 (/dbms/shop) - mno 상관없이 전체 매장 대상.
   * mno가 null이면 전체, 값이 있으면 해당 회원 소유 매장만.
   */
  public Page<Shop> searchAdmin(Long mno, String keyword, Pageable pageable) {
    return shopRepository.searchAdmin(mno, keyword, pageable);
  }
  
  
  
  /**
   * 등록
   * @param shopDTO
   * @return
   */
  public Shop save(ShopDTO shopDTO) {
    shopDTO.setCdate(Tool.getDate());
    Shop shop = shopRepository.save(shopDTO.toEntity());

    return shop;
  }

  public List<Shop> findAll() {
    List<Shop> list = shopRepository.findAll();

    return list;
  }

  public Shop findById(long pk) {
    Optional<Shop> optional = shopRepository.findById(pk);

    if (optional.isPresent()) {
      Shop shop = optional.get();
      return shop;
    }
    return null;
  }

  /**
   * 조회 + 수정 처리
   * @param shopDTO
   * @return
   */
  public Shop update(ShopDTO shopDTO) {
    Shop shop = shopRepository.findById(shopDTO.getNo()).get();
    shop.setMno(shopDTO.getMno());
    shop.setTitle(shopDTO.getTitle());
    shop.setZip(shopDTO.getZip());
    shop.setAddress(shopDTO.getAddress());
    shop.setAddress2(shopDTO.getAddress2());
    shop.setTel(shopDTO.getTel());
    shop.setComent(shopDTO.getComent());
    shop.setPhone(shopDTO.getPhone());
    shop.setSnum(shopDTO.getSnum());
    shop.setUdate(Tool.getDate());

    Shop savedEntity = shopRepository.save(shop);

    return savedEntity;
  }

  /**
   * 조회 + 삭제 처리
   * @param pk
   * @return
   */
  public boolean deleteById(long pk) {
    Optional<Shop> optional = shopRepository.findById(pk);

    if (optional.isPresent()) {
      shopRepository.deleteById(pk);
      return true;
    }
    return false;
  }
  

}