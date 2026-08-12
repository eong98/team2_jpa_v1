package dev.jpa.allimio.manager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.history.update.UpdateHistoryDTO;
import dev.jpa.allimio.history.update.UpdateHistoryRepository;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor    // final 붙은 필드들을 파라미터로 가지는 생성자가 자동생성
public class ManagerService {
  private final ManagerRepository managerRepository;
  private final UpdateHistoryRepository updatehistoryRepository;
  
  /**
   * 아이디 중복 체크
   * @param id
   * @return 중복된 아이디 없음 true, 중복된 아이디 있음 false
   */
  public boolean isIdAvailable(String id) {
    return !(managerRepository.existsById(id));
  }
  
  /**
   * 회원가입
   * @param managerDTO
   * @return 입력한 관리자 정보를 저장
   */
  public Manager save(ManagerDTO managerDTO) {
    managerDTO.setCdate(Tool.getDate());
    
    return managerRepository.save(managerDTO.toEntity());
  }
  
  /**
   * 로그인
   * @param id 
   * @param password
   * @return 성공이면 관리자의 DTO, 실패면 빈 DTO
   */
  public Optional<ManagerDTO> login(String id, String password) {
    boolean check = managerRepository.existsByIdAndPassword(id, password);
    
    if(check ) {
      Optional<ManagerDTO> managerDTO = managerRepository.longinDTO(id, password);
      
      return managerDTO;
    } else {
      return Optional.empty();
    }
  }
  
  
  /**
   * 패스워드 제외 목록 조회
   * @return 패스워드를 제외한 회원 정보
   */
  public List<ManagerDTO> findAllWithoutPassword(){
    List<ManagerDTO> list = managerRepository.findAllWithoutPassword();
    
    return list;
  }
  
  
  /**
   * 관리자 정보 조회
   * @param managerno
   * @return
   */
  public ManagerDTO findWithNoWithoutPassword(Long managerno) {
    
    return managerRepository.findWithNoWithoutPassword(managerno).orElseThrow();
  }
  
  /**
   * 관리자가 자기 정보 수정하는 경우.
   * 중간에 saveUpdateLogs 호출 해서 업데이트 로그 보존.
   * @param managerno 변경된 회원 번호
   * @param managerDTO 변경할 값
   */
  @Transactional
  public void updateManagerBySelf(Long managerno, ManagerDTO managerDTO) {
    String now = Tool.getDate();
    Manager manager = managerRepository.findById(managerno).orElseThrow();
    
    saveUpdateLogs(manager, managerDTO, 0, managerno, now);
    
    manager.updateBySelf(managerDTO.getMname(), managerDTO.getEmail(), managerDTO.getPhone(), now);
  }
  
  /**
   * 최상위 관리자가 관리자 정보를 수정하는 경우.
   * 중간에 saveUpdateLogs 호출 해서 업데이트 로그 보존.
   * @param targetno 
   * @param managerDTO
   * @param managerno
   */
  @Transactional
  public void updateManagerByManager(Long targetno, ManagerDTO managerDTO, Long managerno) {
    String now = Tool.getDate();
    Manager manager = managerRepository.findById(targetno).orElseThrow();
    
    saveUpdateLogs(manager, managerDTO, 1, managerno, now);
    
    manager.updateByManager(managerDTO.getMname(), managerDTO.getEmail(), managerDTO.getPhone(), managerDTO.getGrade(),  managerDTO.getStatus(), now);
  }
  
  /**
   * 기존값과 비교해 바뀐 값이 있다면 업데이트 로그를 찍어줌
   * @param manager 기존 값
   * @param managerDTO 새로운 값
   * @param managerno 변경한 관리자 번호
   * @param now 변경한 시간
   */
  private void saveUpdateLogs(Manager manager, ManagerDTO managerDTO, int changedBy, Long managerno, String now) {
    String [] columsToCompare = {"mname", "phone", "email", "grade", "status"};
    
    try {
      for (String fieldName : columsToCompare) {
        // 키 : 값 형태로 가져오기 위해 필드 정보 세팅 
        Field entityField = Manager.class.getDeclaredField(fieldName);
        Field dtoField = ManagerDTO.class.getDeclaredField(fieldName);
        
        // 필드 수정 권한 
        entityField.setAccessible(true);
        dtoField.setAccessible(true);
        
        // 필드 안의 값의 형태가 다르므로 오브젝트로 값을 저장.
        Object oldValueObj = entityField.get(manager);
        Object newValueObj = dtoField.get(managerDTO);
        
        // 바뀐 값이 null 이면 수정하지 않은 값으로 판단하고 스킵
        if (newValueObj == null) {
          continue;
        }
        
        // 값이 null인 경우 ""으로 변환
        String oldValue = (oldValueObj != null) ? oldValueObj.toString() : "";
        String newValue = newValueObj.toString();
        
        // oldValue와 newValue의 값이 다를경우 log를 생성 후 저장.
        if(!oldValue.equals(newValue)) {
          UpdateHistoryDTO log = UpdateHistoryDTO.builder()
              .mno(null)
              .mnno(manager.getNo())
              .changedColumn(fieldName.toUpperCase())
              .oldValue(oldValue)
              .newValue(newValue)
              .changeDate(now)
              .changedBy(changedBy)
              .updtMnno(managerno)
              .build();
          
          updatehistoryRepository.save(log.toEntity());
        }
      }
    } catch(Exception e) {
      throw new RuntimeException("로그 생성 중 오류 발생", e);
    }
  }
  
  /**
   * 비밀번호 수정
   * @param id 아이디
   * @param password 비밀번호
   * @return 수정됐다면 true, 실패했다면 false
   */
 public boolean updatePassword(String id, String password) {
   int check = managerRepository.updatePassword(id, password);
   
   return check > 0;
 }
 
 
}
