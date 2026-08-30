package dev.jpa.allimio.qa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.notice.NoticeDTO;
import dev.jpa.allimio.tool.Tool;

@Service
@Transactional(readOnly = true)
public class QaService {
  @Autowired
  QaRepository qaRepository;

  @Autowired
  MemberRepository memberRepository; // 이 방식이면 이렇게

  public QaService() {
    System.out.println("-> QaService created");
  }

  /**
   * 전체 회원 1:1 문의내역 전체 + 키워드 검색 (페이징)
   * 
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getAllQuestions(QaDTO.QaSearchRequest req, Pageable pageable) {
    return qaRepository.searchAllQuestions(req.getWord(), req.getType(), req.getStatus(), req.getMno(), pageable);
  }

  /**
   * 비회원 문의 목록 검색
   * 
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getSearchGuestQa(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Qa> qaPage = qaRepository.searchGuestList(req.getWord(), pageable);
    System.out.println("검색어" + req);
    return qaPage.map(QaDTO.QaResponse::fromEntity);
  }

  /***
   * 내 문의내역 전체조회 + 검색조회 (페이징)
   * 
   * @param mno
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getMyQuestions(QaDTO.QaSearchRequest req, Pageable pageable) {
    return qaRepository.searchMyQuestions(req.getWord(), req.getType(), req.getStatus(), req.getMno(), pageable);
  }

  /**
   * 문의글 상세페이지 조회 (통합: 회원 / 비회원 / 관리자) - 내 문의글만 조회 가능 (회원은 mno 일치, 비회원은 pw 일치) -
   * 관리자는 모든 글 조회 가능 (조회 시 답변대기(0) -> 확인중(1) 상태 변경)
   *
   * @param no  게시글 번호
   * @param mno 조회하려는 회원 번호 (nullable)
   * @param ano 조회하려는 관리자 번호 (nullable)
   * @param pw  비회원 문의글 비밀번호 (nullable)
   * @return QaResponse
   */
  @Transactional
  public QaDTO.QaResponse getQaDetail(Long no, Long mno, Long ano, String pw) {
    // 1. 게시글 존재 및 삭제 여부 확인
    Qa qa = qaRepository.findById(no).filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));

    QaDTO.QaNav prev = null;
    QaDTO.QaNav next = null;

    if ("N".equals(qa.getIsfaq())) {
      boolean isAdmin = (ano != null && ano > 0);

      boolean isGuestPost = qa.getMno() == null || qa.getMno() == 0;

      if (!isAdmin) {
        if (!isGuestPost) {
          // [회원 글] 비밀글(vmode='Y')일 때만 본인 확인
          if ("Y".equals(qa.getVmode())) {
            if (mno == null || !mno.equals(qa.getMno())) {
              throw new IllegalArgumentException("본인의 문의글만 조회할 수 있습니다.");
            }
          }
        } else {
          // [비회원 글] 비밀글(vmode='Y')일 때만 비밀번호 검증
          if ("Y".equals(qa.getVmode())) {
            if (pw == null || !pw.equals(qa.getPw())) {
              throw new IllegalArgumentException("비밀번호가 일치하지 않거나 비회원 문의글 접근 권한이 없습니다.");
            }
          }
        }
      }

      // 이전글 / 다음글 조회
      prev = qaRepository.findFirstByNoLessThanAndIsdelAndIsfaqOrderByNoDesc(no, "N", "N")
          .map(n -> new QaDTO.QaNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate())).orElse(null);

      next = qaRepository.findFirstByNoGreaterThanAndIsdelAndIsfaqOrderByNoAsc(no, "N", "N")
          .map(n -> new QaDTO.QaNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate())).orElse(null);

      // 관리자 조회 시 답변 대기(0) -> 확인중(1) 변경
      if (isAdmin && qa.getStatus() == 0) {
        qa.setStatus(1); // Dirty Checking으로 자동 update
      }
    }

    // 작성자 아이디 조회 (회원 글일 때만)
    String id = (qa.getMno() != null) ? memberRepository.findById(qa.getMno()).map(Member::getId).orElse(null) : null;

    return QaDTO.QaResponse.fromEntity(qa, prev, next, id);
  }

