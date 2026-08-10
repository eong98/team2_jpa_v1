package dev.jpa.alllimio.survey;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 설문 API 공통 예외 처리기.
 *
 * <p>Service에서 발생한 검증 예외를 프론트가 처리하기 쉬운
 * JSON 형식으로 반환한다.</p>
 */
@RestControllerAdvice(basePackages = {
        "survey",
        "surveyquestion",
        "surveyresponse",
        "surveyanswer"
})
public class SurveyExceptionHandler {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 잘못된 요청값을 400 Bad Request로 반환한다. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage());
    }

    /** 중복 제출, 종료된 설문 등 현재 상태 충돌을 409로 반환한다. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage());
    }

    /** 예상하지 못한 서버 오류를 500으로 반환한다. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleServerError(
            Exception exception) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "설문 처리 중 서버 오류가 발생했습니다.");
    }

    /** 공통 오류 응답 JSON을 만든다. */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp",
                LocalDateTime.now().format(FORMATTER));
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}
