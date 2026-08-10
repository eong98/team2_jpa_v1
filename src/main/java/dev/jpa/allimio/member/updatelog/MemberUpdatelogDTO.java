package dev.jpa.allimio.member.updatelog;

import dev.jpa.allimio.member.Member;
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
public class MemberUpdatelogDTO {
  /** 업데이트 로그 번호 */
  private Long no;
  
  /** 회원 번호 */
  private Long mno;
  
  /** 변경한 항목 */
  private String changedColumn;
  
  /** 변경 전 값 */
  private String oldValue;
  
  /** 변경 후 값 */
  private String newValue;
  
  /** 변경 일시 */
  private String changeDate;
  
  /** 변경자 유형 */
  private int changedBy;
  
  /** 변경한 관리자 번호 */
  private Long updtMnno;
  
  public MemberUpdatelog toEntity() {
    Member tempMno = new Member();   
    tempMno.setNo(this.mno);
    
    Member tempUpdtMnno = null;   // 회원이 정보를 수정한 경우 관리자 번호가 없으므로 null 객체 생성
    if(this.updtMnno != null) {
      tempUpdtMnno = new Member();
      tempUpdtMnno.setNo(this.updtMnno);
    }
    
    return MemberUpdatelog.builder()
        .no(this.no)
        .mno(tempMno)
        .changedColumn(this.changedColumn)
        .oldValue(this.oldValue)
        .newValue(this.newValue)
        .changeDate(this.changeDate)
        .changedBy(this.changedBy)
        .updtMnno(tempUpdtMnno)
        .build();
  }

}
