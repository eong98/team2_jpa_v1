package dev.jpa.allimio.attach;

import dev.jpa.allimio.tool.Tool;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * [첨부파일 전담 비즈니스 로직 서비스]
 */
@Service
@RequiredArgsConstructor
public class AttachService {

  private final AttachRepository attachRepository;

  /** 문의/FAQ 구분 조회용 (QA 테이블에 문의와 FAQ가 함께 있음) */
  private final dev.jpa.allimio.qa.QaRepository qaRepository;

  // [서버 루트 파일 저장 경로] 예: /allimio/attach
  private final String baseDir = Tool.getServerDir("attach");

  /**
   * [다중 파일 업로드 및 DB 저장]
   */
  /**
   * 첨부파일 저장 경로를 쓸 수 있는지 확인 — 공유폴더 서버가 꺼져 있거나 권한이 없으면 false.
   * 공유폴더(\\서버\...)는 꺼져 있을 때 Windows가 폴더 확인에 수십 초씩 걸리므로
   * SMB 포트(445)로 먼저 짧게 연결해 봅니다.
   */
  public boolean isStorageAvailable() {
    if (baseDir.startsWith("\\\\")) {
      String host = baseDir.substring(2, baseDir.indexOf('\\', 2));
      try (java.net.Socket socket = new java.net.Socket()) {
        socket.connect(new java.net.InetSocketAddress(host, 445), 1500);
      } catch (java.io.IOException e) {
        return false;
      }
    }
    File dir = new File(baseDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir.isDirectory() && dir.canWrite();
  }

  /** 저장 경로를 쓸 수 없을 때 — 컨트롤러가 503 + 안내 문구로 응답 */
  public static class AttachStorageException extends RuntimeException {
    public AttachStorageException(String message) {
      super(message);
    }
  }

  public static final String STORAGE_UNAVAILABLE_MESSAGE =
      "첨부파일 저장 경로에 접근할 수 없습니다.\n파일 서버(공유폴더)가 켜져 있는지 확인하거나 관리자에게 문의해주세요.";

  @Transactional
  public List<AttachDTO> saveAttachFiles(String tname, Long bno, List<MultipartFile> files) {
    // 저장 경로를 쓸 수 없으면 아무것도 저장하지 않고 중단 (예전엔 파일 없이 DB만 기록되던 문제)
    if (!isStorageAvailable()) {
      throw new AttachStorageException(STORAGE_UNAVAILABLE_MESSAGE);
    }
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

          sname = saveWithRandomName(mf, imageDir);
          checkSaved(imageDir, sname);

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
          sname = saveWithRandomName(mf, fileDir);
          checkSaved(fileDir, sname);
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
   * @param tname 게시판 테이블명 (QA, NOTICE, CCTV_ISSUE …) — 글번호(bno)는 게시판마다 따로 매겨지므로
   *              tname이 있어야 다른 게시판의 같은 번호 글 첨부파일과 섞이지 않음
   */
  public List<AttachDTO> getAttachList(Long bno, String tname) {
    if (bno == null) return List.of();

    Long tno;
    if (tname != null && !tname.isBlank()) {
      tno = attachRepository.findTnoByTname(tname).orElse(null);
    } else {
      // tname 없이 호출한 예전 화면 — 이 번호의 첨부가 한 게시판에만 있을 때만 그대로 보여줌
      List<Long> tnos = attachRepository.findTnosByBno(bno);
      if (tnos.size() > 1) {
        System.out.println("⚠ 첨부 목록 조회: bno=" + bno + "가 여러 게시판에 있어 tname이 필요합니다. " + tnos);
        return List.of();
      }
      tno = tnos.isEmpty() ? null : tnos.get(0);
    }
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
    Page<AttachDTO> result = page.map(AttachDTO::fromEntity);

    // 문의(QA) 첨부는 글이 FAQ인지 같이 내려줌 — 화면의 "게시글로 이동"을 FAQ/문의로 나누기 위함
    List<Long> qaNos = result.getContent().stream()
        .filter(d -> "QA".equalsIgnoreCase(d.getTname()))
        .map(AttachDTO::getBno)
        .distinct()
        .collect(Collectors.toList());
    if (!qaNos.isEmpty()) {
      java.util.Map<Long, String> faqMap = qaRepository.findAllById(qaNos).stream()
          .collect(Collectors.toMap(dev.jpa.allimio.qa.Qa::getNo, q -> q.getIsfaq() == null ? "N" : q.getIsfaq()));
      result.getContent().forEach(d -> {
        if ("QA".equalsIgnoreCase(d.getTname())) {
          d.setIsfaq(faqMap.getOrDefault(d.getBno(), "N"));
        }
      });
    }
    return result;
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
  public void deleteByTnoAndBno(Long bno, String tname) {
    if (bno == null) return;

    // 게시판마다 글번호가 따로라서 tname으로 게시판을 정해야 다른 게시판 첨부를 지우지 않음
    Long tno;
    if (tname != null && !tname.isBlank()) {
      tno = attachRepository.findTnoByTname(tname).orElse(null);
    } else {
      List<Long> tnos = attachRepository.findTnosByBno(bno);
      if (tnos.size() > 1) {
        System.out.println("⚠ 첨부 일괄삭제 건너뜀: bno=" + bno + "가 여러 게시판에 있어 tname이 필요합니다. " + tnos);
        return;
      }
      tno = tnos.isEmpty() ? null : tnos.get(0);
    }
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
  /** 저장된 파일이 실제로 있는지 확인 — 없으면 예외(트랜잭션 롤백 → DB에도 기록 안 됨) */
  /**
   * 무작위 파일명(UUID + 원래 확장자)으로 저장
   * - 원본 이름(예: cat04.jpg)으로 저장하면 비밀글 첨부도 주소를 추측해 열 수 있어서
   *   (/attach/storage/** 는 비회원 FAQ·공지 이미지 때문에 로그인 없이 열림)
   * - 화면 표시·다운로드 이름은 ATTACH.NAME(원본명)을 그대로 씀
   * - 같은 이름 파일이 덮어써지던 문제도 함께 없어짐
   * @return 저장한 파일명 (실패하면 빈 문자열 → checkSaved에서 오류)
   */
  private String saveWithRandomName(MultipartFile mf, String dir) {
    String original = mf.getOriginalFilename() == null ? "" : mf.getOriginalFilename();
    int dot = original.lastIndexOf('.');
    String ext = dot >= 0 ? original.substring(dot).toLowerCase() : "";
    String sname = UUID.randomUUID().toString().replace("-", "") + ext;

    try {
      createFolder(dir);
      try (var in = mf.getInputStream()) {
        Files.copy(in, new File(dir, sname).toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      return sname;
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  private void checkSaved(String dir, String sname) {
    if (sname == null || sname.isBlank() || !new File(dir, sname).isFile()) {
      throw new AttachStorageException(STORAGE_UNAVAILABLE_MESSAGE);
    }
  }

  private void createFolder(String path) {
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }
}