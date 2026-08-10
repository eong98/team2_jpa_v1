package dev.jpa.allimio.tool;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * API 응답 시 페이징 처리된 데이터와 관련 메타데이터를 통일된 형식으로 반환하기 위한 공통 응답 객체(DTO)입니다.
 * 제네릭 타입 <T>를 사용하여 어떤 엔티티/DTO 데이터든 유연하게 담을 수 있습니다.
 */
@Getter             // 모든 필드의 Getter 메서드를 자동으로 생성
@Setter             // 모든 필드의 Setter 메서드를 자동으로 생성
@AllArgsConstructor // 모든 필드를 매개변수로 받는 생성자를 자동으로 생성
public class PageResponse<T> {

  /** 실제 페이징 처리되어 조회된 데이터 목록 (예: List<Contents>) */
  private List<T> content;

  /** 현재 페이지 번호 (0부터 시작) */
  private int page;

  /** 한 페이지당 보여줄 데이터의 개수 */
  private int size;

  /** 조건에 맞는 전체 데이터(레코드)의 총 개수 */
  private long totalElements;

  /** 전체 페이지 수 (totalElements / size를 계산하여 올림한 값) */
  private int totalPages;
  
  //⭐ Spring Data의 Page<T> 객체를 PageResponse<T>로 변환해 주는 편의 메서드
  public static <T> PageResponse<T> of(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages()
    );
}
}

