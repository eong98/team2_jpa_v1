package dev.jpa.allimio.member.profileimage;

import dev.jpa.allimio.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProfileImageDTO {
    /** 회원 번호 (PK이자 FK) */
    private Long mno;

    /** 저장된 파일명 */
    private String storeFilename;

    /** 원본 파일명 */
    private String uploadFilename;

    /** 변경일 */
    private String updateDate;

    /** DTO -> Entity 변환 */
    public ProfileImage toEntity() {
        return ProfileImage.builder()
                .mno(this.mno)
                .member(this.mno != null ? Member.builder().no(this.mno).build() : null)
                .storeFilename(this.storeFilename != null ? this.storeFilename : "")
                .uploadFilename(this.uploadFilename != null ? this.uploadFilename : "")
                .updateDate(this.updateDate)
                .build();
    }

    /** Entity -> DTO 변환 편의 메서드 */
    public static ProfileImageDTO fromEntity(ProfileImage entity) {
        if (entity == null) {
            return null;
        }
        return ProfileImageDTO.builder()
                .mno(entity.getMno())
                .storeFilename(entity.getStoreFilename())
                .uploadFilename(entity.getUploadFilename())
                .updateDate(entity.getUpdateDate())
                .build();
    }
}