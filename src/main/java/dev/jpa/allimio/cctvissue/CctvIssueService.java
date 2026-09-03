package dev.jpa.allimio.cctvissue;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.cctvissuecode.CctvIssueCodeRepository;
import dev.jpa.allimio.tool.Tool;

@Service
public class CctvIssueService {
  @Autowired
  CctvIssueRepository cctvIssueRepository;

  // 이상행동유형코드(CODE) 유효성 검증용. FastAPI(jetson_worker → modules/cctv_issue.py)는
  // Spring API를 거치지 않고 Oracle에 직접 INSERT하므로 거기서도 별도로 검증하지만
  // (codes_cache.py), dbms 관리자 화면 등 Spring API를 직접 호출하는 경로도 있어서 여기서도
  // 한 번 더 막아줍니다 - CCTV_ISSUE_CODE에 없는 코드가 저장되는 걸 원천 차단.
  @Autowired
  CctvIssueCodeRepository cctvIssueCodeRepository;

  public CctvIssueService() {

  }

  private void validateCode(String code) {
    if (code == null || !cctvIssueCodeRepository.existsById(code)) {
      throw new IllegalArgumentException("등록되지 않은 이상행동유형코드입니다: " + code);
    }
  }

  public CctvIssue save(CctvIssueDTO cctvIssueDTO) {
    validateCode(cctvIssueDTO.getCode());
    cctvIssueDTO.setCdate(Tool.getDate());
    CctvIssue cctvIssue = cctvIssueRepository.save(cctvIssueDTO.toEntity());

    return cctvIssue;
  }

  public List<CctvIssue> findAll() {
    List<CctvIssue> list = cctvIssueRepository.findAll();

    return list;
  }

  /**
   * 관리자 목록 검색 - 조건은 전부 선택 사항
   * (첨부파일 유무(hasAttach)를 EXISTS 서브쿼리로 이 쿼리 한 번에 같이 계산 - N+1 쿼리 없음)
   */
  public Page<CctvIssueListDTO> search(Long cno, String code, Integer state, String noticeyn,
      String keyword, String cdateFrom, String cdateTo, Pageable pageable) {
    return cctvIssueRepository.search(cno, code, state, noticeyn, keyword, cdateFrom, cdateTo, pageable);
  }

  /**
   * 사용자(매장) 화면용 검색 - sno(매장 번호)로 소유 CCTV의 이슈만 조회
   * (첨부파일 유무(hasAttach)를 EXISTS 서브쿼리로 이 쿼리 한 번에 같이 계산 - N+1 쿼리 없음)
   */
  public Page<CctvIssueListDTO> searchByShop(long sno, Long cno, String code, Integer state, String noticeyn,
      String keyword, String cdateFrom, String cdateTo, Pageable pageable) {
    return cctvIssueRepository.searchByShop(sno, cno, code, state, noticeyn, keyword, cdateFrom, cdateTo, pageable);
  }

  public CctvIssue findById(long pk) {
    Optional<CctvIssue> optional = cctvIssueRepository.findById(pk);

    if (optional.isPresent()) {
      CctvIssue cctvIssue = optional.get();
      return cctvIssue;
    }
    return null;
  }

  public CctvIssue update(CctvIssueDTO cctvIssueDTO) {
    validateCode(cctvIssueDTO.getCode());
    CctvIssue cctvIssue = cctvIssueRepository.findById(cctvIssueDTO.getNo()).get();
    cctvIssue.setCno(cctvIssueDTO.getCno());
    cctvIssue.setMno(cctvIssueDTO.getMno());
    cctvIssue.setCode(cctvIssueDTO.getCode());
    cctvIssue.setState(cctvIssueDTO.getState());
    cctvIssue.setComnet(cctvIssueDTO.getComnet());
    cctvIssue.setReliability(cctvIssueDTO.getReliability());
    cctvIssue.setPdate(cctvIssueDTO.getPdate());
    cctvIssue.setNoticeyn(cctvIssueDTO.getNoticeyn());

    CctvIssue savedEntity = cctvIssueRepository.save(cctvIssue);

    return savedEntity;
  }

  public boolean deleteById(long pk) {
    Optional<CctvIssue> optional = cctvIssueRepository.findById(pk);

    if (optional.isPresent()) {
      cctvIssueRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}