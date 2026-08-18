package dev.jpa.allimio.member.profileimage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.tool.Tool;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileImageService {
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final EntityManager em;

    /**
     * 1:1 대응되는 테이블 생성 (가입 시 자동 생성)
     * @param memberno 회원 번호
     * @return true, false
     */
    @Transactional
    public void create(Long memberno) {
      Member member = memberRepository.findById(memberno)
          .orElseThrow(() -> new IllegalArgumentException("회원 없음: " + memberno));
      
      ProfileImage profileImage = ProfileImage.builder()
          .member(member)
          .storeFilename("")
          .uploadFilename("")
          .updateDate(Tool.getDate())
          .build();

        em.persist(profileImage);
}

    /** 
     * 프로필 파일 수정 (JPA Dirty Checking 활용 또는 쿼리 메서드 호출)
     */
    @Transactional
    public int update_file(String uploadFilename, String storeFilename, long memberno) {
        String now = Tool.getDate();
        
        // 1. 기존 엔티티 조회 후 Dirty Checking(변경 감지)으로 수정하는 방식 (권장)
        return profileImageRepository.findById(memberno)
                .map(image -> {
                    image.setUploadFilename(uploadFilename);
                    image.setStoreFilename(storeFilename);
                    image.setUpdateDate(now);
                    return 1;
                })
                .orElse(0);

        // ※ 기존 벌크 업데이트 쿼리를 유지하신다면 아래 코드로 대체 가능:
        // return profileImageRepository.update_file(uploadFilename, storeFilename, memberno, now);
    }

    /** 
     * 회원 번호로 프로필 이미지 조회
     */
    public ProfileImageDTO findByMemberno(Long memberno) {
        // mno가 PK이므로 findById로 즉시 조회 가능
        ProfileImage profileImage = profileImageRepository.findById(memberno).orElse(null);
        
        // DTO에 만들어둔 fromEntity 편의 메서드 사용
        return ProfileImageDTO.fromEntity(profileImage);
    }
}