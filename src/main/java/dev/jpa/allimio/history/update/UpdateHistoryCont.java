package dev.jpa.allimio.history.update;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/history/update")
public class UpdateHistoryCont {
  private final UpdateHistoryService updateHistoryService;
  
  /**  
   * 전체 변경 이력 조회
   * http://10.1.205.120:9102/history/update/list
   * @return 전체이력
   */
  @GetMapping(path="/list")
  public ResponseEntity<List<UpdateHistoryDTO>> getAllUpdateHistories() {
      List<UpdateHistoryDTO> histories = updateHistoryService.findAll();
      return ResponseEntity.ok(histories); 
  }
  
//  /**
//   * 특정 변경 이력 단건 조회
//   * http://10.1.205.120:9102/history/update/list
//   * @return 단건이력
//   */
//  @GetMapping("/{no}")
//  public ResponseEntity<UpdateHistoryDTO> getUpdateHistoryById(@PathVariable("no") Long no) {
//      UpdateHistoryDTO history = updateHistoryService.findById(no);
//      return ResponseEntity.ok(history);
//  }
}
