package dev.jpa.allimio.board.qa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.tool.Tool;

@Service
@Transactional(readOnly = true)
public class QaService {
  @Autowired
  QaRepository qaRepository;
  
  public QaService () {
    System.out.println("-> QaService created");
  }
  
  
  /***
   * 내 문의내역 전체조회 + 검색조회 (페이징)
   * @param mno
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getMyQuestions(Long mno, String word, Pageable pageable) {
    Page<Qa> qaPage = qaRepository.searchMyQuestions(mno, word, pageable);
    return qaPage.map(QaDTO.QaResponse::fromEntity);
  }
  
  
  public Qa getQa(Long no) {
    return qaRepository.findById(no)
            .filter(q -> "N".equals(q.getIsdel()))
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));
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
   * 전체 회원 1:1 문의내역 전체 + 키워드 검색 (페이징)
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getAllQuestionsAdmin(QaDTO.QaSearchRequest req, Pageable pageable) {
    // Repository로 DTO 안의 필드값들을 전달
    Page<Qa> qaPage = qaRepository.searchAllQuestionsAdmin(
        req.getWord(),
        req.getType(),
        req.getStatus(),
        req.getMno(),
        pageable
    );
    return qaPage.map(QaDTO.QaResponse::fromEntity);
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
  public Page<QaDTO.QaResponse> getFaqsAdmin(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Qa> qaPage = qaRepository.searchFaqsAdmin(
        req.getWord(),
        req.getType(),
        req.getStatus(),
        req.getMno(),
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

    if (qa.getType() == 4) {
      // 2. 분류 구분이 FAQ 인 경우 실제 데이터 삭제
      qaRepository.delete(qa);
    } else {
      // 3. 엔티티 도메인 메서드 호출 (FAQ 가 아닌 것들은 데이터 삭제 안함)
      qa.delete(Tool.getDate());
    }
    
  }
  

  
  
  
}
