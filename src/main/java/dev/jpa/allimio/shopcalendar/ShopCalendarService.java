package dev.jpa.allimio.shopcalendar;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopCalendarService {
  @Autowired
  ShopCalendarRepository shopCalendarRepository;

  public ShopCalendarService() {

  }

  /**
   * 등록
   * @param shopCalendarDTO
   * @return
   */
  public ShopCalendar save(ShopCalendarDTO shopCalendarDTO) {
    shopCalendarDTO.setCdate(Tool.getDate());
    ShopCalendar shopCalendar = shopCalendarRepository.save(shopCalendarDTO.toEntity());

    return shopCalendar;
  }

  public List<ShopCalendar> findAll() {
    List<ShopCalendar> list = shopCalendarRepository.findAll();

    return list;
  }

  public ShopCalendar findById(long pk) {
    Optional<ShopCalendar> optional = shopCalendarRepository.findById(pk);

    if (optional.isPresent()) {
      ShopCalendar shopCalendar = optional.get();
      return shopCalendar;
    }
    return null;
  }

  /**
   * 매장번호(sno) 기준 캘린더 목록
   * @param sno
   * @return
   */
  public List<ShopCalendar> findBySno(long sno) {
    List<ShopCalendar> list = shopCalendarRepository.findBySno(sno);

    return list;
  }

  /**
   * 조회 + 수정 처리
   * @param shopCalendarDTO
   * @return
   */
  public ShopCalendar update(ShopCalendarDTO shopCalendarDTO) {
    ShopCalendar shopCalendar = shopCalendarRepository.findById(shopCalendarDTO.getNo()).get();
    shopCalendar.setSno(shopCalendarDTO.getSno());
    shopCalendar.setCtype(shopCalendarDTO.getCtype());
    shopCalendar.setTitle(shopCalendarDTO.getTitle());
    shopCalendar.setContents(shopCalendarDTO.getContents());
    shopCalendar.setSdate(shopCalendarDTO.getSdate());
    shopCalendar.setEdate(shopCalendarDTO.getEdate());
    shopCalendar.setAllday(shopCalendarDTO.getAllday());
    shopCalendar.setColor(shopCalendarDTO.getColor());
    shopCalendar.setStatus(shopCalendarDTO.getStatus());
    shopCalendar.setUdate(Tool.getDate());

    ShopCalendar savedEntity = shopCalendarRepository.save(shopCalendar);

    return savedEntity;
  }

  /**
   * 조회 + 삭제 처리
   * @param pk
   * @return
   */
  public boolean deleteById(long pk) {
    Optional<ShopCalendar> optional = shopCalendarRepository.findById(pk);

    if (optional.isPresent()) {
      shopCalendarRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}