//
//  /**
//   * 비회원 문의 상세 조회 — 비밀번호가 일치할 때만 내용을 반환합니다.
//   * @param no 문의글 번호
//   * @param request 입력한 비밀번호
//   * @return 상세 정보, 비밀번호 불일치/대상 없음이면 null
//   */
//  public QaDTO.QaResponse getGuestDetail(Long no, QaDTO.GuestDetailRequest request) {
//    Optional<Qa> optional = qaRepository.findById(no);
//    if (optional.isEmpty()) return null;
//    QaDTO.QaNav prev = null;
//    QaDTO.QaNav next = null;
//
//
//    Qa qa = optional.get();
//    if (qa.getMno() != null) return null; // 회원 글은 이 API로 조회 불가
//    if (!qa.getPw().equals(request.getPw())) return null; // 비밀번호 불일치
//    
//
//    prev = qaRepository
//        .findFirstByNoLessThanAndIsdelAndIsfaqOrderByNoDesc(no, "N", "N")
//        .map(n -> new QaDTO.QaNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate()))
//        .orElse(null);
//
//    next = qaRepository
//        .findFirstByNoGreaterThanAndIsdelAndIsfaqOrderByNoAsc(no, "N", "N")
//        .map(n -> new QaDTO.QaNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate()))
//        .orElse(null);
//
//
//    // qa.getStatus()를 그대로 넘겨서, 위에서 바꾼 상태값(아직 flush 전)도 정확히 반영되게 함
//    return QaDTO.QaResponse.fromEntity(qa, prev, next, null);
//  }

  /**
   * 1:1 문의글 작성 (등록)
   * 
   * @param dto
   * @return 문의글 pk(no) 반환
   */
  @Transactional
  public QaDTO.QaResponse createQuestion(QaDTO.QCRequest dto) {
    Qa qa = dto.toEntity();
    Qa savedQa = qaRepository.save(qa);

    // 등록 직후 작성자 ID 조회하여 DTO로 변환
    String id = savedQa.getMno() != null ? memberRepository.findById(savedQa.getMno()).map(Member::getId).orElse(null)
        : null;

    return QaDTO.QaResponse.fromEntity(savedQa, null, null, id);
  }

  /**
   * 1:1 문의글 수정
   * 
   * @param updateDto
   */
  @Transactional
  public void updateQuestion(Long no, QaDTO.QCRequest updateDto) {
    // 1. 수정할 게시글 조회(삭제되지 않은 글)
    Qa qa = qaRepository.findById(no).filter(q -> "N".equals(q.getIsdel()))
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
   * 
   * @param replyDto
   */
  @Transactional
  public void replyToQuestion(Long no, QaDTO.QARequest replyDto) {
    // 1. 답변 작성할 게시글 조회
    Qa qa = qaRepository.findById(no).filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다. no=" + no));

    // 2. dto 메서드 호출 (ano, answer, adate = Tool.getDate(), status=2 변경)
    replyDto.applyTo(qa);
  }

  /**
   * FAQ 게시글 전체 + 키워드 검색 (페이징)
   * 
   * @param word
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getFaqs(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Qa> qaPage = qaRepository.searchFaqsAll(req.getWord(), req.getType(), req.getStatus(), pageable);
    return qaPage.map(QaDTO.QaResponse::fromEntity);
  }

  /**
   * FAQ 작성 (등록)
   * 
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
   * 
   * @param updateDto
   */
  @Transactional
  public void updateFAQ(Long no, QaDTO.FaqCRequest updateDto) {
    // 1. 수정할 게시글 조회(삭제되지 않은 글)
    Qa qa = qaRepository.findById(no).filter(q -> "N".equals(q.getIsdel()))
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
   * 
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
