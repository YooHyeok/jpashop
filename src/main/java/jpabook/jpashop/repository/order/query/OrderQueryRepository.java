package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 *
 * 컬렉션을 별도 DTO로 분리해서 따로 조회한다. <br/>
 * ToOne 관계 먼저 조회 -> ToMany관계 각각 별도로 처리 <br/>
 * findOrders() : orders 주문 조회(ToOne) <br/>
 * findOrderItems() : orderItems 주문상품 조회(ToMany)
 *
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

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
}
