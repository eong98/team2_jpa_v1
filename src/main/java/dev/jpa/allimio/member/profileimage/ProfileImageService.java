package dev.jpa.allimio.member.profileimage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileImageService {
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;

    /** 
     * 프로필 파일 수정 (JPA Dirty Checking 활용 또는 쿼리 메서드 호출)
     */
    @Transactional
    public int update_file(String uploadFilename, String storeFilename, long memberno) {
        String now = Tool.getDate();

        // 1. 기존 엔티티 조회 없으면 신규 생성
        ProfileImage profileImage = profileImageRepository.findById(memberno)
                .orElseGet(() -> {
                    // 최초 등록 시 Member 영속/참조 객체 조회
                    Member member = memberRepository.findById(memberno)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 번호입니다: " + memberno));
                    
                    return ProfileImage.builder()
                            .member(member)
                            .build();
                });

        // 2. 값 설정
        profileImage.setUploadFilename(uploadFilename);
        profileImage.setStoreFilename(storeFilename);
        profileImage.setUpdateDate(now);

        // 3. 저장 (신규면 em.persist/INSERT, 기존이면 Dirty Checking/UPDATE)
        profileImageRepository.save(profileImage);

        return 1;
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