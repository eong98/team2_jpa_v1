package dev.jpa.allimio.cctvvisitor;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class CctvVisitorService {
  @Autowired
  CctvVisitorRepository cctvVisitorRepository;

  public CctvVisitorService() {

  }

  public CctvVisitor save(CctvVisitorDTO cctvVisitorDTO) {
    cctvVisitorDTO.setCdate(Tool.getDate());
    CctvVisitor cctvVisitor = cctvVisitorRepository.save(cctvVisitorDTO.toEntity());

    return cctvVisitor;
  }

  public List<CctvVisitor> findAll() {
    List<CctvVisitor> list = cctvVisitorRepository.findAll();

    return list;
  }

  /**
   * 관리자 손님(방문객) 목록 검색 (/dbms/cctvvisitor) - sno 상관없이 전체 대상.
   * sno를 넘기면 해당 매장 소유 CCTV로 찍힌 손님만 필터링.
   */
  public Page<CctvVisitor> searchAdmin(Long sno, Long cno, Integer state, String keyword,
      String intimeFrom, String intimeTo, Pageable pageable) {
    return cctvVisitorRepository.searchAdmin(sno, cno, state, keyword, intimeFrom, intimeTo, pageable);
  }

  /**
   * 매장(사용자) 손님 목록 검색 (/user/cctvvisitor) - sno(입장한 매장) 소유 CCTV로 찍힌 손님만 대상.
   */
  public Page<CctvVisitor> searchByShop(long sno, Long cno, Integer state, String keyword,
      String intimeFrom, String intimeTo, Pageable pageable) {
    return cctvVisitorRepository.searchByShop(sno, cno, state, keyword, intimeFrom, intimeTo, pageable);
  }

  public CctvVisitor findById(long pk) {
    Optional<CctvVisitor> optional = cctvVisitorRepository.findById(pk);

    if (optional.isPresent()) {
      CctvVisitor cctvVisitor = optional.get();
      return cctvVisitor;
    }
    return null;
  }

  public CctvVisitor update(CctvVisitorDTO cctvVisitorDTO) {
    CctvVisitor cctvVisitor = cctvVisitorRepository.findById(cctvVisitorDTO.getNo()).get();
    cctvVisitor.setCno(cctvVisitorDTO.getCno());
    cctvVisitor.setTrackId(cctvVisitorDTO.getTrackId());
    cctvVisitor.setIntime(cctvVisitorDTO.getIntime());
    cctvVisitor.setOuttime(cctvVisitorDTO.getOuttime());
    cctvVisitor.setStaytime(cctvVisitorDTO.getStaytime());
    cctvVisitor.setState(cctvVisitorDTO.getState());

    CctvVisitor savedEntity = cctvVisitorRepository.save(cctvVisitor);

    return savedEntity;
  }

  public boolean deleteById(long pk) {
    Optional<CctvVisitor> optional = cctvVisitorRepository.findById(pk);

    if (optional.isPresent()) {
      cctvVisitorRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}