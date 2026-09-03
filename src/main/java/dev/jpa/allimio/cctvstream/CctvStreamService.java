package dev.jpa.allimio.cctvstream;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class CctvStreamService {
  @Autowired
  CctvStreamRepository cctvStreamRepository;

  public CctvStreamService() {

  }

  public CctvStream save(CctvStreamDTO cctvStreamDTO) {
    cctvStreamDTO.setCdate(Tool.getDate());
    CctvStream cctvStream = cctvStreamRepository.save(cctvStreamDTO.toEntity());

    return cctvStream;
  }

  public List<CctvStream> findAll() {
    List<CctvStream> list = cctvStreamRepository.findAll();

    return list;
  }

  public Page<CctvStream> search(Long cno, Integer connState, String keyword, Pageable pageable) {
    return cctvStreamRepository.search(cno, connState, keyword, pageable);
  }

  public CctvStream findById(long pk) {
    Optional<CctvStream> optional = cctvStreamRepository.findById(pk);

    if (optional.isPresent()) {
      CctvStream cctvStream = optional.get();
      return cctvStream;
    }
    return null;
  }

  /** CCTV번호(cno)로 스트림 조회 - Jetson 워커/CCTV 상세 화면에서 사용 */
  public CctvStream findByCno(long cno) {
    Optional<CctvStream> optional = cctvStreamRepository.findByCno(cno);

    if (optional.isPresent()) {
      return optional.get();
    }
    return null;
  }

  public CctvStream update(CctvStreamDTO cctvStreamDTO) {
    CctvStream cctvStream = cctvStreamRepository.findById(cctvStreamDTO.getNo()).get();
    cctvStream.setCno(cctvStreamDTO.getCno());
    cctvStream.setStreamUrl(cctvStreamDTO.getStreamUrl());
    cctvStream.setProtocol(cctvStreamDTO.getProtocol());
    cctvStream.setPort(cctvStreamDTO.getPort());
    cctvStream.setConnState(cctvStreamDTO.getConnState());
    cctvStream.setLastConnectedAt(cctvStreamDTO.getLastConnectedAt());

    CctvStream savedEntity = cctvStreamRepository.save(cctvStream);

    return savedEntity;
  }

  public boolean deleteById(long pk) {
    Optional<CctvStream> optional = cctvStreamRepository.findById(pk);

    if (optional.isPresent()) {
      cctvStreamRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}
