package dev.jpa.allimio.cctv;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class CctvService {
  @Autowired
  CctvRepository cctvRepository;

  public CctvService() {

  }

  public Cctv save(CctvDTO cctvDTO) {
    cctvDTO.setCdate(Tool.getDate());
    Cctv cctv = cctvRepository.save(cctvDTO.toEntity());

    return cctv;
  }

  /**
   * 매장 CCTV 목록 검색 (/user/shop/cctv) - sno(입장한 매장) 소유 CCTV만 대상
   */
  public Page<Cctv> search(long sno, String keyword, Pageable pageable) {
    return cctvRepository.search(sno, keyword, pageable);
  }

  /**
   * 관리자 CCTV 목록 검색 (/dbms/cctv) - sno 상관없이 전체 CCTV 대상.
   * sno가 null이면 전체, 값이 있으면 해당 매장 소유 CCTV만.
   */
  public Page<Cctv> searchAdmin(Long sno, Integer state, String keyword, Pageable pageable) {
    return cctvRepository.searchAdmin(sno, state, keyword, pageable);
  }

  public List<Cctv> findAll() {
    List<Cctv> list = cctvRepository.findAll();

    return list;
  }

  public Cctv findById(long pk) {
    Optional<Cctv> optional = cctvRepository.findById(pk);

    if (optional.isPresent()) {
      Cctv cctv = optional.get();
      return cctv;
    }
    return null;
  }

  public Cctv update(CctvDTO cctvDTO) {
    Cctv cctv = cctvRepository.findById(cctvDTO.getNo()).get();
    cctv.setSno(cctvDTO.getSno());
    cctv.setMac(cctvDTO.getMac());
    cctv.setRepresent(cctvDTO.getRepresent());
    cctv.setCname(cctvDTO.getCname());
    cctv.setCkdate(cctvDTO.getCkdate());
    cctv.setState(cctvDTO.getState());

    Cctv savedEntity = cctvRepository.save(cctv);

    return savedEntity;
  }

  public boolean deleteById(long pk) {
    Optional<Cctv> optional = cctvRepository.findById(pk);

    if (optional.isPresent()) {
      cctvRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}