package dev.jpa.allimio.survey;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.surveyanswer.SurveyAnswer;
import dev.jpa.allimio.surveyanswer.SurveyAnswerDTO;
import dev.jpa.allimio.surveyanswer.SurveyAnswerRepository;
import dev.jpa.allimio.surveyquestion.SurveyQuestion;
import dev.jpa.allimio.surveyquestion.SurveyQuestionDTO;
import dev.jpa.allimio.surveyquestion.SurveyQuestionRepository;
import dev.jpa.allimio.surveyresponse.SurveyResponse;
import dev.jpa.allimio.surveyresponse.SurveyResponseDTO;
import dev.jpa.allimio.surveyresponse.SurveyResponseRepository;

/**
 * 설문조사 전체 비즈니스 로직.
 *
 * <p>설문·문항 등록, 점주 답변 제출, 관리자 확인,
 *  관리자 확인과 DTO 변환을 담당한다.</p>
 */
@Service
@Transactional
public class SurveyService {

    /** DB VARCHAR2(30) 날짜 컬럼에 저장할 공통 형식. */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 허용하는 문항유형. */
    private static final Set<String> QUESTION_TYPES =
            Set.of("SINGLE", "MULTIPLE", "TEXT", "SCORE");

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;

    public SurveyService(
            SurveyRepository surveyRepository,
            SurveyQuestionRepository questionRepository,
            SurveyResponseRepository responseRepository,
            SurveyAnswerRepository answerRepository) {

        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
    }

    /**
     * 설문 기본정보와 문항 목록을 한 번에 등록한다.
     */
    public SurveyDTO createSurvey(SurveyDTO dto) {
        validateSurveyDTO(dto);

        Survey survey = new Survey();
        applySurveyValues(survey, dto);
        survey.setCdate(now());

        List<SurveyQuestionDTO> questionDTOs =
                dto.getQuestions() == null
                        ? Collections.emptyList()
                        : dto.getQuestions();

        int defaultSeq = 1;
        for (SurveyQuestionDTO questionDTO : questionDTOs) {
            SurveyQuestion question =
                    createQuestionEntity(questionDTO, defaultSeq);
            survey.addQuestion(question);
            defaultSeq++;
        }

        Survey saved = surveyRepository.save(survey);
        return toSurveyDTO(saved);
    }

    /**
     * 설문 목록을 최신 설문번호순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<SurveyDTO> getSurveyList() {
        return surveyRepository.findAllByOrderByNoDesc()
                .stream()
                .map(this::toSurveyDTOWithoutQuestions)
                .collect(Collectors.toList());
    }

    /**
     * 현재 시각이 시작일과 종료일 사이인 설문 목록을 조회한다.
     *
     * <p>날짜 문자열이 yyyy-MM-dd HH:mm:ss 형식이면
     * 문자열 비교만으로도 시간순 비교가 가능하다.</p>
     */
    @Transactional(readOnly = true)
    public List<SurveyDTO> getActiveSurveyList() {
        String current = now();

        return surveyRepository.findAllByOrderByNoDesc()
                .stream()
                .filter(survey ->
                        survey.getStartDate().compareTo(current) <= 0
                        && survey.getEndDate().compareTo(current) >= 0)
                .map(this::toSurveyDTOWithoutQuestions)
                .collect(Collectors.toList());
    }

    /**
     * 설문 문항을 포함한 상세정보를 조회한다.
     */
    @Transactional(readOnly = true)
    public SurveyDTO getSurvey(Long surveyNo) {
        return toSurveyDTO(findSurveyWithQuestions(surveyNo));
    }

