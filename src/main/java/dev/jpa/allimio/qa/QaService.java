package dev.jpa.allimio.qa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.chatbot.session.ChatSessionRepository;
import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.tool.Tool;

/* ---------------------------------------------------------------------
   문의사항(QA) / FAQ 서비스

   상세·수정·삭제는 요청자에 따라 메서드를 나눕니다.
   - 관리자 : 로그인 JWT(ROLE_MANAGER) — 모든 글
   - 회원   : 로그인 JWT(ROLE_MEMBER)의 회원번호 — 비밀글은 본인 글만, 수정·삭제도 본인 글만
   - 비회원 : 게시글 비밀번호 확인 후 받은 임시 토큰(QaGuestTokenProvider) — 그 글 하나만

   예전처럼 화면이 보내는 accessNo/grade 헤더나 body의 mno는 믿지 않습니다
   (값만 바꾸면 남의 비밀글을 보거나 다른 회원 이름으로 글을 쓸 수 있었음).
--------------------------------------------------------------------- */
@Service
@Transactional(readOnly = true)
public class QaService {
  @Autowired
  QaRepository qaRepository;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  PasswordEncoder pwEncoder;

  @Autowired
  QaGuestTokenProvider guestTokenProvider;

  @Autowired
  ChatSessionRepository chatSessionRepository;

  public QaService() {
    System.out.println("-> QaService created");
  }

  /**
   * 전체 회원 1:1 문의내역 전체 + 키워드 검색 (페이징)
   *
   * @param req
   * @param pageable
   * @param isAdmin 관리자면 그대로, 아니면 남의 비밀글 내용·답변·이메일을 가림
   * @param viewerMno 조회하는 회원번호 (비회원/관리자는 null)
   * @return
   */
  public Page<QaDTO.QaResponse> getAllQuestions(QaDTO.QaSearchRequest req, Pageable pageable,
      boolean isAdmin, Long viewerMno) {
    Page<Object[]> result = qaRepository.searchAllQuestions(
        req.getWord(), req.getType(), req.getStatus(), req.getMno(), pageable);

    return result.map(row -> {
      Qa qa = (Qa) row[0];
      String id = (String) row[1];

      QaDTO.QaResponse res = QaDTO.QaResponse.fromEntity(qa, null, null, id);
      return isAdmin ? res : maskForOthers(res, viewerMno);
    });
  }

  /**
   * 비회원 문의 목록 검색 — 목록에서는 이메일과 비밀글 내용·답변을 내려주지 않음
   *
   * @param req
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getSearchGuestQa(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Qa> qaPage = qaRepository.searchGuestList(req.getWord(), pageable);
    return qaPage.map(qa -> maskForOthers(QaDTO.QaResponse.fromEntity(qa), null));
  }

  /***
   * 내 문의내역 전체조회 + 검색조회 (페이징)
   *
   * @param req mno는 컨트롤러에서 로그인 회원번호로 채움
   * @param pageable
   * @return
   */
  public Page<QaDTO.QaResponse> getMyQuestions(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Object[]> result = qaRepository.searchMyQuestions(req.getWord(), req.getType(), req.getStatus(), req.getMno(), pageable);

    return result.map(row -> {
      Qa qa = (Qa) row[0];
      String id = (String) row[1];

      return QaDTO.QaResponse.fromEntity(qa, null, null, id);
    });
  }

  // ==========================================
  // 상세 조회 (관리자 / 회원 / 비회원)
  // ==========================================

  /**
   * [관리자] 문의글·FAQ 상세 — 모든 글 조회 가능, 답변대기(0)면 확인중(1)으로 변경
   */
  @Transactional
  public QaDTO.QaResponse getAdminQaDetail(Long no) {
    Qa qa = findActive(no);
    qa.increaseVcnt();

    if ("N".equals(qa.getIsfaq()) && qa.getStatus() == 0) {
      qa.setStatus(1); // Dirty Checking으로 자동 update
    }
    return toDetail(qa);
  }

