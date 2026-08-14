package dev.jpa.allimio.attach;

import dev.jpa.allimio.inmenu.InMenu;
import dev.jpa.allimio.inmenu.InMenuRepository;
import dev.jpa.allimio.shopmenu.ShopMenu;
import dev.jpa.allimio.shopmenu.ShopMenuRepository;
import dev.jpa.allimio.tool.Tool;
import dev.jpa.allimio.tool.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [첨부파일 전담 비즈니스 로직 서비스]
 * 
 * - 게시판/메뉴 번호(tno), 테이블/폴더명(tname) 및 게시글 번호(bno)에 귀속되는 파일의 저장/조회/삭제를 담당합니다. -
 * 파일 확장자를 체크하여 이미지/일반문서 저장 경로를 자동 분리하고, 이미지 파일은 썸네일을 생성합니다. - InMenu(내부메뉴) 및
 * ShopMenu(상점메뉴) 두 가지 메뉴 테이블 정보를 조회하여 연동할 수 있습니다. - 실물 파일 삭제/업로드 및 디렉터리 처리는 공통
 * 모듈인 `Tool.java` 및 `Upload.java`를 사용합니다.
 */
@Service
public class AttachService {

  @Autowired
  private AttachRepository attachRepository;

  /**
   * 메뉴 정보 조회를 위해 주입 (등록한 테이블명 PK(tno) / 등록한 테이블명 tname 조회)
   * 
   */
  @Autowired
  private InMenuRepository inMenuRepository;

  @Autowired
  private ShopMenuRepository shopMenuRepository;

  // [서버 루트 파일 저장 경로] 예: /allimio/attach
  private final String baseDir = Tool.getServerDir("attach");

  /**
   * [테이블명/메뉴명 조회 헬퍼 메서드]
   * - 메뉴 PK(tno)를 기준으로 InMenu에서 먼저 조회하고, 없으면 ShopMenu에서 2차 조회를 수행합니다.
   * 
   * @param tno 메뉴/게시판 구분 PK 번호
   * @return 조회된 테이블명/메뉴명 (기본값: "기타")
   */
  public String getTableName(Long tno) {
    // PK가 넘어오지 않은 경우 예외 처리
    if (tno == null) return "기타";

    // 1단계: InMenu 테이블에서 해당 PK(tno) 조회
    Optional<InMenu> inMenu = inMenuRepository.findById(tno);
    if (inMenu.isPresent()) {
      return inMenu.get().getTname(); // InMenu에 존재하면 해당 테이블명 반환
    }

    // 2단계: InMenu에 없으면 ShopMenu 테이블에서 해당 PK(tno) 조회
    Optional<ShopMenu> shopMenu = shopMenuRepository.findById(tno);
    if (shopMenu.isPresent()) {
      return shopMenu.get().getTname(); // ShopMenu에 존재하면 해당 테이블명 반환
    }

    // 두 곳 모두 존재하지 않을 경우 기본값 반환
    return "기타";
  }


