package dev.jpa.allimio.inmenu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InMenuRepository extends JpaRepository<InMenu, Long> {

  /**
   * 상위메뉴번호(FKNO) 기준 하위메뉴 조회, 정렬순서(ORD) 오름차순
   * - 최상위 메뉴를 찾을 때는 fkno에 null을 넘기면 됨
   */
  List<InMenu> findByFknoOrderByOrdAsc(Long fkno);

}