  /**
   * [회원] 문의글·FAQ 상세 — 비밀글은 본인 글만 (비회원 비밀글 포함 남의 비밀글은 403)
   */
  @Transactional
  public QaDTO.QaResponse getMemberQaDetail(Long no, Long mno) {
    Qa qa = findActive(no);
    boolean isOwner = isMemberOwner(qa, mno);

    if (isSecretQuestion(qa) && !isOwner) {
      throw new QaAccessException(403, "FORBIDDEN", "비밀글은 작성자만 볼 수 있습니다.");
    }
    qa.increaseVcnt();

    QaDTO.QaResponse res = toDetail(qa);
    if (!isOwner) {
      res.setGuestEmail(null); // 남의 글 이메일은 숨김
    }
    return res;
  }

  /**
   * [비회원] 문의글·FAQ 상세 — 비회원 비밀글은 임시 토큰이 있어야 조회 (회원 비밀글은 불가)
   *
   * @param token 비밀번호 확인 후 받은 임시 토큰 (없으면 null)
   */
  @Transactional
  public QaDTO.QaResponse getGuestQaDetail(Long no, String token) {
    Qa qa = findActive(no);
    boolean isOwner = isGuestPost(qa) && guestTokenProvider.isValid(token, no);

    if (isSecretQuestion(qa) && !isOwner) {
      if (!isGuestPost(qa)) {
        throw new QaAccessException(400, "FORBIDDEN", "회원 비밀글은 작성자만 볼 수 있습니다.");
      }
      throw new QaAccessException(400, "TOKEN_REQUIRED", "비밀번호 확인이 필요하거나 확인 시간(10분)이 지났습니다.");
    }
    qa.increaseVcnt();

    QaDTO.QaResponse res = toDetail(qa);
    if (!isOwner) {
      res.setGuestEmail(null);
    }
    return res;
  }

  /**
   * [비회원] 게시글 비밀번호 확인 → 이 글 전용 임시 토큰(10분) 발급
   * 5회 연속 실패 시 10분 잠금
   *
   * @return 임시 토큰
   */
  public String verifyGuestPw(Long no, String pw) {
    Qa qa = findActive(no);
    if (!isGuestPost(qa)) {
      throw new QaAccessException(400, "FORBIDDEN", "회원 문의글은 로그인 후 이용해 주세요.");
    }

    long lockedSeconds = guestTokenProvider.lockedSecondsLeft(no);
    if (lockedSeconds > 0) {
      throw new QaAccessException(429, "LOCKED",
          "비밀번호를 " + QaGuestTokenProvider.MAX_FAIL + "회 잘못 입력해 잠겼습니다. "
          + ((lockedSeconds + 59) / 60) + "분 후 다시 시도해 주세요.");
    }

    if (pw == null || !pwEncoder.matches(pw, qa.getPw())) {
      int left = guestTokenProvider.recordFail(no);
      if (left == 0) {
        throw new QaAccessException(429, "LOCKED",
            "비밀번호를 " + QaGuestTokenProvider.MAX_FAIL + "회 잘못 입력해 10분간 잠겼습니다.");
      }
      throw new QaAccessException(400, "PW_MISMATCH", "비밀번호가 일치하지 않습니다. (남은 횟수 " + left + "회)");
    }

    guestTokenProvider.clearFail(no);
    return guestTokenProvider.issue(no);
  }

  // ==========================================
  // 등록 / 수정 / 삭제
  // ==========================================

  /**
   * 1:1 문의글 작성 (등록)
   *
   * @param dto
   * @param loginMno 로그인 회원번호 (비회원이면 null) — body의 mno는 쓰지 않음
   * @return 등록된 문의글
   */
  @Transactional
  public QaDTO.QaResponse createQuestion(QaDTO.QCRequest dto, Long loginMno) {
    if (dto.getPw() == null || dto.getPw().isBlank()) {
      throw new QaAccessException(400, "PW_REQUIRED", "게시글 비밀번호를 입력해 주세요.");
    }

    String guestEmail = null;
    String id = null;

    if (loginMno == null) {
      // 비회원 문의 등록
      guestEmail = dto.getGuestEmail();
    } else {
      // 회원 문의 등록
      Member member = memberRepository.findById(loginMno).orElse(null);
      guestEmail = member != null ? member.getEmail() : null;
      id = member != null ? member.getId() : null;
    }

    Qa qa = Qa.builder()
        .mno(loginMno)
        .type(dto.getType())
        .title(dto.getTitle())
        .content(dto.getContent())
        .cdate(Tool.getDate())
        .pw(pwEncoder.encode(dto.getPw()))
        .vmode(dto.getVmode() != null ? dto.getVmode() : "N")
        .status(0) // 답변 대기
        .isdel("N")
        .isfaq("N")
        .fileyn(dto.getFileyn())
        .guestEmail(guestEmail)
        .build();

    Qa saved = qaRepository.save(qa);
    linkChatSession(saved.getNo(), dto.getSno(), loginMno, dto.getGno());

    return QaDTO.QaResponse.fromEntity(saved, null, null, id);
  }