    /**
     * 설문 기본정보와 문항 목록을 수정한다.
     *
     * <p>기존 문항을 모두 지우고 요청받은 문항 목록으로 교체한다.
     * 이미 응답이 있는 설문에서는 문항 교체가 데이터 정합성을 해칠 수 있으므로
     * 응답 존재 시 수정하지 못하도록 막는다.</p>
     */
    public SurveyDTO updateSurvey(Long surveyNo, SurveyDTO dto) {
        validateSurveyDTO(dto);

        if (!responseRepository
                .findBySurveyNoOrderByNoDesc(surveyNo)
                .isEmpty()) {
            throw new IllegalStateException(
                    "이미 제출된 응답이 있는 설문은 수정할 수 없습니다.");
        }

        Survey survey = findSurveyWithQuestions(surveyNo);
        
        //  설문 기본정보 수정
        applySurveyValues(survey, dto);
        
        
        // 기존 질문 제거
        survey.getQuestions().clear();


        // orphanRemoval에 의한 DELETE를 DB에 먼저 반영
        surveyRepository.flush();

        //  수정 화면에서 받은 질문 목록
        List<SurveyQuestionDTO> questionDTOs =
                dto.getQuestions() == null
                        ? Collections.emptyList()
                        : dto.getQuestions();

        // 새 질문 등록
        int defaultSeq = 1;
        
        for (SurveyQuestionDTO questionDTO : questionDTOs) {
            SurveyQuestion question =
                    createQuestionEntity(questionDTO, defaultSeq);
          
            
            survey.addQuestion(question);
            defaultSeq++;
        }

        return toSurveyDTO(surveyRepository.save(survey));
    }

    /**
     * 설문을 삭제한다.
     * 연관된 문항·응답·실제답변도 Cascade 설정에 따라 함께 삭제된다.
     */
    public void deleteSurvey(Long surveyNo) {
        Survey survey = surveyRepository.findById(surveyNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "설문을 찾을 수 없습니다: " + surveyNo));

