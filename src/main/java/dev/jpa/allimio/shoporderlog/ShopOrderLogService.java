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
  
  public ShopOrderLogService() {
    System.out.println("-> ShopOrderLogService created");
  }

  /**
   * 로그 한 건 기록. ShopOrderService의 save/linkShop/renew/cancel 각 메서드
   * 마지막에 이 메서드를 호출해서 이벤트를 남깁니다.
   * @param ono 구독 내역 번호
   * @param mno 회원번호
   * @param action 이벤트 종류 (0 결제 / 1 매장연결 / 2 갱신 / 3 취소 / 4 변경신청 / 5 변경완료 / 6 변경반려)
   * @param sno 관련 매장번호 (매장연결일 때만)
   * @param beforeEdate 변경 전 종료일 (갱신일 때만)
   * @param afterEdate 변경 후 종료일
   * @param amount 관련 금액 (결제액/환불액)
   * @param memo 부가 설명
   */
  public void log(String ono, Long mno, Integer action, Long sno, Long pno, 
      String beforeEdate, String afterEdate, Long amount, String memo, 
      Integer ccnt, Double bprice) {
    ShopOrderLog logEntity = ShopOrderLog.builder()
        .ono(ono)
        .mno(mno)
        .action(action)
        .sno(sno)
        .pno(pno)
        .beforeEdate(beforeEdate)
        .afterEdate(afterEdate)
        .amount(amount)
        .memo(memo)
        .ccnt(ccnt)
        .bprice(bprice)
        .cdate(Tool.getDate())
        .build();

    shopOrderLogRepository.save(logEntity);
  }
  
  /** 대수/단가 스냅샷이 필요 없는 이벤트(결제·매장연결 등)용 오버로드 */
  public void log(String ono, Long mno, Integer action, Long sno,
      String beforeEdate, String afterEdate, Long amount, String memo) {
    log(ono, mno, action, sno, null, beforeEdate, afterEdate, amount, memo, null, null);
  }


  /** 특정 주문의 전체 이력 (최신순) */
  public List<ShopOrderLogDTO.Response> findByOno(String ono) {
    return shopOrderLogRepository.findByOnoOrderByCdateDesc(ono)
        .stream().map(ShopOrderLogDTO.Response::from).collect(Collectors.toList());
  }

  /** 회원 기준 검색 + 페이징 */
  public Page<ShopOrderLogDTO.Response> search(ShopOrderLogDTO.SearchRequest c, Pageable pageable) {
    Page<Object[]> result = shopOrderLogRepository.searchByMno(
        c.getMno(), c.getOno(), c.getSno(), c.getAction(), c.getDateFrom(), c.getDateTo(), pageable);
    
    return result.map(row -> {
      ShopOrderLog log = (ShopOrderLog) row[0];
      String newPname = (String) row[1];
      Integer newCcnt = row[2] != null ? ((Number) row[2]).intValue() : null;
      String pname = (String) row[3];
      String sdate = (String) row[4];
      return ShopOrderLogDTO.Response.from(log, null, null, newPname, newCcnt, pname, sdate);
    });
  }

  /** 관리자용 검색 + 페이징 */
  public Page<ShopOrderLogDTO.Response> searchAllAdmin(ShopOrderLogDTO.SearchRequest req, Pageable pageable) {
    Page<Object[]> result = shopOrderLogRepository.searchAllAdmin(
        req.getWord(), req.getAction(), req.getDateFrom(), req.getDateTo(), pageable);

    return result.map(row -> {
      ShopOrderLog log = (ShopOrderLog) row[0];
      String id = (String) row[1];
      String sname = (String) row[2];
      String newPname = (String) row[3];
      Integer newCcnt = row[4] != null ? ((Number) row[4]).intValue() : null;
      String pname = (String) row[5];
      String sdate = (String) row[6];
      return ShopOrderLogDTO.Response.from(log, id, sname, newPname, newCcnt, pname, sdate);
    });
  }
}