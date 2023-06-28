package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 주문 조회 V4 & V5 <br/>
 * 컬렉션 DTO 직접 조회 및 최적화
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * 주문 조회 V4 - 컬렉션 Dto 직접 조회 <br/>
     * 컬렉션을 별도 DTO로 분리해서 따로 조회한다. <br/>
     * ToOne 관계 먼저 조회 -> ToMany관계 각각 별도로 처리 <br/>
     * findOrders() : orders 주문 조회(ToOne) <br/>
     * findOrderItems() : orderItems 주문상품 조회(ToMany)
     * @return
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> orders = findOrders();  //orders 주문 조회
        //OrderQueryDto 제너릭 타입 리스트로 반환
        orders.forEach(orderQueryDto -> 
        //반환된 결과를 루프로 돌린다.
        {
            List<OrderItemQueryDto> orderItems = findOrderItems(orderQueryDto.getOrderId()); 
            // 주문 ID를 기준으로 orderItems 주문상품을 검색하는 쿼리 -> OrderItemQueryDto 타입 리스트로 반환한다.
            orderQueryDto.setOrderItems(orderItems);
            // 반환받은 orderItems 리스트를 OrderQueryDto객체에 반복하여 저장 
        });
        return orders;
    }

    /**
     * [orders 주문 조회] <br/>
     * 1:N관계 (컬렉션) 를 제외한 나머지를 한번에 조회 <br/>
     * OrderQueryDto타입으로 반환
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    /**
     * [orderItems 주문상품 조회] <br/>
     * 1:N 관계인 orderItems 조회
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    //=== === === === === === === === === === === === 컬렉션 조회 최적화 시작 === === === === === === === === === === === ===//

    /**
     * 주문 조회 V5 - 컬렉션 Dto 직접 조회 [최적화] <br/>
     * 주문목록 List 반환 <br/>
     * 주문목록 List의 각 회원 id 기준으로 그룹핑한 Map 반환 <br/>
     * 루프를 돌려 Map으로부터 각 주문별 회원 id로 검색 -> 주문 List 반환 -> 주문 List Inject
     */
    public List<OrderQueryDto> findAllByDto_Optimization() {
        List<OrderQueryDto> result = findOrders();
        // ^ 주문 목록을 반환받는다.
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));
        // ^ 주문 목록으로 부터 회원 id를 리스트로 반환받고 해당 id리스트를 넘겨 주문 목록 리스트 Map을 반환받는다.
        result.forEach(orderQueryDto -> orderQueryDto.setOrderItems(orderItemMap.get(orderQueryDto.getOrderId())));
        // ^ 루프를 돌려서 주문한 회원 아이디를 기준으로 주문 목록 리스트 Map으로부터 검색후 반환받은 List객체를 주문 DTO의 orderItems에 다시 세팅한다.
        return result;
    }

    /**
     * 주문 조회한 목록의 회원 id를 리스트로 반환
     */
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(orderQueryDto -> orderQueryDto.getOrderId())
                .collect(Collectors.toList());
    }

    /**
     * [orderItems 주문상품 조회] <br/>
     * In 쿼리로 한번에 조회
     * 반환은 Long, List 제너릭타입으로 반환한다.
     * @return : orderId를 기준으로 그룹핑하여 Map의 Key로 반환
     * @return : orderItems를 OrderItemQueryDto타입의 리스트로 변환하여 Map의 Value로 변환
     * ex) id 1번 - 1번 주문리스트 / id 2번 - 2번 주문리스트 ...
     */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery("select " +
                        "new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto -> {
                    return OrderItemQueryDto.getOrderId();
                }));
    }

    //=== === === === === === === === === === === === 컬렉션 조회 [플랫 최적화] 시작 === === === === === === === === === === === ===//

    /**
     * 주문 조회 V6 - 컬렉션 Dto 직접 조회 [플랫 최적화]
     */
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery("select " +
                "new jpabook.jpashop.repository.order.query.OrderFlatDto" +
                "(o.id, m.name, o.orderDate, d.address, o.status, i.name, oi.orderPrice, oi.count) " +
                "from Order o " +
                "join o.member m " +
                "join o.delivery d " +
                "join o.orderItems oi " +
                "join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
