package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.*;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.order.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 주문 컬렉션 조회 V1 - 엔티티 직접 노출
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화
            // stream().foreach() : 루프를 사용해서 하나씩 객체 탐색
        }
        return all;
        /**
         * Open Session In View = false <br/>
         * Service단으로부터 반환받는다. <br/>
         */
    }

    private final OrderQueryService orderQueryService;
    /**
     * 주문 컬렉션 조회 V2 - 엔티티 DTO로 변환
     * N+1현상 발생 - 총 쿼리 수 : 11 <br/>
     * Order(1) Member(2) delivery(2) orderItems(2) < item(2)
     * 
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        /*List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return result;*/
        /**
         * Open Session In View = false <br/>
         * Service단으로부터 반환받는다. <br/>
         */
        List<OrderDto> orders = orderQueryService.findAllByString(new OrderSearch());
        return orders;
    }

    /**
     * 주문 컬렉션 조회 V3 - 컬렉션 패치조인 최적화
     * 패치조인으로 SQL이 한번만 실행된다. <br/>
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 주문 컬렉션 조회 V3.3 - 페이징 <br/>
     * toOne 관계만 우선 모두 페치조인으로 최적화 <br/>
     * N+1 발생할 수 밖에 없음 - Order & Member & Delivery(1) - Items(2) < OrderItems(2) <br/>
     * 컬렉션 관계는 지연로딩 성능 최적화 해야한다. <br/>
     * (hibernate.default_batch_fetch_size={__} / @BatchSize) <br/>
     * URL : http://localhost:8080/api/v3.1/orders?offset=1&limit=2
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 주문 컬렉션 조회 V4 - JPA에서 DTO 직접 조회
     * new 연산자로 쿼리레벨에서 Dto 타입으로 반환한다.
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 주문 컬렉션 조회 V5 - JPQL DTO 직접 조회 (컬렉션 조회 최적화) <br/>
     * In 쿼리로 최적화된다.
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_Optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        // ^ OrderFlatDto 타입으로 전체 조인 결과 조회
        return flats.stream()
                .collect(Collectors.groupingBy( //(OrderQueryDto, List<OrderItemQueryDto>) 그룹핑 -> @EqualsHashCode(of="orderId)" 기준으로 매핑 되므로 중복이 제거된다.
                        // Map<OrderQueryDto, List<OrderFlatDto>>
                        // OrderFlatDto를 OrderQueryDto로 매핑하고 [Key]로 사용
                        orderFlatDto ->
                            {return new OrderQueryDto(orderFlatDto.getOrderId(),
                                                        orderFlatDto.getName(),
                                                        orderFlatDto.getOrderDate(),
                                                        orderFlatDto.getOrderStatus(),
                                                        orderFlatDto.getAddress());
                            }
                        // OrderFlatDto를 OrderQueryDto로 매핑하고 [Key]로 사용
                            ,Collectors.mapping(
                                    // OrderFlatDto를 OrderItemQueryDto로 매핑한 뒤 리스트로 반환하여 [Value]로 사용
                                    orderFlatDto ->
                                            new OrderItemQueryDto(orderFlatDto.getOrderId(),
                                                                    orderFlatDto.getItemName(),
                                                                    orderFlatDto.getOrderPrice(),
                                                                    orderFlatDto.getCount())
                                            , Collectors.toList()
                                    // OrderFlatDto를 OrderItemQueryDto로 매핑한 뒤 리스트로 반환하여 [Value]로 사용
                            )
                        )
                )
                .entrySet().stream() // entrySet으로 변환후 map(중복 제거)
                .map(entry -> new OrderQueryDto(entry.getKey().getOrderId()
                                            , entry.getKey().getName()
                                            , entry.getKey().getOrderDate()
                                            , entry.getKey().getOrderStatus()
                                            , entry.getKey().getAddress()
                                            , entry.getValue()
                ))
                .collect(Collectors.toList());

    }

    /**
     * Order 엔터티를 변환할 Dto 내부클래스
     */
    @Data
    public static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Order의 OrderItem 엔터티를 변환할 Dto 내부클래스
     */
    @Data
    public static class OrderItemDto {
        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count; //주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
