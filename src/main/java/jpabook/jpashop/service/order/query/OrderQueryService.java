package jpabook.jpashop.service.order.query;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Open Session In View False
 * 서비스 로직으로 변환 예제 클래스.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * 주문 컬렉션 조회 V2 - 엔티티 DTO로 변환 <br/>
     * N+1현상 발생 - 총 쿼리 수 : 11 <br/>
     * Order(1) Member(2) delivery(2) orderItems(2) < item(2)
     * @param orderSearch
     * @return
     */
    public List<OrderApiController.OrderDto> findAllByString(OrderSearch orderSearch) {

        List<Order> orders = orderRepository.findAllByString(orderSearch);
        List<OrderApiController.OrderDto> result = orders.stream()
                .map(order -> new OrderApiController.OrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

}
