package dev.jpa.allimio.member.profileimage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.jpa.allimio.history.update.UpdateHistoryService;
import dev.jpa.allimio.tool.Tool;
import dev.jpa.allimio.tool.Upload;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile/img")
public class ProfileImageCont {
  private final ProfileImageService profileImageService;

  /**
   * 등록 처리 http://localhost:9100/contents/create
   * 
   * @return
   */
  @PostMapping(value = "/create")
  public ResponseEntity<Void> create(@RequestParam(name="memberno") Long memberno) {
    boolean check = profileImageService.create(memberno);
    
    if(check) {
      return ResponseEntity.ok().build();
    }else {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 파일 수정, 회원가입시 테이블 1:1 매칭되서 첫 등록도 수정으로 취급.
   * 
   * @param file1MF 원본이미지
   * @param memberno 회원 번호
   * @return 0: 이미지 수정 실패, 1: 이미지 수정 성공, 2: 전송할 파일이 없음
   */
  @PostMapping(path="/update")
  public ResponseEntity<Integer> update_file(
      @RequestParam(name="file1MF", defaultValue = "") MultipartFile file1MF,
      @RequestParam(name="memberno", defaultValue = "0" ) Long memberno) {
      int sw = 0;
    
      String upDir = Tool.getServerDir("profile");
      
      // 파일 삭제
      ProfileImageDTO  profileImageDTO  = this.profileImageService.findByMemberno(memberno);
      if(profileImageDTO.getStoreFilename() != null && !profileImageDTO.getStoreFilename().equals("")) {
        Tool.deleteFile(upDir, profileImageDTO.getStoreFilename());
      }
  
      // ------------------------------------------------------------------------------
      // 파일 전송 코드 시작
      // ------------------------------------------------------------------------------
      String uploadFilename = ""; // 원본 파일명 image
      String storeFilename = ""; // 저장된 파일명, image
      
      // 전송 파일이 없어도 file1MF 객체가 생성됨.
      // <input type='file' class="form-control" name='file1MF' id='file1MF'
      // value='' placeholder="파일 선택">
      MultipartFile mf = file1MF;

      if (mf != null) { // 1. 파일 전송 객체가 존재할 때
        uploadFilename = mf.getOriginalFilename(); 
        System.out.println("-> 원본 파일명 산출 file1: " + uploadFilename);
        
        if (Tool.checkUploadFile(uploadFilename) == true) { // 2. 업로드 가능한 파일인지 검사
            // 파일 저장
            storeFilename = Upload.saveFileSpring(mf, upDir);

            profileImageDTO.setUploadFilename(uploadFilename); 
            profileImageDTO.setStoreFilename(storeFilename);          

            // 검사 통과 시에만 DB 업데이트 실행
            int cnt = this.profileImageService.update_file(uploadFilename, storeFilename, memberno);
            sw = cnt; // 0 or 1
            
        } else { // 💡 [추가 권장] 확장자 검사 실패 시 처리
            sw = 0; // 업로드 불가능한 파일 형식일 때 0 리턴
        }
        
    } else { // 파일 객체 자체가 없을 때
        sw = 2;
    }
      
    return ResponseEntity.ok(sw);
  }

  /**
   * 파일 삭제, http://localhost:9100/contents/delete_file1
   * @param memberno
   * @return 0: 이미지 삭제 실패, 1: 이미지 삭제 성공, 2: 기본 이미지 삭제 시도
   */
  @PostMapping(path="/delete")
  public ResponseEntity<Integer> delete_file(
      @RequestParam(name="memberno", defaultValue = "0" ) Long memberno) {
    int sw = 0;
    
      String dir = Tool.getServerDir("profile");
      
      // 삭제할 파일 정보 읽기
      ProfileImageDTO  profileImageDTO  = this.profileImageService.findByMemberno(memberno);
      
      if (!profileImageDTO.getStoreFilename().equals("")) {
        Tool.deleteFile(dir, profileImageDTO.getStoreFilename());      
        
        // 파일을 삭제했음으로 DBMS에서 파일 정보를 삭제함.
        int cnt = this.profileImageService.update_file("", "",  memberno);
        sw = cnt; // 0 or 1
      } else {
        sw = 2; // 기본 이미지 삭제 시도
      }
  
    return ResponseEntity.ok(sw);
  }
}
