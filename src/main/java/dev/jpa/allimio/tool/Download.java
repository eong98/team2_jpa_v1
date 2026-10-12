package dev.jpa.allimio.tool;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Download {
  public Download() {
    System.out.println("-> Download created.");
  }
  
  /**
   * 
   * @param dir 저장된 폴더명 예) contents, member, product...
   * @param filename 저장된 파일명 
   * @param downname 원본 파일명
   * @return
   */
  @GetMapping(value="/download")
  public ResponseEntity<Resource> download(
      @RequestParam(name="dir", defaultValue = "") String dir,
      @RequestParam(name="filename", defaultValue = "") String filename,
      @RequestParam(name="downname", defaultValue = "") String downname) {
    // C:/kd/deploy/resort/home/storage
    // C:/kd/deploy/resort/member/storage
    // C:/kd/deploy/resort/product/storage
    // C:/kd/deploy/resort/contents/storage
    
    File file = new File(Tool.getServerDir(dir) + downname);    

    // 저장 폴더 밖의 파일은 내려주지 않음 (dir·downname에 ../ 를 넣어 다른 경로를 읽는 것 차단)
    if (!isInsideStorage(dir, file)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    if(!file.isFile()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      Resource resource = new FileSystemResource(file);
      // 한글 파일명을 UTF-8로 인코딩
      String encodedDownname = "";
      
      try {
          encodedDownname = URLEncoder.encode(downname, "UTF-8").replaceAll("\\+", "%20");
      } catch (UnsupportedEncodingException e) {
          encodedDownname = downname; // 인코딩 실패 시 원래 이름 사용
      }

      return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedDownname)
              .body(resource);
    }
  }

  /** 요청한 파일이 저장 폴더(…/{dir}/storage/) 안에 있는지 — dir은 폴더명 하나만 허용 */
  private boolean isInsideStorage(String dir, File file) {
    if (dir == null || !dir.matches("[A-Za-z0-9_-]+")) {
      return false;
    }
    Path base = Paths.get(Tool.getServerDir(dir)).toAbsolutePath().normalize();
    Path target = file.toPath().toAbsolutePath().normalize();
    return target.startsWith(base) && !target.equals(base);
  }
    
}


