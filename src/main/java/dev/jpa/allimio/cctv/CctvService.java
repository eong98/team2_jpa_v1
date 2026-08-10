package dev.jpa.allimio.cctv;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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