  /**
   * 챗봇 상담에서 넘어와 등록한 문의면 상담(CHAT_SESSION.QNO)에 문의글 번호 연결
   * → 채팅방에 [이전 내용으로 다시 문의하기] 대신 [문의한 게시글로 이동] 표시
   * 본인 상담일 때만 (회원: 회원번호, 비회원: 브라우저 식별값 gno), 이미 연결된 상담은 그대로 둠
   */
  private void linkChatSession(Long qno, String sno, Long loginMno, String gno) {
    if (sno == null || sno.isBlank()) {
      return;
    }
    chatSessionRepository.findById(sno).ifPresent(session -> {
      boolean isOwner = loginMno != null
          ? loginMno.equals(session.getMno())
          : gno != null && !gno.isBlank() && gno.equals(session.getGno());
      if (isOwner && session.getQno() == null) {
        session.setQno(qno); // Dirty Checking으로 자동 update
      }
    });
  }

  /**
   * [회원] 본인 문의글 수정 — 로그인으로 본인 확인 (게시글 비밀번호 불필요)
   */
  @Transactional
  public void updateMemberQuestion(Long no, Long mno, QaDTO.QCRequest dto) {
    Qa qa = findActiveQuestion(no);
    if (!isMemberOwner(qa, mno)) {
      throw new QaAccessException(403, "FORBIDDEN", "본인의 문의글만 수정할 수 있습니다.");
    }
    // 회원 글 이메일은 회원정보에서 가져온 값 유지 (수정 화면엔 이메일 입력칸이 없음)
    applyQuestionUpdate(qa, dto, qa.getGuestEmail());
  }

  /**
   * [비회원] 문의글 수정 — 비밀번호 확인 후 받은 임시 토큰으로 본인 확인
   */
  @Transactional
  public void updateGuestQuestion(Long no, String token, QaDTO.QCRequest dto) {
    Qa qa = findActiveQuestion(no);
    requireGuestToken(qa, token);

    String email = dto.getGuestEmail() != null && !dto.getGuestEmail().isBlank()
        ? dto.getGuestEmail() : qa.getGuestEmail();
    applyQuestionUpdate(qa, dto, email);
  }

  /**
   * [회원] 본인 문의글 삭제 (소프트 삭제)
   */
  @Transactional
  public void deleteMemberQuestion(Long no, Long mno) {
    Qa qa = findActiveQuestion(no);
    if (!isMemberOwner(qa, mno)) {
      throw new QaAccessException(403, "FORBIDDEN", "본인의 문의글만 삭제할 수 있습니다.");
    }
    qa.delete(Tool.getDate());
    chatSessionRepository.clearQno(no); // 상담방 [문의한 게시글로 이동] → [다시 문의하기]로 되돌림
  }

  /**
   * [비회원] 문의글 삭제 (소프트 삭제) — 임시 토큰으로 본인 확인
   */
  @Transactional
  public void deleteGuestQuestion(Long no, String token) {
    Qa qa = findActiveQuestion(no);
    requireGuestToken(qa, token);
    qa.delete(Tool.getDate());
    chatSessionRepository.clearQno(no); // 상담방 [문의한 게시글로 이동] → [다시 문의하기]로 되돌림
  }

