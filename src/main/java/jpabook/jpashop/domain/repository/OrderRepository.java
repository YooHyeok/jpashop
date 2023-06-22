package jpabook.jpashop.domain.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
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
     * [주문 전체조회] <br/>
     * 동적쿼리 - JPQL
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true; //조건여부 true

        String memberName = orderSearch.getMemberName(); //회원이름
        OrderStatus orderStatus = orderSearch.getOrderStatus(); //주문상태

        // 주문 상태 검색
        if (orderStatus != null) { // 검색조건 중 주문상태가 존재할 경우 where
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false; // 조건여부를 false로 변경 후 종료
            } else {  // 조건여부 false이면 and
                jpql += " and";
            }
            jpql += "o.status = :status"; // 조건 추가
        }

        // 회원 이름 검색

        if (memberName != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += "m.name like :name"; // 조건 추가
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);//최대 1000건

        if (orderStatus != null) { // orderStatus 값이 존재하면 쿼리 파라미터 바인딩
            query = query.setParameter("status", orderStatus);
        }
        if (StringUtils.hasText(memberName)) { // StringUtils 사용 (ValueArgs에 String값이 포함되어있는지)
            query = query.setParameter("name", memberName);
        }
        return query.getResultList();
    }

    /**
     * [주문 전체조회] <br/>
     * 동적쿼리 - JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        String memberName = orderSearch.getMemberName(); //회원이름
        OrderStatus orderStatus = orderSearch.getOrderStatus(); //주문상태

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //주문 - 회원 Inner Join

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderStatus != null) {
            Predicate status = cb.equal(o.get("status"), orderStatus);
            criteria.add(status);
        }
        // 회원 이름 검색
        if (StringUtils.hasText(memberName)) {
            Predicate name = cb.like(m.get("name"), memberName);
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq)
                .setMaxResults(1000);// 최대 1000건
        return query.getResultList();
    }
}
