package dev.jpa.allimio.member.profileimage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.jpa.allimio.tool.Tool;
import dev.jpa.allimio.tool.Upload;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile/img")
public class ProfileImageCont {

    private final ProfileImageService profileImageService;

    /**
     * 프로필 이미지 수정/업로드
     * POST /profile/img/update
     * @return 0: 이미지 수정 실패(확장자 불일치 등), 1: 수정 성공, 2: 전송할 파일이 없음
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update_file(
            @RequestParam(name = "file1MF", required = false) MultipartFile file1MF,
            @RequestParam(name = "memberno", defaultValue = "0") Long memberno) {

        // 1. 파일 전송 여부 확인
        if (file1MF == null || file1MF.isEmpty()) {
            return ResponseEntity.ok(2); // 전송할 파일이 없음
        }

        String uploadFilename = file1MF.getOriginalFilename();

        // 2. 확장자 검사
        if (!Tool.checkUploadFile(uploadFilename)) {
            return ResponseEntity.ok(0); // 허용되지 않은 파일 형식
        }

        String upDir = Tool.getServerDir("profile");
        
        System.out.println("검사 통과");

        // 3. 새 파일 물리적 저장
        String storeFilename = Upload.saveFileSpring(file1MF, upDir);

        // 4. 기존 파일명 조회 (교체 성공 후 삭제하기 위함)
        ProfileImageDTO oldImage = this.profileImageService.findByMemberno(memberno);
        String oldStoreFilename = (oldImage != null) ? oldImage.getStoreFilename() : null;

        // 5. DB 정보 갱신
        int cnt = this.profileImageService.update_file(uploadFilename, storeFilename, memberno);

        // 6. DB 갱신 성공 시 이전 파일 삭제
        if (cnt > 0 && oldStoreFilename != null && !oldStoreFilename.trim().isEmpty()) {
            Tool.deleteFile(upDir, oldStoreFilename);
        }

        return ResponseEntity.ok(cnt);
    }

    /**
     * 프로필 이미지 삭제 (기본 이미지로 초기화)
     * POST /profile/img/delete
     * @return 0: 삭제 실패, 1: 삭제 성공, 2: 이미 등록된 파일이 없음
     */
    @PostMapping("/delete")
    public ResponseEntity<Integer> delete_file(
            @RequestParam(name = "memberno", defaultValue = "0") Long memberno) {

        ProfileImageDTO profileImageDTO = this.profileImageService.findByMemberno(memberno);

        if (profileImageDTO == null || profileImageDTO.getStoreFilename() == null || profileImageDTO.getStoreFilename().trim().isEmpty()) {
            return ResponseEntity.ok(2); // 삭제할 커스텀 이미지가 없음 (기본 이미지 상태)
        }

        String dir = Tool.getServerDir("profile");

        // 실제 물리 디스크 파일 삭제
        Tool.deleteFile(dir, profileImageDTO.getStoreFilename());

        // DB 파일명 정보 빈 문자열로 초기화
        int cnt = this.profileImageService.update_file("", "", memberno);

        return ResponseEntity.ok(cnt);
    }
    
    /**
     * 프로필 이미지 정보 조회
     * GET /profile/img/{memberno}
     */
    @GetMapping("/{memberno}")
    public ResponseEntity<ProfileImageDTO> getProfileImage(@PathVariable("memberno") Long memberno) {
        ProfileImageDTO dto = this.profileImageService.findByMemberno(memberno);

        // 등록된 이미지가 없으면 null을 담아 200 OK 반환 (프론트가 기본 아바타/첫 글자 출력)
        if (dto == null) {
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(dto);
    }
}