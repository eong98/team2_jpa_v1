package dev.jpa.allimio.cctvissuecode;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class CctvIssueCodeService {
  @Autowired
  CctvIssueCodeRepository cctvIssueCodeRepository;

  public CctvIssueCodeService() {

  }

  public CctvIssueCode save(CctvIssueCodeDTO cctvIssueCodeDTO) {
    cctvIssueCodeDTO.setCdate(Tool.getDate());
    CctvIssueCode cctvIssueCode = cctvIssueCodeRepository.save(cctvIssueCodeDTO.toEntity());

    return cctvIssueCode;
  }

  public List<CctvIssueCode> findAll() {
    List<CctvIssueCode> list = cctvIssueCodeRepository.findAll();

    return list;
  }

  /** 사용 중(useYn='Y')인 코드만 정렬 순서대로 - CctvIssueList.tsx 등 화면 드롭다운/라벨 매핑용 */
  public List<CctvIssueCode> findAllUse() {
    return cctvIssueCodeRepository.findAllByUseYnOrderByOrdAsc("Y");
  }

  /** 관리자 코드관리 목록 검색 - 조건은 전부 선택 사항 */
  public Page<CctvIssueCode> search(String useYn, String keyword, Pageable pageable) {
    return cctvIssueCodeRepository.search(useYn, keyword, pageable);
  }

  public CctvIssueCode findById(String pk) {
    Optional<CctvIssueCode> optional = cctvIssueCodeRepository.findById(pk);

    if (optional.isPresent()) {
      CctvIssueCode cctvIssueCode = optional.get();
      return cctvIssueCode;
    }
    return null;
  }

  public CctvIssueCode update(CctvIssueCodeDTO cctvIssueCodeDTO) {
    CctvIssueCode cctvIssueCode = cctvIssueCodeRepository.findById(cctvIssueCodeDTO.getCode()).get();
    cctvIssueCode.setCodeName(cctvIssueCodeDTO.getCodeName());
    cctvIssueCode.setDescription(cctvIssueCodeDTO.getDescription());
    cctvIssueCode.setSeverity(cctvIssueCodeDTO.getSeverity());
    cctvIssueCode.setOrd(cctvIssueCodeDTO.getOrd());
    cctvIssueCode.setUseYn(cctvIssueCodeDTO.getUseYn());

    CctvIssueCode savedEntity = cctvIssueCodeRepository.save(cctvIssueCode);

    return savedEntity;
  }

  public boolean deleteById(String pk) {
    Optional<CctvIssueCode> optional = cctvIssueCodeRepository.findById(pk);

    if (optional.isPresent()) {
      cctvIssueCodeRepository.deleteById(pk);
      return true;
    }
    return false;
  }

}