  /**
   * [다중 파일 업로드 및 DB 저장]
   * - 클라이언트가 전송한 파일 목록(List<MultipartFile>)을 순회하며 실물 파일 저장 및 DB 등록을 진행합니다.
   * 
   * @param tno   메뉴/게시판 PK 번호 (FK)
   * @param tname 테이블명 또는 게시판 폴더명 (예: "notice", "qa")
   * @param bno   해당 게시글의 PK (게시글 번호)
   * @param files 컨트롤러에서 전달받은 업로드 파일 목록
   * @return DB 저장 및 파일 업로드가 완료된 AttachDTO 리스트
   */
  @Transactional
  public List<AttachDTO> saveAttachFiles(Long tno, String tname, Long bno, List<MultipartFile> files) {
    List<AttachDTO> resultList = new ArrayList<>();
    
    // [게시판 폴더 경로] 예: /attach/storage/notice
    String boardDir = baseDir + tname;
    System.out.println("-> 게시판 전용 대상 경로 boardDir: " + boardDir);

    String sname = ""; // 서버에 실제 저장될 난수화된 파일명 (UUID/타임스탬프 적용)
    String name = "";  // 클라이언트가 올린 원래 파일명
    String thumb = ""; // 이미지 썸네일 파일명
    String purl = "";  // 저장 상대 경로 (/images 또는 /files)
    int type = 1;      // 파일 구분 (0: 이미지, 1: 일반 문서/기타 파일)
    

    // 파일 리스트가 없거나 빈 경우 바로 빈 결과 반환
    if (files == null || files.isEmpty()) {
      return resultList;
    }
    

//    createFolder(boardDir);   // 테이블 폴더 생성

    // [파일 리스트 순회 처리]
    for (MultipartFile mf : files) {
      if (mf == null || mf.isEmpty()) continue; // 빈 요소는 스킵

      name = mf.getOriginalFilename(); // 원본 파일명 추출
      long fsize = mf.getSize();       // 파일 용량(Byte)

      // 파일 사이즈가 0보다 크고, 허용된 확장자인지 검사 (보안 체킹)
      if (fsize > 0 && Tool.checkUploadFile(name)) {        
        boolean isImg = Tool.isImage(name); // 이미지 파일 여부 확인 (jpg, png, gif 등)

        if (isImg) {
          // ==========================================
          // 1) 이미지 파일 처리 (type = 0)
          // ==========================================
          type = 0;
          purl = "/images";
          
          String imageDir = boardDir + purl;       // 원본 이미지 저장 경로: /storage/attach/테이블명/images
          String thumbDir = imageDir + "/thumbs"; // 썸네일 저장 경로: /storage/attach/테이블명/images/thumbs

          // 썸네일 폴더 생성 (상위 images 폴더도 함께 생성됨)
          createFolder(thumbDir);
          
          // 실물 원본 이미지 파일 저장
          sname = Upload.saveFileSpring(mf, imageDir);
          
          // 썸네일 이미지 생성 및 /thumbs 폴더로 이동
          try {
            // Tool.preview를 통해 200x150 크기 썸네일 생성 (images 폴더에 생성됨)
            thumb = Tool.preview(imageDir, sname, 200, 150); 
            
            if (thumb != null && !thumb.isBlank()) {
              File generatedThumb = new File(imageDir, thumb);
              File targetThumb = new File(thumbDir, thumb);
              // 생성된 썸네일 파일을 /images/thumbs 디렉터리로 이동
              Files.move(generatedThumb.toPath(), targetThumb.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

        } else {
          // ==========================================
          // 2) 일반 문서/기타 파일 처리 (type = 1)
          // ==========================================
          type = 1;
          purl = "/files";
          String fileDir = boardDir + purl; // 일반파일 저장 경로: /storage/attach/테이블명/files

          // 파일 저장 디렉터리 생성
          createFolder(fileDir);
          
          // 실물 일반 파일 저장
          sname = Upload.saveFileSpring(mf, fileDir);
          System.out.println("4");
        }

        // DB에 저장할 AttachDTO 객체 빌드
        AttachDTO dto = AttachDTO.builder()
            .tno(tno)
            .tname(tname)
            .bno(bno)
            .type(type)
            .name(name)
            .fsize(fsize)
            .sname(sname)
            .thumb(thumb)
            .purl(purl)
            .cdate(Tool.getDate()) // 현재 등록 일시 (YYYY-MM-DD HH:mm:ss)
            .build();

        // DTO -> Entity 변환 후 DB에 최종 저장
        Attach saved = attachRepository.save(dto.toEntity());
        // 저장 결과 Entity -> DTO 변환 후 리턴 목록에 추가
        resultList.add(AttachDTO.fromEntity(saved));
      }
    }

    return resultList;
  }


  /**
   * [특정 게시글의 첨부파일 목록 조회]
   * - 상세보기(Read) 페이지에서 해당 게시글(bno)과 메뉴(tno)에 귀속된 파일 목록을 가져옵니다.
   * 
   * @param tno 메뉴/게시판 PK 번호
   * @param bno 게시글 번호
   * @return 해당 게시글의 첨부파일 DTO 목록
   */
  public List<AttachDTO> getAttachList(Long tno, Long bno) {
    List<Attach> list = attachRepository.findByTnoAndBno(tno, bno);
    
    // Entity 리스트를 DTO 리스트로 변환하여 반환
    return list.stream()
        .map(AttachDTO::fromEntity)
        .collect(Collectors.toList());
  }


  /**
   * [단건 첨부파일 상세 조회 + 메뉴/테이블 정보 연동 조립]
   * - 특정 첨부파일 정보 조회 시, InMenu / ShopMenu 중 어느 메뉴 소속인지 이름을 조립합니다.
   * 
   * @param no 첨부파일 PK (ATTACH.NO)
   * @return 테이블명이 연동/확인된 AttachDTO 객체
   */
  public AttachDTO getAttachWithMenu(Long no) {
    Attach attach = attachRepository.findById(no).orElse(null);
    if (attach == null) return null;

    AttachDTO dto = AttachDTO.fromEntity(attach);
    
    // tno(메뉴PK)로 InMenu/ShopMenu 중 연동된 테이블명(tname) 가져오기
    String tableName = getTableName(dto.getTno());
    System.out.println("-> 연동된 테이블명 이름: " + tableName);

    return dto;
  }


  /**
   * [관리자/목록용 첨부파일 다중 조건 동적 검색 및 페이징 조회]
   * 
   * @param word     검색어 (파일명/서버파일명 등)
   * @param tno      메뉴 번호 조건 (선택)
   * @param tname    테이블명 조건 (선택)
   * @param name     원본 파일명 조건 (선택)
   * @param type     파일 타입 (0: 이미지, 1: 일반파일) (선택)
   * @param cdate    등록일자 조건 (선택)
   * @param pageable 페이징 및 정렬 정보 (PageRequest)
   * @return 페이징 처리된 AttachDTO 결과 객체 (Page<AttachDTO>)
   */
  public Page<AttachDTO> searchAllAttach(String word, Long tno, String tname, String name, Integer type, String cdate, Pageable pageable) {
    Page<Attach> page = attachRepository.searchAllAttach(word, tno, tname, name, type, cdate, pageable);
    
    // Page<Entity>를 Page<DTO>로 일괄 변환하여 반환
    return page.map(AttachDTO::fromEntity);
  }


  /**
   * [단일 첨부파일 삭제]
   * - 서버 디렉터리의 실물 파일(원본 이미지/문서 + 썸네일)을 삭제한 후, DB의 레코드를 제거합니다.
   * 
   * @param no 삭제할 첨부파일의 PK (ATTACH.NO)
   * @return 성공 시 1, 대상 없을 시 0
   */
  @Transactional
  public int deleteAttachFile(Long no) {
    // 1단계: DB에서 삭제할 파일 정보 조회
    Attach attach = attachRepository.findById(no).orElse(null);
    if (attach == null) {
      return 0; // 해당 PK의 레코드가 없으면 처리 종료
    }

    // 2단계: 실물 파일이 존재할 경우 파일 삭제
    if (attach.getSname() != null && !attach.getSname().isBlank()) {
      // 저장된 테이블명에 맞게 매칭
      String boardDir = baseDir + attach.getTname(); 
      
      if (attach.getType() == 0) {
        // 이미지 파일인 경우: 원본 및 썸네일 실물 파일 둘 다 삭제
        String imageDir = boardDir + "/images";
        String thumbDir = imageDir + "/thumbs";
        
        Tool.deleteFile(imageDir, attach.getSname()); // 원본 이미지 삭제
        if (attach.getThumb() != null && !attach.getThumb().isBlank()) {
          Tool.deleteFile(thumbDir, attach.getThumb()); // 썸네일 이미지 삭제
        }
      } else {
        // 일반 문서 파일인 경우: /files 폴더에서 실물 파일 삭제
        Tool.deleteFile(boardDir + "/files", attach.getSname());
      }
    }

    // 3단계: DB 레코드 삭제
    attachRepository.delete(attach);
    return 1;
  }


  /**
   * [특정 게시글 삭제 시 연관된 모든 첨부파일 일괄 삭제]
   * - 게시글이 삭제될 때 해당 게시글(tno, bno)에 묶여 있는 모든 실물 파일과 DB 레코드를 정리합니다.
   * 
   * @param tno 메뉴/게시판 PK 번호
   * @param bno 게시글 번호
   */
  @Transactional
  public void deleteByTnoAndBno(Long tno, Long bno) {
    // 1단계: 해당 게시글에 연결된 모든 첨부파일 목록 조회
    List<Attach> list = attachRepository.findByTnoAndBno(tno, bno);

    // 2단계: 모든 파일에 대해 실물 파일 반복 삭제
    for (Attach attach : list) {
      // 저장된 테이블명에 맞게 매칭
      String boardDir = baseDir + attach.getTname();
      
      if (attach.getSname() != null && !attach.getSname().isBlank()) {
        if (attach.getType() == 0) {
          String imageDir = boardDir + "/images";
          String thumbDir = imageDir + "/thumbs";

          Tool.deleteFile(imageDir, attach.getSname()); 
          if (attach.getThumb() != null && !attach.getThumb().isBlank()) {
            Tool.deleteFile(thumbDir, attach.getThumb()); 
          }
        } else {
          Tool.deleteFile(boardDir + "/files", attach.getSname()); 
        }
      }
    }

    // 3단계: DB에서 해당 (tno, bno) 조건의 모든 첨부파일 레코드 일괄 삭제
    attachRepository.deleteByTnoAndBno(tno, bno);
  }
  
  
  /**
   * [내부 헬퍼 메서드: 폴더 자동 생성]
   * - 디렉터리가 존재하지 않는 경우 상위 경로까지 포함해서 디렉터리를 자동 생성합니다.
   * 
   * @param path 생성할 디렉터리 경로
   */
  private void createFolder(String path) {
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs(); // mkdirs(): 상위 폴더까지 존재하지 않으면 한 번에 다 생성
    }
  }
}