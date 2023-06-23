package jpabook.jpashop.repository;

import jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 검색조건 파라미터 클래스 <br/>
 * 회원 이름과 주문 상태를 기준으로 검색하는 클래스이다.
 */
@Getter @Setter
public class OrderSearch {
    private String memberName; //회원 이름
    private OrderStatus orderStatus; //주문 상태[ORDER, CANCEL]
}
