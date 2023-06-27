package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order 조회 (xToOne 관계 최적화)
 * Order -> Member 연관관계
 * Order -> Delivery 연관관계
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    /**
     * 주문조회 API V2 - 엔티티를 DTO로 변환 <br/>
     * 지연 로딩으로 너무 많은 SQL을 실행한다. (N+1) <br/>
     * Order 2개 (1번) 1 +2+2+2
     *  -> Member1, Delivery(address), OrderItem, Item <br/>
     *  -> Member2, Delivery(address), OrderItem, Item <br/>
     *
     * @return
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))// OrderDto로 반복 변환
                .collect(Collectors.toList()); // 변환한 Dto 객체들을 List로 변환
        return result;

    }

    /**
     * 주문조회 API V3 - DTO변환 - Fetch 조인 최적화 <br/>
     * 패치조인으로 SQL이 한번만 실행된다. <br/>
     * dis
     * @return
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 주문조회 API V4 - 엔티티 -> DTO로 바로조회 <br/>
     * 리포지토리 재사용성이 떨어진다. <br/>
     * API스펙에 맞춘 코드가 리포지토리에 들어가는 단점이 있다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    /**
     * Order에 대한 Dto변환
     */
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime localDateTime;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // Lazy 초기화 (영속성컨텍스트 검색후 없으면 DB쿼리 호출)
            this.localDateTime = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // Lazy 초기화 (영속성컨텍스트 검색후 없으면 DB쿼리 호출)

        }
    }
}
