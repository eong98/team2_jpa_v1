package dev.jpa.allimio.cctvvisitor;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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