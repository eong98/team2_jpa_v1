package dev.jpa.allimio.shopinvitecode;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.shopinvitecode.InviteCodeDTO.Request;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invite")
public class InviteCodeCont {
  private final InviteCodeService codeService;
  
  /**
   * 코드 생성
   * @param shopno
   * @return
   */
  @PostMapping(path="/create/{shopno}")
  public ResponseEntity<String> createCode(
      @PathVariable("shopno") Long shopno){
    String code = codeService.createCode(shopno);
    
    return ResponseEntity.ok(code);
  }
  
  /**
   * 초대 수락 후 등록
   * @param request
   * @return
   */
  @PostMapping(path="/accept")
  public ResponseEntity<?> inviteMember(
      @RequestBody Request request){
    try {
      codeService.invite(request.getCode(), request.getMno());
      return ResponseEntity.ok(Map.of("success", true));
  } catch (IllegalArgumentException | IllegalStateException e) {
      // 서비스에서 던진 "이미 만료된 초대코드입니다" 등의 메시지를 그대로 전달
      return ResponseEntity.ok(Map.of("success", false));
 
    }
  }
}