  /**
   * [관리자] 1:1 문의글 답변 작성/수정
   *
   * @param ano 로그인한 관리자 번호 (body의 ano는 쓰지 않음)
   */
  @Transactional
  public void replyToQuestion(Long no, Long ano, QaDTO.QARequest replyDto) {
    Qa qa = findActiveQuestion(no);
    qa.updateAnswer(ano, replyDto.getAnswer(), Tool.getDate()); // status=2(답변완료)
  }

  /**
   * FAQ 게시글 전체 + 키워드 검색 (페이징)
   *
   * @param req
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
    // 문의 등록과 같이 BCrypt로 암호화해서 저장 (예전엔 평문 저장 → 수정·삭제 때 matches 비교가 항상 실패)
    if (dto.getPw() != null && !dto.getPw().isBlank()) {
      qa.changePw(pwEncoder.encode(dto.getPw()));
    }
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
    Qa qa = findActive(no);

    // 2. 비밀번호 검증
    if (updateDto.getPw() == null || !pwEncoder.matches(updateDto.getPw(), qa.getPw())) {
      throw new QaAccessException(400, "PW_MISMATCH", "비밀번호가 일치하지 않습니다.");
    }

    // 3. QaDTO 내부 applyUpdateTo 사용 -> 수정
    updateDto.applyUpdateTo(qa);

  }

  /**
   * [관리자] FAQ 삭제 (실제 DB 삭제) — 게시글 비밀번호 확인
   * 1:1 문의글은 작성자(회원/비회원)만 삭제 (관리자 화면에도 문의글 삭제 버튼 없음)
   * 첨부파일은 화면에서 삭제 성공 후 따로 지움 (DELETE /attach/delete_by_bno/{no}?tname=QA)
   *
   * @param deleteDto
   */
  @Transactional
  public void deleteQuestion(QaDTO.DeleteRequest deleteDto) {
    // 1. 글번호 + 미삭제(N) 조건으로 조회 — FAQ만
    Qa qa = qaRepository.findByNoAndIsdel(deleteDto.getNo(), "N")
        .filter(q -> "Y".equals(q.getIsfaq()))
        .orElseThrow(QaAccessException::notFound);

    if (deleteDto.getPw() == null || !pwEncoder.matches(deleteDto.getPw(), qa.getPw())) {
      throw new QaAccessException(400, "PW_MISMATCH", "비밀번호가 일치하지 않습니다.");
    }

    // 2. 실제 삭제 (예전 코드는 문자열을 == 로 비교해서 FAQ도 소프트 삭제만 되던 버그)
    qaRepository.delete(qa);
  }

  // ==========================================
  // [관리자] 삭제된 문의 관리
  // ==========================================

  /**
   * [관리자] 삭제된(소프트 삭제) 1:1 문의 목록 — 검색어·유형·회원번호
   */
  public Page<QaDTO.QaResponse> getDeletedQuestions(QaDTO.QaSearchRequest req, Pageable pageable) {
    Page<Object[]> result = qaRepository.searchDeletedQuestions(req.getWord(), req.getType(), req.getMno(), pageable);

    return result.map(row -> QaDTO.QaResponse.fromEntity((Qa) row[0], null, null, (String) row[1]));
  }

  /**
   * [관리자] 삭제된 1:1 문의 영구 삭제 (DB에서 실제 삭제)
   * - 이미 삭제(ISDEL='Y')된 문의만 가능 — 살아 있는 글을 바로 지우지 않게
   * - 이 문의를 참조하는 챗봇 상담(CHAT_SESSION.QNO)은 연결만 끊음 (상담 기록은 유지)
   * - 첨부파일은 화면에서 삭제 성공 후 따로 지움 (DELETE /attach/delete_by_bno/{no}?tname=QA)
   */
  @Transactional
  public void purgeQuestion(Long no) {
    Qa qa = qaRepository.findById(no)
        .filter(q -> "Y".equals(q.getIsdel()) && "N".equals(q.getIsfaq()))
        .orElseThrow(() -> new QaAccessException(404, "NOT_FOUND", "삭제된 문의 목록에 없는 글입니다."));

    chatSessionRepository.clearQno(no);
    qaRepository.delete(qa);
  }

  // ==========================================
  // 내부 도우미
  // ==========================================

