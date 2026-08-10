package dev.jpa.allimio.inmenu;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class InMenuService {
  @Autowired
  InMenuRepository inMenuRepository;

  public InMenuService() {

  }

  /**
   * 등록
   * @param inMenuDTO
   * @return
   */
  public InMenu save(InMenuDTO inMenuDTO) {
    inMenuDTO.setCdate(Tool.getDate());
    InMenu inMenu = inMenuRepository.save(inMenuDTO.toEntity());

    return inMenu;
  }

  public List<InMenu> findAll() {
    List<InMenu> list = inMenuRepository.findAll();

    return list;
  }

  public InMenu findById(long pk) {
    Optional<InMenu> optional = inMenuRepository.findById(pk);

    if (optional.isPresent()) {
      InMenu inMenu = optional.get();
      return inMenu;
    }
    return null;
  }

  /**
   * 상위메뉴번호(fkno) 기준 하위메뉴 목록, 최상위메뉴는 fkno=null로 조회
   * @param fkno
   * @return
   */
  public List<InMenu> findByFkno(Long fkno) {
    List<InMenu> list = inMenuRepository.findByFknoOrderByOrdAsc(fkno);

    return list;
  }

  /**
   * 조회 + 수정 처리
   * @param inMenuDTO
   * @return
   */
  public InMenu update(InMenuDTO inMenuDTO) {
    InMenu inMenu = inMenuRepository.findById(inMenuDTO.getNo()).get();
    inMenu.setFkno(inMenuDTO.getFkno());
    inMenu.setDept(inMenuDTO.getDept());
    inMenu.setOrd(inMenuDTO.getOrd());
    inMenu.setTitle(inMenuDTO.getTitle());
    inMenu.setPurl(inMenuDTO.getPurl());
    inMenu.setTname(inMenuDTO.getTname());
    inMenu.setMname(inMenuDTO.getMname());
    inMenu.setUseYn(inMenuDTO.getUseYn());

    InMenu savedEntity = inMenuRepository.save(inMenu);

    return savedEntity;
  }

  /**
   * 조회 + 삭제 처리
   * @param pk
   * @return
   */
  public boolean deleteById(long pk) {
    Optional<InMenu> optional = inMenuRepository.findById(pk);

    if (optional.isPresent()) {
      inMenuRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}
