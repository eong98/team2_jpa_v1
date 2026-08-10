package dev.jpa.allimio.manager.updatelog;

import dev.jpa.allimio.manager.Manager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ManagerUpdatelogDTO {
  /** 업데이트 로그 번호 */
  private Long no;
  
  /** 관리자 번호 */
  private Long mnno;
  
  /** 변경한 항목 */
  private String changedColumn;
  
  /** 변경 전 값 */
  private String oldValue;
  
  /** 변경 후 값 */
  private String newValue;
  
  /** 변경 일시 */
  private String changeDate;
  
  /** 변경한 관리자 번호 */
  private Long updtMnno;
  
  public ManagerUpdatelog toEntity() {
    Manager tempMnno = new Manager();   
    tempMnno.setNo(this.mnno);
    
    Manager tempUpdtMnno = new Manager(); 
      tempUpdtMnno.setNo(this.updtMnno);
    
    return ManagerUpdatelog.builder()
        .no(this.no)
        .mnno(tempMnno)
        .changedColumn(this.changedColumn)
        .oldValue(this.oldValue)
        .newValue(this.newValue)
        .changeDate(this.changeDate)
        .updtMnno(tempUpdtMnno)
        .build();
  }
}
