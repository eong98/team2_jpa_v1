package dev.jpa.allimio.qa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.notice.NoticeDTO;
import dev.jpa.allimio.tool.Tool;

@Service
@Transactional(readOnly = true)
public class QaService {
  @Autowired
  QaRepository qaRepository;
  
  public QaService () {
    System.out.println("-> QaService created");
  }
  

  /**
   * 전체 회원 1:1 문의내역 전체 + 키워드 검색 (페이징)
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getAllQuestions(QaDTO.QaSearchRequest req, Pageable pageable) {
    // Repository로 DTO 안의 필드값들을 전달
    Page<Qa> qaPage = qaRepository.searchAllQuestions(
        req.getWord(),
        req.getType(),
        req.getStatus(),
        req.getMno(),
        pageable
    );
    return qaPage.map(QaDTO.QaResponse::fromEntity);
  }

  /***
   * 내 문의내역 전체조회 + 검색조회 (페이징)
   * @param mno
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getMyQuestions(QaDTO.QaSearchRequest req, Pageable pageable) {
      Page<Qa> qaPage = qaRepository.searchMyQuestions(
      req.getWord(),
      req.getType(),
      req.getStatus(),
      req.getMno(),
      pageable
    );
    return qaPage.map(QaDTO.QaResponse::fromEntity);
  }

  
  /**
   * 문의글 상세페이지 조회
   * - 내 문의글만 조회가능
   * - 관리자는 전부 조회가능
   * - 관리자가 조회시 답변대기 -> 확인중으로 status 변경* @param no  게시글 번호
   * @param mno 조회하려는 회원 번호 (nullable)
   * @param ano 조회하려는 관리자 번호 (nullable)
   * @return Qa
   */
  @Transactional
  public QaDTO.QaResponse getQa(Long no, Long mno, Long ano) {
    // 1. 게시글 존재 및 미삭제 여부 확인
    Qa qa = qaRepository.findById(no)
        .filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));
    QaDTO.QaNav prev = null;
    QaDTO.QaNav next = null;

    // isfaq === N (문의글)
    if ("N".equals(qa.getIsfaq())) {
      boolean isAdmin = (ano != null && ano > 0);

      // 2. 관리자가 아니고 비밀글인 경우 경우 작성자 본인 확인
      if (!isAdmin && "Y".equals(qa.getVmode())) {
        if (mno == null || !mno.equals(qa.getMno())) {
          throw new IllegalArgumentException("본인의 문의글만 조회할 수 있습니다.");
        }
      }
      

      // 3. 이전글 / 다음글 조회 (미삭제만 대상)
      prev = qaRepository
          .findFirstByNoLessThanAndIsdelAndIsfaqOrderByNoDesc(no, "N", "N")
          .map(n -> new QaDTO.QaNav (n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate()))
          .orElse(null);

      next = qaRepository
          .findFirstByNoGreaterThanAndIsdelAndIsfaqOrderByNoAsc(no, "N", "N")
          .map(n -> new QaDTO.QaNav (n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate()))
          .orElse(null);
      

      // 3. 관리자 조회 시 상태값 변경 (0: 답변대기 -> 1: 확인중)
      if (isAdmin && qa.getStatus() == 0) {
        qa.setStatus(1); // 엔티티 상태 필드 직접 변경 (Dirty Checking 적용)
      }
      
    }

    // 4. DTO 변환 후 반환
    return QaDTO.QaResponse.fromEntity(qa, prev, next);
  }
    
  /**
   * 1:1 문의글 작성 (등록)
   * @param dto
   * @return 문의글 pk(no) 반환
   */
  @Transactional
  public Long createQuestion(QaDTO.QCRequest dto) {
    Qa qa = dto.toEntity();
    
    Qa savedQa = qaRepository.save(qa);
    return savedQa.getNo();
  }
//  게시글 반환용
//  public QaDTO.QaResponse createQa(QaDTO.QCRequest request) {
//    Qa qa = request.toEntity();
//    Qa savedQa = qaRepository.save(qa);
//    return QaDTO.QaResponse.fromEntity(savedQa);
//  }
  
  
  /**
   * 1:1 문의글 수정
   * @param updateDto
   */
  @Transactional
  public void updateQuestion(Long no, QaDTO.QCRequest updateDto) {
    // 1. 수정할 게시글 조회(삭제되지 않은 글)
    Qa qa = qaRepository.findById(no)
        .filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));
    
    // 2. 비밀번호 검증
    if (!qa.matchPw(updateDto.getPw())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    
    // 3. QaDTO 내부 applyUpdateTo 사용 -> 수정
    updateDto.applyUpdateTo(qa);
    
  }
  
  /**
   * 1:1 문의글 답변 작성/수정
   * @param replyDto
   */
  @Transactional
  public void replyToQuestion(Long no, QaDTO.QARequest replyDto) {
    // 1. 답변 작성할 게시글 조회
    Qa qa = qaRepository.findById(no)
        .filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));
    
    // 2. dto 메서드 호출 (ano, answer, adate = Tool.getDate(), status=2 변경)
    replyDto.applyTo(qa);
  }
  

  /**
   * FAQ 게시글 전체 + 키워드 검색 (페이징)
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getFaqs(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Qa> qaPage = qaRepository.searchFaqsAll(
        req.getWord(),
        req.getType(),
        req.getStatus(),
        pageable
    );
    return qaPage.map(QaDTO.QaResponse::fromEntity);
  }

  /**
   * FAQ 작성 (등록)
   * @param dto
   * @return FAQ pk(no) 반환
   */
  @Transactional
  public Long createFAQ(QaDTO.FaqCRequest dto) {
    Qa qa = dto.toEntity();
    System.out.println(qa.getIsfaq());
    Qa savedQa = qaRepository.save(qa);
    return savedQa.getNo();
  }

  /**
   * FAQ 수정
   * @param updateDto
   */
  @Transactional
  public void updateFAQ(Long no, QaDTO.FaqCRequest updateDto) {
    // 1. 수정할 게시글 조회(삭제되지 않은 글)
    Qa qa = qaRepository.findById(no)
        .filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));
    
    // 2. 비밀번호 검증
    if (!qa.matchPw(updateDto.getPw())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    
    // 3. QaDTO 내부 applyUpdateTo 사용 -> 수정
    updateDto.applyUpdateTo(qa);
    
  }
  
  
  /**
   * 1:1 문의글(소프트 삭제) /FAQ(실제 DB 삭제) 삭제
   * @param deleteDto
   */
  @Transactional
  public void deleteQuestion(QaDTO.DeleteRequest deleteDto) {
    // 1. 글번호 + 비밀번호 + 미삭제(N) 조건으로 조회
    Qa qa = qaRepository.findByNoAndPwAndIsdel(deleteDto.getNo(), deleteDto.getPw(), "N")
            .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않거나 비밀번호가 일치하지 않습니다."));

    if (qa.getIsfaq() == "Y") {
      // 2. 분류 구분이 FAQ 인 경우 실제 데이터 삭제
      qaRepository.delete(qa);
    } else {
      // 3. 엔티티 도메인 메서드 호출 (FAQ 가 아닌 것들은 데이터 삭제 안함)
      qa.delete(Tool.getDate());
    }
    
  }
  

  
  
  
}
