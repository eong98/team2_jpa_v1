package dev.jpa.allimio.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopService {
  @Autowired
  ShopRepository shopRepository;

  public ShopService() {

  }

  /**
   * 매장 목록 검색 (/user/shop) - mno(로그인 회원) 소유 매장만 대상
   */
  public Page<Shop> search(long mno, String keyword, Pageable pageable) {
    return shopRepository.search(mno, keyword, pageable);
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