  /** 삭제되지 않은 글(문의·FAQ) 조회, 없으면 404 */
  private Qa findActive(Long no) {
    return qaRepository.findById(no).filter(q -> "N".equals(q.getIsdel()))
        .orElseThrow(QaAccessException::notFound);
  }

  /** 삭제되지 않은 1:1 문의글 조회 (FAQ는 이 경로로 수정·삭제·답변 불가) */
  private Qa findActiveQuestion(Long no) {
    Qa qa = findActive(no);
    if ("Y".equals(qa.getIsfaq())) {
      throw QaAccessException.notFound();
    }
    return qa;
  }

  /** 비회원 글 여부 */
  private boolean isGuestPost(Qa qa) {
    return qa.getMno() == null || qa.getMno() == 0;
  }

  /** 잠긴 1:1 문의글 여부 (FAQ는 비밀글 아님) */
  private boolean isSecretQuestion(Qa qa) {
    return "N".equals(qa.getIsfaq()) && "Y".equals(qa.getVmode());
  }

  /** 로그인 회원이 작성자인지 */
  private boolean isMemberOwner(Qa qa, Long mno) {
    return mno != null && !isGuestPost(qa) && mno.equals(qa.getMno());
  }

  /** 비회원 글 + 그 글 전용 임시 토큰인지 확인 */
  private void requireGuestToken(Qa qa, String token) {
    if (!isGuestPost(qa)) {
      throw new QaAccessException(400, "FORBIDDEN", "회원 문의글은 로그인 후 수정·삭제할 수 있습니다.");
    }
    if (!guestTokenProvider.isValid(token, qa.getNo())) {
      throw new QaAccessException(400, "TOKEN_REQUIRED", "비밀번호 확인이 필요하거나 확인 시간(10분)이 지났습니다.");
    }
  }

  /** 수정 내용 반영 (제목·내용·비밀글·유형·첨부여부·이메일) */
  private void applyQuestionUpdate(Qa qa, QaDTO.QCRequest dto, String email) {
    qa.updateQuestion(dto.getTitle(), dto.getContent(),
        dto.getVmode() != null ? dto.getVmode() : qa.getVmode(),
        dto.getType(), dto.getFileyn(), email);
  }

  /** 상세 응답 — 문의글이면 이전글/다음글, 회원 글이면 작성자 아이디 포함 */
  private QaDTO.QaResponse toDetail(Qa qa) {
    QaDTO.QaNav prev = null;
    QaDTO.QaNav next = null;

    if ("N".equals(qa.getIsfaq())) {
      // 목록에 보이는 순서(최신순) 기준 — 이전글: 목록에서 바로 위 글(더 최근), 다음글: 바로 아래 글(더 이전)
      prev = qaRepository.findAboveInList(qa.getCdate(), qa.getNo(), PageRequest.of(0, 1)).stream().findFirst()
          .map(n -> new QaDTO.QaNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate())).orElse(null);

      next = qaRepository.findBelowInList(qa.getCdate(), qa.getNo(), PageRequest.of(0, 1)).stream().findFirst()
          .map(n -> new QaDTO.QaNav(n.getNo(), n.getTitle(), n.getFileyn(), n.getVmode(), n.getCdate())).orElse(null);
    }

    // 작성자 아이디 조회 (회원 글일 때만)
    String id = (qa.getMno() != null) ? memberRepository.findById(qa.getMno()).map(Member::getId).orElse(null) : null;

    return QaDTO.QaResponse.fromEntity(qa, prev, next, id);
  }

  /** 목록용 — 본인 글이 아니면 이메일을 숨기고, 비밀글이면 내용·답변도 숨김 */
  private QaDTO.QaResponse maskForOthers(QaDTO.QaResponse res, Long viewerMno) {
    boolean isOwner = viewerMno != null && viewerMno.equals(res.getMno());
    if (isOwner) {
      return res;
    }
    res.setGuestEmail(null);
    if ("Y".equals(res.getVmode()) && !"Y".equals(res.getIsfaq())) {
      res.setContent(null);
      res.setAnswer(null);
    }
    return res;
  }

}
