package dev.jpa.allimio.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopService {
  @Autowired
  ShopRepository shopRepository;

  public ShopService() {

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
    shop.setPaystate(shopDTO.getPaystate());
    shop.setQrimg(shopDTO.getQrimg());
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