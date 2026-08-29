package dev.jpa.allimio.shoprefund;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopRefundService {
  @Autowired
  ShopRefundRepository shopRefundRepository;

  /**
   * 환불계좌 등록. ShopOrderService.cancel()이 환불 발생 시(refundAmount > 0) 호출합니다.
   * @param ono 구독 내역 번호
   * @param pno 연결된 환불 결제기록 번호 (SHOP_PAYMENT.NO)
   * @param mno 회원번호
   * @param request 은행명/계좌번호/예금주명
   * @param amount 환불 금액
   * @return 등록된 환불계좌 정보
   */
  public ShopRefundDTO.Response save(String ono, Long pno, Long mno, ShopRefundDTO.Request request, Long amount) {
    ShopRefund shopRefund = ShopRefund.builder()
        .ono(ono)
        .pno(pno)
        .mno(mno)
        .bankName(request.getBankName())
        .accountNo(request.getAccountNo())
        .accountHolder(request.getAccountHolder())
        .amount(amount)
        .status(0)
        .cdate(Tool.getDate())
        .build();

    ShopRefund saved = shopRefundRepository.save(shopRefund);
    return ShopRefundDTO.Response.from(saved);
  }

  /** 특정 주문의 환불계좌 (최신순) */
  public List<ShopRefundDTO.Response> findByPno(Long pno) {
    return shopRefundRepository.findByPnoOrderByCdateDesc(pno)
        .stream().map(ShopRefundDTO.Response::from).collect(Collectors.toList());
  }

  /** 회원 기준 검색 + 페이징 */
//  public Page<ShopRefundDTO.Response> search(ShopRefundDTO.SearchRequest c, Pageable pageable) {
//    Page<ShopRefund> result = shopRefundRepository.searchByMno(c.getMno(), c.getStatus(), c.getDateFrom(), c.getDateTo(), pageable);
//    return result.map(ShopRefundDTO.Response::from);
//  }

  /** 관리자용 검색 + 페이징 */
  public Page<ShopRefundDTO.Response> searchAllAdmin(ShopRefundDTO.SearchRequest c, Pageable pageable) {
    Page<ShopRefund> result = shopRefundRepository.searchAllAdmin(c.getMno(), c.getStatus(), c.getDateFrom(), c.getDateTo(), pageable);
    return result.map(ShopRefundDTO.Response::from);
  }

  /**
   * 관리자용 — 처리상태 변경 (실제 이체 완료 처리 등)
   * @param no 환불 고유번호
   * @param request 변경할 상태
   * @return 변경된 환불계좌 정보, 대상 없으면 null
   */
  public ShopRefundDTO.Response updateStatus(Long no, ShopRefundDTO.UpdateStatusRequest request) {
    Optional<ShopRefund> optional = shopRefundRepository.findById(no);
    if (optional.isEmpty()) return null;

    ShopRefund shopRefund = optional.get();
    shopRefund.setStatus(request.getStatus());
    shopRefund.setUdate(Tool.getDate());

    ShopRefund saved = shopRefundRepository.save(shopRefund);
    return ShopRefundDTO.Response.from(saved);
  }
}