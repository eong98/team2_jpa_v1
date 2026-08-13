package dev.jpa.allimio.history.login;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.history.update.UpdateHistoryDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/history/login")
public class LoginHistoryCont {
  private final LoginHistoryService loginHistoryService;

  /**  
   * 전체 변경 이력 조회
   * http://10.1.205.120:9102/history/login/list
   * @return 전체이력
   */
  @GetMapping(path="/list")
  public ResponseEntity<List<LoginHistoryDTO>> getAllLoginHistories() {
      List<LoginHistoryDTO> histories = loginHistoryService.findAll();
      return ResponseEntity.ok(histories); 
  }
}
