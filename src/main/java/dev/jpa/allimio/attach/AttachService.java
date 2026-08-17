package dev.jpa.allimio.attach;

import dev.jpa.allimio.tool.Tool;
import dev.jpa.allimio.tool.Upload;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

/**
 * [첨부파일 전담 비즈니스 로직 서비스]
 */
@Service
@RequiredArgsConstructor
public class AttachService {

  private final AttachRepository attachRepository;

  // [서버 루트 파일 저장 경로] 예: /allimio/attach
  private final String baseDir = Tool.getServerDir("attach");

  /**
   * [다중 파일 업로드 및 DB 저장]
   */
  @Transactional
  public List<AttachDTO> saveAttachFiles(String tname, Long bno, List<MultipartFile> files) {
    List<AttachDTO> resultList = new ArrayList<>();

    if (files == null || files.isEmpty()) {
      return resultList;
    }

    // ⭐️ Optional 처리: tname으로 tno 조회
    Long tno = attachRepository.findTnoByTname(tname).orElse(null);

    // [게시판 폴더 실물 경로] 예: /allimio/attach/notice
    String boardDir = baseDir + File.separator + tname;

    for (MultipartFile mf : files) {
      if (mf == null || mf.isEmpty()) continue;

      String name = mf.getOriginalFilename(); // 원본 파일명
      long fsize = mf.getSize();              // 파일 용량(Byte)

      if (fsize > 0 && Tool.checkUploadFile(name)) {
        boolean isImg = Tool.isImage(name);
        int type;
        String sname = "";
        String thumb = "";
        String purl = "";

        if (isImg) {
          type = 0;
          String imageDir = boardDir + "/images";
          String thumbDir = imageDir + "/thumbs";

          purl = "/attach/storage/" + tname + "/images";
          createFolder(thumbDir);

          sname = Upload.saveFileSpring(mf, imageDir);

          try {
            thumb = Tool.preview(imageDir, sname, 200, 150);
            if (thumb != null && !thumb.isBlank()) {
              File generatedThumb = new File(imageDir, thumb);
              File targetThumb = new File(thumbDir, thumb);
              Files.move(generatedThumb.toPath(), targetThumb.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

        } else {
          type = 1;
          String fileDir = boardDir + "/files";
          purl = "/attach/storage/" + tname + "/files";

          createFolder(fileDir);
          sname = Upload.saveFileSpring(mf, fileDir);
        }

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
            .cdate(Tool.getDate())
            .build();

        Attach saved = attachRepository.save(dto.toEntity());
        resultList.add(AttachDTO.fromEntity(saved));
      }
    }

    return resultList;
  }

  /**
   * [특정 게시글의 첨부파일 목록 조회]
   */
  public List<AttachDTO> getAttachList(Long bno) {
    if (bno == null) return List.of();

    Long tno = attachRepository.findTnoByBno(bno).orElse(null);
    if (tno == null) return List.of();

    List<Attach> list = attachRepository.findByTnoAndBno(tno, bno);
    return list.stream()
        .map(AttachDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * [단건 첨부파일 상세 조회]
   */
  public AttachDTO getAttachWithMenu(Long no) {
    if (no == null) return null;
    return attachRepository.findById(no)
        .map(AttachDTO::fromEntity)
        .orElse(null);
  }

  /**
   * [관리자/목록용 첨부파일 다중 조건 동적 검색 및 페이징 조회]
   */
  public Page<AttachDTO> searchAllAttach(String word, Long tno, Integer type, String cdate, Pageable pageable) {
    Page<Attach> page = attachRepository.searchAllAttach(word, tno, type, cdate, pageable);
    System.out.println(tno);
    return page.map(AttachDTO::fromEntity);
  }

  /**
   * [단일 첨부파일 삭제]
   */
  @Transactional
  public int deleteAttachFile(Long no) {
    if (no == null) return 0;

    Attach attach = attachRepository.findById(no).orElse(null);
    if (attach == null) return 0;

    if (attach.getSname() != null && !attach.getSname().isBlank()) {
      String boardDir = baseDir + File.separator + attach.getTname();

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

    attachRepository.delete(attach);
    return 1;
  }

  /**
   * [특정 게시글 삭제 시 연관된 모든 첨부파일 일괄 삭제]
   */
  @Transactional
  public void deleteByTnoAndBno(Long bno) {
    if (bno == null) return;

    Long tno = attachRepository.findTnoByBno(bno).orElse(null);
    if (tno == null) return;

    List<Attach> list = attachRepository.findByTnoAndBno(tno, bno);

    for (Attach attach : list) {
      String boardDir = baseDir + File.separator + attach.getTname();

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

    attachRepository.deleteByTnoAndBno(tno, bno);
  }

  /**
   * [내부 헬퍼 메서드: 폴더 자동 생성]
   */
  private void createFolder(String path) {
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }
}