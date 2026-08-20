package dev.jpa.allimio.shopmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

/**
 * 매장 도면 Service
 *
 * 매장 도면의 조회, 등록, 수정, 삭제 기능을 처리합니다.
 */
@Service
public class ShopMapService {

  @Autowired
  private ShopMapRepository shopMapRepository;


  /**
   * 전체 매장 도면 목록을 조회합니다.
   *
   * 관리자 화면에서 전체 도면을 조회할 때 사용합니다.
   *
   * @return 전체 매장 도면 목록
   */
  public List<ShopMapDTO> list() {

    List<ShopMap> list = shopMapRepository.findAll();
    List<ShopMapDTO> dtoList = new ArrayList<>();

    for (ShopMap shopMap : list) {

      ShopMapDTO dto = new ShopMapDTO(
          shopMap.getNo(),
          shopMap.getSno(),
          shopMap.getFname(),
          shopMap.getFsaved(),
          shopMap.getCdate()
      );

      dtoList.add(dto);
    }

    return dtoList;
  }


  /**
   * 매장번호로 도면을 조회합니다.
   *
   * 회원이 본인의 매장을 선택했을 때
   * 해당 매장의 도면을 조회하기 위해 사용합니다.
   *
   * @param sno 매장번호
   * @return 매장 도면 정보
   */
  public ShopMapDTO readByNo(long sno) {

    Optional<ShopMap> optional = shopMapRepository.findByNo(sno);

    if (optional.isEmpty()) {
      return null;
    }

    ShopMap shopMap = optional.get();

    return new ShopMapDTO(
        shopMap.getNo(),
        shopMap.getSno(),
        shopMap.getFname(),
        shopMap.getFsaved(),
        shopMap.getCdate()
    );
  }


  /**
   * 새로운 매장 도면을 등록합니다.
   *
   * 매장당 도면은 1개만 등록할 수 있습니다.
   * 이미 등록된 도면이 있으면 등록하지 않습니다.
   *
   * @param shopMapDTO 등록할 도면 정보
   * @return 등록 성공 여부
   */
  public boolean create(ShopMapDTO shopMapDTO) {

    // 해당 매장에 이미 도면이 등록되어 있는지 확인
    Optional<ShopMap> existing = shopMapRepository.findByNo(shopMapDTO.getSno());

    if (existing.isPresent()) {
      return false;
    }

    // 등록일 저장
    shopMapDTO.setCdate(Tool.getDate());

    // DTO를 Entity로 변환 후 저장
    shopMapRepository.save(shopMapDTO.toEntity());

    return true;
  }


  /**
   * 기존 매장 도면 정보를 수정합니다.
   *
   * 실제 파일 교체 처리는 Controller에서 진행하고,
   * 여기서는 변경된 파일 정보를 DB에 저장합니다.
   *
   * @param no 매장 도면 번호
   * @param shopMapDTO 수정할 도면 정보
   * @return 수정 성공 여부
   */
  public boolean update(long no, ShopMapDTO shopMapDTO) {

    Optional<ShopMap> optional = shopMapRepository.findById(no);

    if (optional.isEmpty()) {
      return false;
    }

    ShopMap shopMap = optional.get();

    // 변경 가능한 값만 수정
    shopMap.setFname(shopMapDTO.getFname());
    shopMap.setFsaved(shopMapDTO.getFsaved());

    shopMapRepository.save(shopMap);

    return true;
  }


  /**
   * 매장 도면을 삭제합니다.
   *
   * 실제 서버에 저장된 파일 삭제는
   * Controller에서 별도로 처리합니다.
   *
   * @param no 매장 도면 번호
   * @return 삭제 성공 여부
   */
  public boolean delete(long no) {

    Optional<ShopMap> optional = shopMapRepository.findById(no);

    if (optional.isEmpty()) {
      return false;
    }

    shopMapRepository.deleteById(no);

    return true;
  }
  
  /**
   * 도면번호로 매장 도면을 조회합니다.
   *
   * 수정하거나 삭제할 때
   * 기존 파일명을 확인하기 위해 사용합니다.
   *
   * @param no 매장 도면 번호
   * @return 매장 도면 정보
   */
  public ShopMapDTO read(long no) {

    Optional<ShopMap> optional = shopMapRepository.findById(no);

    if (optional.isEmpty()) {
      return null;
    }

    ShopMap shopMap = optional.get();

    return new ShopMapDTO(
        shopMap.getNo(),
        shopMap.getSno(),
        shopMap.getFname(),
        shopMap.getFsaved(),
        shopMap.getCdate()
    );
  }

}