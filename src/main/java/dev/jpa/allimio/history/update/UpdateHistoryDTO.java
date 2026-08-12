package dev.jpa.allimio.history.update;

import dev.jpa.allimio.manager.Manager;
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
public class UpdateHistoryDTO {
  /** 업데이트 로그 번호 */
  private Long no;
  
  /** 회원 번호 */
  private Long mno;
  
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
  
  /** 변경자 유형 */
  private int changedBy;
  
  /** 변경한 관리자 번호 */
  private Long updtMnno;
  
  public UpdateHistory toEntity() {    
    return UpdateHistory.builder()
        .no(this.no)
        .mno(this.mno != null ? Member.builder().no(this.mno).build() : null)
        .mnno(this.mnno != null ? Manager.builder().no(this.mnno).build() : null)
        .changedColumn(this.changedColumn)
        .oldValue(this.oldValue)
        .newValue(this.newValue)
        .changeDate(this.changeDate)
        .changedBy(this.changedBy)
        .updtMnno(this.updtMnno != null ? Manager.builder().no(this.updtMnno).build() : null)
        .build();
  }
  
  public static UpdateHistoryDTO from(UpdateHistory entity) {
    if (entity == null) return null;

    return UpdateHistoryDTO.builder()
        .no(entity.getNo())
        .mno(entity.getMno() != null ? entity.getMno().getNo() : null)
        .mnno(entity.getMnno() != null ? entity.getMnno().getNo() : null)
        .changedColumn(entity.getChangedColumn())
        .oldValue(entity.getOldValue())
        .newValue(entity.getNewValue())
        .changeDate(entity.getChangeDate())
        .changedBy(entity.getChangedBy())
        .updtMnno(entity.getUpdtMnno() != null ? entity.getUpdtMnno().getNo() : null)
        .build();
  }

}
