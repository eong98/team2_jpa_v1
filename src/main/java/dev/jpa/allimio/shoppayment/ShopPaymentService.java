package dev.jpa.allimio.shoppayment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.jpa.allimio.tool.Tool;

@Service
public class ShopPaymentService {
  @Autowired
  ShopPaymentRepository shopPaymentRepository;

  /**
   * 결제 기록 등록. 지금은 PG 연동 전이라 항상 결제완료(pstatus=0)로 저장됩니다.
   * 나중에 토스페이 등 실제 PG를 붙이면, PG 콜백 결과에 따라 pstatus를 결정하도록
   * 이 메서드를 확장하면 됩니다.
   * @param ono 구독 내역 번호
   * @param mno 회원번호
   * @param price 결제 금액
   * @param pmethod 결제 수단 (0 카드 / 1 계좌이체 / 2 토스페이)
   * @return 기록된 결제 내역
   */
  public ShopPaymentDTO.Response pay(String ono, Long mno, Long price, Integer pmethod) {
    ShopPayment shopPayment = ShopPayment.builder()
        .ono(ono)
        .mno(mno)
        .price(price)
        .pmethod(pmethod)
        .pstatus(0)
        .cdate(Tool.getDate())
        .build();

    ShopPayment saved = shopPaymentRepository.save(shopPayment);
    return ShopPaymentDTO.Response.from(saved);
  }

  /**
   * 환불(취소) 기록 등록. 결제 자체를 취소로 남기는 별도 행을 추가하는 방식입니다
   * (기존 결제 행을 수정하지 않고, 새 행으로 환불액을 기록). 결제수단(PMETHOD)은
   * NOT NULL 컬럼이라, 이 주문의 가장 최근 결제 건에서 사용된 수단을 그대로 물려받습니다.
   * @param ono 구독 내역 번호
   * @param mno 회원번호
   * @param refundAmount 환불 금액
   * @return 기록된 환불 내역
   */
  public ShopPaymentDTO.Response refund(String ono, Long mno, Long refundAmount) {
    Integer pmethod = shopPaymentRepository.findByOnoOrderByCdateDesc(ono).stream()
        .filter(p -> p.getPstatus() == 0) // 결제완료 건 중 최신
        .findFirst()
        .map(ShopPayment::getPmethod)
        .orElse(0); // 원 결제 기록을 못 찾으면 카드(0)로 기본 처리

    ShopPayment shopPayment = ShopPayment.builder()
        .ono(ono)
        .mno(mno)
        .price(refundAmount)
        .pmethod(pmethod)
        .pstatus(2)
        .cdate(Tool.getDate())
        .udate(Tool.getDate())
        .build();

    ShopPayment saved = shopPaymentRepository.save(shopPayment);
    return ShopPaymentDTO.Response.from(saved);
  }
  
  /**
   * 결제 단건 조회 (PK 기준)
   * @param no 결제 고유번호
   * @return 결제 내역, 없으면 null
   */
  public ShopPaymentDTO.Response findById(Long no) {
    Optional<ShopPayment> optional = shopPaymentRepository.findById(no);
    return optional.map(ShopPaymentDTO.Response::from).orElse(null);
  }

  /** 특정 주문의 결제 내역 (최신순) */
  public List<ShopPaymentDTO.Response> findByOno(String ono) {
    return shopPaymentRepository.findByOnoOrderByCdateDesc(ono)
        .stream().map(ShopPaymentDTO.Response::from).collect(Collectors.toList());
  }

  /** 회원 기준 검색 + 페이징 */
  public Page<ShopPaymentDTO.Response> search(ShopPaymentDTO.SearchRequest c, Pageable pageable) {
    Page<ShopPayment> result = shopPaymentRepository.searchByMno(
        c.getMno(), c.getOno(), c.getPmethod(), c.getPstatus(), c.getDateFrom(), c.getDateTo(), pageable);
    return result.map(ShopPaymentDTO.Response::from);
  }

  /** 관리자용 검색 + 페이징 */
//  public Page<ShopPaymentDTO.Response> searchAllAdmin(ShopPaymentDTO.SearchRequest c, Pageable pageable) {
//    Page<ShopPayment> result = shopPaymentRepository.searchAllAdmin(
//        c.getMno(), c.getWord(), c.getPmethod(), c.getPstatus(), c.getDateFrom(), c.getDateTo(), pageable);
//    return result.map(ShopPaymentDTO.Response::from);
//  }
}