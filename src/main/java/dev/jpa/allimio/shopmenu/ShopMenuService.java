package dev.jpa.allimio.shopmenu;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopMenuService {
  @Autowired
  ShopMenuRepository shopMenuRepository;

  public ShopMenuService() {

  }

  /**
   * 등록
   * @param shopMenuDTO
   * @return
   */
  public ShopMenu save(ShopMenuDTO shopMenuDTO) {
    shopMenuDTO.setCdate(Tool.getDate());
    ShopMenu shopMenu = shopMenuRepository.save(shopMenuDTO.toEntity());

    return shopMenu;
  }

  public List<ShopMenu> findAll() {
    List<ShopMenu> list = shopMenuRepository.findAll();

    return list;
  }

  public ShopMenu findById(long pk) {
    Optional<ShopMenu> optional = shopMenuRepository.findById(pk);

    if (optional.isPresent()) {
      ShopMenu shopMenu = optional.get();
      return shopMenu;
    }
    return null;
  }

  /**
   * 상위메뉴번호(fkno) 기준 하위메뉴 목록, 최상위메뉴는 fkno=null로 조회
   * @param fkno
   * @return
   */
  public List<ShopMenu> findByFkno(Long fkno) {
    List<ShopMenu> list = shopMenuRepository.findByFknoOrderByOrdAsc(fkno);

    return list;
  }

  /**
   * 조회 + 수정 처리
   * @param shopMenuDTO
   * @return
   */
  public ShopMenu update(ShopMenuDTO shopMenuDTO) {
    ShopMenu shopMenu = shopMenuRepository.findById(shopMenuDTO.getNo()).get();
    shopMenu.setFkno(shopMenuDTO.getFkno());
    shopMenu.setDept(shopMenuDTO.getDept());
    shopMenu.setOrd(shopMenuDTO.getOrd());
    shopMenu.setTitle(shopMenuDTO.getTitle());
    shopMenu.setPurl(shopMenuDTO.getPurl());
    shopMenu.setTname(shopMenuDTO.getTname());
    shopMenu.setMname(shopMenuDTO.getMname());
    shopMenu.setUseYn(shopMenuDTO.getUseYn());

    ShopMenu savedEntity = shopMenuRepository.save(shopMenu);

    return savedEntity;
  }

  /**
   * 조회 + 삭제 처리
   * @param pk
   * @return
   */
  public boolean deleteById(long pk) {
    Optional<ShopMenu> optional = shopMenuRepository.findById(pk);

    if (optional.isPresent()) {
      shopMenuRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}
