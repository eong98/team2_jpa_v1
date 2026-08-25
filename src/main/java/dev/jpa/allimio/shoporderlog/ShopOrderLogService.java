package dev.jpa.allimio.shoporderlog;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopOrderLogService {
  @Autowired
  ShopOrderLogRepository shopOrderLogRepository;

  /**
   * 로그 한 건 기록. ShopOrderService의 save/linkShop/renew/cancel 각 메서드
   * 마지막에 이 메서드를 호출해서 이벤트를 남깁니다.
   * @param orderno 구독 내역 번호
   * @param mno 회원번호
   * @param action 이벤트 종류 (0 결제 / 1 매장연결 / 2 갱신 / 3 취소)
   * @param sno 관련 매장번호 (매장연결일 때만)
   * @param beforeEdate 변경 전 종료일 (갱신일 때만)
   * @param afterEdate 변경 후 종료일
   * @param amount 관련 금액 (결제액/환불액)
   * @param memo 부가 설명
   */
  public void log(String orderno, Long mno, Integer action, Long sno,
      String beforeEdate, String afterEdate, Long amount, String memo) {
    ShopOrderLog logEntity = ShopOrderLog.builder()
        .orderno(orderno)
        .mno(mno)
        .action(action)
        .sno(sno)
        .beforeEdate(beforeEdate)
        .afterEdate(afterEdate)
        .amount(amount)
        .memo(memo)
        .cdate(Tool.getDate())
        .build();

    shopOrderLogRepository.save(logEntity);
  }

  /** 특정 주문의 전체 이력 (최신순) */
  public List<ShopOrderLogDTO.Response> findByOrderno(String orderno) {
    return shopOrderLogRepository.findByOrdernoOrderByCdateDesc(orderno)
        .stream().map(ShopOrderLogDTO.Response::from).collect(Collectors.toList());
  }

  /** 회원 기준 검색 + 페이징 */
  public Page<ShopOrderLogDTO.Response> search(ShopOrderLogDTO.SearchRequest c, Pageable pageable) {
    Page<ShopOrderLog> result = shopOrderLogRepository.searchByMno(
        c.getMno(), c.getSno(), c.getAction(), c.getDateFrom(), c.getDateTo(), pageable);
    return result.map(ShopOrderLogDTO.Response::from);
  }

  /** 관리자용 검색 + 페이징 */
  public Page<ShopOrderLogDTO.Response> searchAllAdmin(ShopOrderLogDTO.SearchRequest c, Pageable pageable) {
    Page<ShopOrderLog> result = shopOrderLogRepository.searchAllAdmin(
        c.getMno(), c.getSno(), c.getAction(), c.getDateFrom(), c.getDateTo(), pageable);
    return result.map(ShopOrderLogDTO.Response::from);
  }
}