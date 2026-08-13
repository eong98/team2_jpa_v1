package dev.jpa.allimio.history.login;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {
  private final LoginHistoryRepository loginHistoryRepository;
  
  /** 전체 로그인 이력 조회 */
  public List<LoginHistoryDTO> findAll(){
    return loginHistoryRepository.findAll().stream()
             .map(LoginHistoryDTO::from)
             .toList();
  }
  
  
}
