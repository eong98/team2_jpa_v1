package dev.jpa.allimio.history.update;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateHistoryService {
  private final UpdateHistoryRepository updateHistoryRepository;
  
  /**
   * 전체 변경 이력 목록 조회
   */
  public List<UpdateHistoryDTO> findAll() {
      return updateHistoryRepository.findAll().stream()
              .map(UpdateHistoryDTO::from)
              .toList(); 
  }

//  /**
//   * 특정 이력 단건 조회 (필요 시)
//   */
//  public UpdateHistoryDTO findById(Long no) {
//      return updateHistoryRepository.findById(no)
//              .map(UpdateHistoryDTO::from)
//              .orElseThrow(() -> new IllegalArgumentException("해당 변경 이력이 존재하지 않습니다. id=" + no));
//  }

}
