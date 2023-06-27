package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Order 조회 (xToOne 관계 최적화)
 * Order -> Member 연관관계
 * Order -> Delivery 연관관계
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    /**
     * 주문 조회 API V1 - 엔터티 직접 노출
     * - Hibernate5Module 모듈 등록, Lazy = null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
//            order.getOrderItems().get(0); // Lazy 강제 초기화 하지 않았으나 getTotalPrice()로 인해 호출된다.
            /**
             * Lazy를 강제 초기화한다.
             * (Lazy를 초기화 한다는 말은 Lazy Loading이라고 볼 수 있다. 연관관계에 있는 엔터티를 LazyLoading 조회한다는 뜻)             * member 데이터 조회
             * delivery 데이터 조회
             * Jackson이 JSON을 생성할 때 getXxx을 찾아서 호출한다.
             * 현재, getTotalPrice 메서드 에서 orderItems를 사용하고 있다.
             *
             */
        }
        return all;
    }
}