        surveyRepository.delete(survey);
    }

    /**
     * 점주가 작성한 설문 답변을 제출한다.
     *
     * <p>SURVEYRESPONSE 한 행을 먼저 만들고,
     * 각 문항의 실제 답변을 SURVEYANSWER에 저장한다.
     * 메서드 전체가 하나의 트랜잭션이므로 일부 저장 실패 시 전부 롤백된다.</p>
     */
    public SurveyResponseDTO submitResponse(
            Long surveyNo,
            SurveyResponseDTO dto) {

        if (dto == null || dto.getMemberNo() == null) {
            throw new IllegalArgumentException(
                    "응답한 점주 회원번호는 필수입니다.");
        }

        Survey survey = findSurveyWithQuestions(surveyNo);
        validateSurveyPeriod(survey);

        if (responseRepository.existsBySurveyNoAndMemberNo(
                surveyNo,
                dto.getMemberNo())) {
            throw new IllegalStateException(
                    "이미 제출한 설문입니다.");
        }

        List<SurveyAnswerDTO> answerDTOs =
                dto.getAnswers() == null
                        ? Collections.emptyList()
                        : dto.getAnswers();

        validateSubmittedAnswers(survey, answerDTOs);

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setMemberNo(dto.getMemberNo());
        response.setCheckYn("N");
        response.setCheckDate(null);
        response.setCdate(now());

        for (SurveyAnswerDTO answerDTO : answerDTOs) {
            SurveyQuestion question = questionRepository
                    .findById(answerDTO.getQuestionNo())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "설문 문항을 찾을 수 없습니다: "
                                            + answerDTO.getQuestionNo()));

            if (!question.getSurvey().getNo().equals(surveyNo)) {
                throw new IllegalArgumentException(
                        "해당 설문에 포함되지 않은 문항입니다: "
                                + question.getNo());
            }

            validateAnswerByQuestionType(question, answerDTO);

            SurveyAnswer answer = new SurveyAnswer();
            answer.setQuestion(question);
            answer.setAtext(normalizeText(answerDTO.getAtext()));
            answer.setCdate(now());

            response.addAnswer(answer);
        }

        SurveyResponse saved = responseRepository.save(response);
        return toResponseDTO(saved);
    }

    /**
     * 특정 설문의 전체 점주 응답 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<SurveyResponseDTO> getResponseList(Long surveyNo) {
        if (!surveyRepository.existsById(surveyNo)) {
            throw new IllegalArgumentException(
                    "설문을 찾을 수 없습니다: " + surveyNo);
        }

        return responseRepository
                .findBySurveyNoOrderByNoDesc(surveyNo)
                .stream()
                .map(this::toResponseSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * 응답 한 건과 문항별 실제 답변을 조회한다.
     */
    @Transactional(readOnly = true)
    public SurveyResponseDTO getResponse(Long responseNo) {
        return toResponseDTO(findResponseWithAnswers(responseNo));
    }

    /**
     * 점주 회원번호와 설문번호로 본인의 제출 결과를 조회한다.
     */
    @Transactional(readOnly = true)
    public SurveyResponseDTO getMemberResponse(
            Long surveyNo,
            Long memberNo) {

        SurveyResponse response = responseRepository
                .findBySurveyNoAndMemberNo(surveyNo, memberNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 회원의 설문 응답을 찾을 수 없습니다."));

        return toResponseDTO(response);
    }

    /**
     * 관리자가 점주의 설문 전체 응답을 확인한 상태로 변경한다.
     */
    public SurveyResponseDTO checkResponse(Long responseNo) {
        SurveyResponse response =
                findResponseWithAnswers(responseNo);

        response.setCheckYn("Y");
        response.setCheckDate(now());

        return toResponseDTO(responseRepository.save(response));
    }


    /** 설문 엔티티 조회 공통 메서드. */
    private Survey findSurveyWithQuestions(Long surveyNo) {
        return surveyRepository.findWithQuestionsByNo(surveyNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "설문을 찾을 수 없습니다: " + surveyNo));
    }

    /** 응답과 실제답변 조회 공통 메서드. */
    private SurveyResponse findResponseWithAnswers(Long responseNo) {
        return responseRepository.findWithAnswersByNo(responseNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "설문 응답을 찾을 수 없습니다: "
                                        + responseNo));
    }

    /** DTO 설문 기본값을 엔티티에 반영한다. */
    private void applySurveyValues(Survey survey, SurveyDTO dto) {
        survey.setManagerNo(dto.getManagerNo());
        survey.setTitle(dto.getTitle().trim());
        survey.setDetail(normalizeText(dto.getDetail()));
        survey.setStartDate(dto.getStartDate().trim());
        survey.setEndDate(dto.getEndDate().trim());
    }

    /** 문항 DTO를 엔티티로 변환하고 기본값을 적용한다. */
    private SurveyQuestion createQuestionEntity(
            SurveyQuestionDTO dto,
            int defaultSeq) {

        validateQuestionDTO(dto);

        SurveyQuestion question = new SurveyQuestion();
        question.setQtext(dto.getQtext().trim());
        question.setQtype(dto.getQtype().trim().toUpperCase());
        question.setQoptions(normalizeText(dto.getQoptions()));
        question.setRequiredYn(
                dto.getRequiredYn() == null
                        ? "Y"
                        : dto.getRequiredYn().trim().toUpperCase());
        question.setSeqNo(
                dto.getSeqNo() == null
                        ? defaultSeq
                        : dto.getSeqNo());
        question.setCdate(now());

        return question;
    }

    /** 설문 요청값을 검증한다. */
    private void validateSurveyDTO(SurveyDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "설문 데이터가 없습니다.");
        }

        if (dto.getManagerNo() == null) {
            throw new IllegalArgumentException(
                    "설문 작성 관리자 회원번호는 필수입니다.");
        }

        if (dto.getTitle() == null
                || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "설문 제목은 필수입니다.");
        }

        if (dto.getStartDate() == null
                || dto.getStartDate().isBlank()
                || dto.getEndDate() == null
                || dto.getEndDate().isBlank()) {
            throw new IllegalArgumentException(
                    "설문 시작일과 종료일은 필수입니다.");
        }

        validateDateText(dto.getStartDate(), "설문 시작일");
        validateDateText(dto.getEndDate(), "설문 종료일");

        if (dto.getEndDate().compareTo(dto.getStartDate()) < 0) {
            throw new IllegalArgumentException(
                    "설문 종료일은 시작일보다 빠를 수 없습니다.");
        }

        if (dto.getQuestions() == null
                || dto.getQuestions().isEmpty()) {
            throw new IllegalArgumentException(
                    "설문 문항은 한 개 이상 필요합니다.");
        }

        Set<Integer> seqNumbers = new HashSet<>();
        for (SurveyQuestionDTO question : dto.getQuestions()) {
            validateQuestionDTO(question);

            Integer seqNo = question.getSeqNo();
            if (seqNo != null && !seqNumbers.add(seqNo)) {
                throw new IllegalArgumentException(
                        "문항 순서는 중복될 수 없습니다: " + seqNo);
            }
        }
    }

    /** 문항 요청값을 검증한다. */
    private void validateQuestionDTO(SurveyQuestionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "설문 문항 데이터가 없습니다.");
        }

        if (dto.getQtext() == null
                || dto.getQtext().isBlank()) {
            throw new IllegalArgumentException(
                    "질문 내용은 필수입니다.");
        }

        if (dto.getQtype() == null
                || dto.getQtype().isBlank()) {
            throw new IllegalArgumentException(
                    "문항 유형은 필수입니다.");
        }

        String qtype =
                dto.getQtype().trim().toUpperCase();

        if (!QUESTION_TYPES.contains(qtype)) {
            throw new IllegalArgumentException(
                    "문항 유형은 SINGLE, MULTIPLE, TEXT, SCORE만 가능합니다.");
        }

        if (("SINGLE".equals(qtype)
                || "MULTIPLE".equals(qtype))
                && (dto.getQoptions() == null
                || dto.getQoptions().isBlank())) {
            throw new IllegalArgumentException(
                    "객관식 문항에는 선택지가 필요합니다.");
        }

        if (dto.getRequiredYn() != null) {
            String requiredYn =
                    dto.getRequiredYn().trim().toUpperCase();

            if (!"Y".equals(requiredYn)
                    && !"N".equals(requiredYn)) {
                throw new IllegalArgumentException(
                        "필수응답 여부는 Y 또는 N만 가능합니다.");
            }
        }

        if (dto.getSeqNo() != null
                && dto.getSeqNo() < 1) {
            throw new IllegalArgumentException(
                    "문항 순서는 1 이상이어야 합니다.");
        }
    }

    /**
     * 제출 답변의 중복 여부와 필수 문항 응답 여부를 검증한다.
     */
    private void validateSubmittedAnswers(
            Survey survey,
            List<SurveyAnswerDTO> answerDTOs) {

        Set<Long> submittedQuestionNos = new HashSet<>();

        for (SurveyAnswerDTO answerDTO : answerDTOs) {
            if (answerDTO == null
                    || answerDTO.getQuestionNo() == null) {
                throw new IllegalArgumentException(
                        "답변의 문항번호는 필수입니다.");
            }

            if (!submittedQuestionNos.add(
                    answerDTO.getQuestionNo())) {
                throw new IllegalArgumentException(
                        "같은 문항의 답변을 중복 제출할 수 없습니다: "
                                + answerDTO.getQuestionNo());
            }
        }

        for (SurveyQuestion question :
                survey.getQuestions()) {

            if ("Y".equals(question.getRequiredYn())
                    && !submittedQuestionNos.contains(
                            question.getNo())) {
                throw new IllegalArgumentException(
                        "필수 문항에 응답하지 않았습니다: "
                                + question.getQtext());
            }
        }
    }

    /** 문항유형에 맞는 실제 답변인지 검증한다. */
    private void validateAnswerByQuestionType(
            SurveyQuestion question,
            SurveyAnswerDTO answerDTO) {

        String answerText =
                normalizeText(answerDTO.getAtext());

        if (answerText == null) {
            throw new IllegalArgumentException(
                    "답변 내용은 비워둘 수 없습니다: "
                            + question.getQtext());
        }

        String qtype = question.getQtype();

        if ("SINGLE".equals(qtype)) {
            List<String> options =
                    splitOptions(question.getQoptions());

            if (!options.contains(answerText)) {
                throw new IllegalArgumentException(
                        "단일 선택 답변이 등록된 선택지와 일치하지 않습니다: "
                                + answerText);
            }
        }

        if ("MULTIPLE".equals(qtype)) {
            List<String> options =
                    splitOptions(question.getQoptions());
            List<String> selected =
                    splitOptions(answerText);

            if (selected.isEmpty()
                    || !options.containsAll(selected)) {
                throw new IllegalArgumentException(
                        "복수 선택 답변에 유효하지 않은 선택지가 포함되어 있습니다.");
            }
        }

        if ("SCORE".equals(qtype)) {
            try {
                int score = Integer.parseInt(answerText);

                if (score < 1 || score > 5) {
                    throw new IllegalArgumentException(
                            "점수형 문항은 1부터 5까지 입력해야 합니다.");
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "점수형 문항에는 숫자 1부터 5까지 입력해야 합니다.");
            }
        }
    }

    /** | 구분자로 저장된 선택지 문자열을 목록으로 변환한다. */
    private List<String> splitOptions(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String item : value.split("\\|")) {
            String normalized = normalizeText(item);
            if (normalized != null) {
                result.add(normalized);
            }
        }

        return result;
    }

    /** 설문 기간 내 제출인지 검증한다. */
    private void validateSurveyPeriod(Survey survey) {
        String current = now();

        if (current.compareTo(survey.getStartDate()) < 0) {
            throw new IllegalStateException(
                    "아직 시작되지 않은 설문입니다.");
        }

        if (current.compareTo(survey.getEndDate()) > 0) {
            throw new IllegalStateException(
                    "종료된 설문입니다.");
        }
    }

    /** 날짜 문자열 형식이 올바른지 검증한다. */
    private void validateDateText(String value, String fieldName) {
        try {
            LocalDateTime.parse(
                    value.trim(),
                    DATE_TIME_FORMATTER);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    fieldName
                            + " 형식은 yyyy-MM-dd HH:mm:ss 이어야 합니다.");
        }
    }

    /** 현재 시각을 DB 저장 형식 문자열로 반환한다. */
    private String now() {
        return LocalDateTime.now()
                .format(DATE_TIME_FORMATTER);
    }

    /** 공백 문자열은 null로 변환한다. */
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 설문 엔티티를 문항 포함 DTO로 변환한다. */
    private SurveyDTO toSurveyDTO(Survey survey) {
        SurveyDTO dto = toSurveyDTOWithoutQuestions(survey);

        List<SurveyQuestionDTO> questions =
                survey.getQuestions()
                        .stream()
                        .map(this::toQuestionDTO)
                        .collect(Collectors.toList());

        dto.setQuestions(questions);
        return dto;
    }

    /** 설문 목록용 DTO로 변환한다. */
    private SurveyDTO toSurveyDTOWithoutQuestions(
            Survey survey) {

        SurveyDTO dto = new SurveyDTO();
        dto.setNo(survey.getNo());
        dto.setManagerNo(survey.getManagerNo());
        dto.setTitle(survey.getTitle());
        dto.setDetail(survey.getDetail());
        dto.setStartDate(survey.getStartDate());
        dto.setEndDate(survey.getEndDate());
        dto.setCdate(survey.getCdate());
        dto.setQuestions(new ArrayList<>());

        return dto;
    }

    /** 문항 엔티티를 DTO로 변환한다. */
    private SurveyQuestionDTO toQuestionDTO(
            SurveyQuestion question) {

        SurveyQuestionDTO dto =
                new SurveyQuestionDTO();

        dto.setNo(question.getNo());
        dto.setSurveyNo(
                question.getSurvey().getNo());
        dto.setQtext(question.getQtext());
        dto.setQtype(question.getQtype());
        dto.setQoptions(question.getQoptions());
        dto.setRequiredYn(
                question.getRequiredYn());
        dto.setSeqNo(question.getSeqNo());
        dto.setCdate(question.getCdate());

        return dto;
    }

    /** 응답 목록에 사용하는 요약 DTO로 변환한다. */
    private SurveyResponseDTO toResponseSummaryDTO(
            SurveyResponse response) {

        SurveyResponseDTO dto =
                new SurveyResponseDTO();

        dto.setNo(response.getNo());
        dto.setSurveyNo(
                response.getSurvey().getNo());
        dto.setMemberNo(response.getMemberNo());
        dto.setCheckYn(response.getCheckYn());
        dto.setCheckDate(response.getCheckDate());
        dto.setCdate(response.getCdate());
        dto.setAnswers(new ArrayList<>());

        return dto;
    }

    /** 응답 엔티티와 문항별 답변을 DTO로 변환한다. */
    private SurveyResponseDTO toResponseDTO(
            SurveyResponse response) {

        SurveyResponseDTO dto =
                toResponseSummaryDTO(response);

        List<SurveyAnswerDTO> answers =
                response.getAnswers()
                        .stream()
                        .map(this::toAnswerDTO)
                        .collect(Collectors.toList());

        dto.setAnswers(answers);
        return dto;
    }

    /** 실제답변 엔티티를 DTO로 변환한다. */
    private SurveyAnswerDTO toAnswerDTO(
            SurveyAnswer answer) {

        SurveyAnswerDTO dto =
                new SurveyAnswerDTO();

        dto.setNo(answer.getNo());
        dto.setResponseNo(
                answer.getResponse().getNo());
        dto.setQuestionNo(
                answer.getQuestion().getNo());
        dto.setQtext(
                answer.getQuestion().getQtext());
        dto.setQtype(
                answer.getQuestion().getQtype());
        dto.setAtext(answer.getAtext());
        dto.setCdate(answer.getCdate());

        return dto;
    }
}
