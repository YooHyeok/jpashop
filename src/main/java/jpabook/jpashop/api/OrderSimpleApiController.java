package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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
     * 지연 로딩으로 너무 많은 SQL을 실행한다. (N+1) <br/>
     * Order 2개 (1번) 1 +2+2+2
     *  -> Member1, Delivery(address), OrderItem, Item <br/>
     *  -> Member2, Delivery(address), OrderItem, Item <br/>
     *
     * @return
     */
    @GetMapping("/api/v2/simple-orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))// OrderDto로 반복 변환
                .collect(Collectors.toList()); // 변환한 Dto 객체들을 List로 변환
        return result;

    }

    /**
     * Order에 대한 Dto변환
     */
    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime localDateTime;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // Lazy 초기화 (영속성컨텍스트 검색후 없으면 DB쿼리 호출)
            this.localDateTime = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // Lazy 초기화 (영속성컨텍스트 검색후 없으면 DB쿼리 호출)
            this.orderItems = order.getOrderItems().stream()// Lazy 초기화 (영속성컨텍스트 검색후 없으면 DB쿼리 호출)
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    /**
     * OrderItem에 대한 Dto 변환
     */
    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int  count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }

}
