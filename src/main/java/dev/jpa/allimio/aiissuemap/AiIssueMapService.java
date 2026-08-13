package dev.jpa.allimio.aiissuemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

/**
 * AI 이슈 도면 Service
 */
@Service
public class AiIssueMapService {

  @Autowired
  private AiIssueMapRepository aiIssueMapRepository;


  /**
   * 전체 조회
   */
  public List<AiIssueMapDTO> list() {

    List<AiIssueMap> list = aiIssueMapRepository.findAll();
    List<AiIssueMapDTO> dtoList = new ArrayList<>();

    for (AiIssueMap item : list) {
      dtoList.add(toDTO(item));
    }

    return dtoList;
  }


  /**
   * 단건 조회
   */
  public AiIssueMapDTO read(long aiissueMapno) {

    Optional<AiIssueMap> optional =
        aiIssueMapRepository.findById(aiissueMapno);

    if (optional.isEmpty()) {
      return null;
    }

    return toDTO(optional.get());
  }


  /**
   * 원본 매장 도면 번호로 조회
   */
  public List<AiIssueMapDTO> listByShopmapno(long shopmapno) {

    List<AiIssueMap> list =
        aiIssueMapRepository.findByShopmapno(shopmapno);

    List<AiIssueMapDTO> dtoList = new ArrayList<>();

    for (AiIssueMap item : list) {
      dtoList.add(toDTO(item));
    }

    return dtoList;
  }


  /**
   * AI 도면 데이터 등록
   */
  public AiIssueMapDTO create(AiIssueMapDTO dto) {

    dto.setCdate(Tool.getDate());

    AiIssueMap saved =
        aiIssueMapRepository.save(dto.toEntity());

    return toDTO(saved);
  }


  /**
   * AI 이미지 생성 결과 수정
   */
  public boolean updateResult(
      long aiissueMapno,
      String fsaved,
      int status,
      String err) {

    Optional<AiIssueMap> optional =
        aiIssueMapRepository.findById(aiissueMapno);

    if (optional.isEmpty()) {
      return false;
    }

    AiIssueMap item = optional.get();

    item.setFsaved(fsaved);
    item.setStatus(status);
    item.setErr(err);

    aiIssueMapRepository.save(item);

    return true;
  }


  /**
   * 삭제
   */
  public boolean delete(long aiissueMapno) {

    if (!aiIssueMapRepository.existsById(aiissueMapno)) {
      return false;
    }

    aiIssueMapRepository.deleteById(aiissueMapno);

    return true;
  }


  /**
   * Entity -> DTO
   */
  private AiIssueMapDTO toDTO(AiIssueMap item) {

    return new AiIssueMapDTO(
        item.getAiissueMapno(),
        item.getMno(),
        item.getShopmapno(),
        item.getXpos(),
        item.getYpos(),
        item.getColor(),
        item.getFsaved(),
        item.getStatus(),
        item.getErr(),
        item.getCdate()
    );
  }
}