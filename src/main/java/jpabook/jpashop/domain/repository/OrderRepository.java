package jpabook.jpashop.domain.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    /**
     * [주문(주문저장)]
     */
    public void save(Order order) {
        em.persist(order);
    }

    /**
     * [주문 단건 조회]
     */
    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * [주문 전체조회]
     */
    /*public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o", Order.class).getResultList();
    }*/
}
