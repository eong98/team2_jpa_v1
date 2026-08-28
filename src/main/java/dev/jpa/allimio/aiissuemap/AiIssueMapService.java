package dev.jpa.allimio.aiissuemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
  public AiIssueMapDTO read(long no) {

    Optional<AiIssueMap> optional =
        aiIssueMapRepository.findById(no);

    if (optional.isEmpty()) {
      return null;
    }

    return toDTO(optional.get());
  }


  /**
   * 원본 매장 도면 번호로 조회
   */
  public List<AiIssueMapDTO> listBySmno(long smno) {

    List<AiIssueMap> list =
        aiIssueMapRepository.findBySmno(smno);

    List<AiIssueMapDTO> dtoList = new ArrayList<>();

    for (AiIssueMap item : list) {
      dtoList.add(toDTO(item));
    }

    return dtoList;
  }


  /**
   * Entity -> DTO 변환
   */
  private AiIssueMapDTO toDTO(AiIssueMap item) {

    return new AiIssueMapDTO(
        item.getNo(),
        item.getMno(),
        item.getSmno(),
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