package jpabook.jpashop.repository;

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
            jpql += " o.status = :status"; // 조건 추가
        }

        // 회원 이름 검색

        if (StringUtils.hasText(memberName)) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name"; // 조건 추가
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
            Predicate name = cb.like(m.get("name"), "%" + memberName + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq)
                .setMaxResults(1000);// 최대 1000건
        return query.getResultList();
    }

    /**
     * [주문 전체조회] - 패치조인<br/>
     * distinct : 1대 다 조인이 있으므로 데이터베이스 Row증가. <br/>
     * 같은 엔티티의 조회 수도 증가하게 된다. <br/>
     * JPA의 Distinct는 같은 엔터티가 조회되면, 에플리케이션에서 중복을 걸러준다. <br/>
     * (order가 컬렉션 페치조인때문에 중복조회되는 것을 막아준다.)
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d ") // 컬렉션 패치조인
                .getResultList();
    }

//    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    /**
     * [주문 전체조회] <br/>
     * 동적쿼리 - queryDsl(BooleanBuilder)
     */
    /*public List<Order> findAllByQuerydsl(OrderSearch orderSearch) {
        String memberName = orderSearch.getMemberName(); //회원이름
        OrderStatus orderStatus = orderSearch.getOrderStatus(); //주문상태

        BooleanBuilder builder = new BooleanBuilder();

        if (orderStatus != null) {
            QOrder order = QOrder.order;
            builder.and(order.orderStatus.eq(orderStatus));
        }
        if (StringUtils.hasText(memberName)) {
            QMember member = QMember.member;
            builder.and(member.name.eq(memberName));
        }
        return queryFactory
                .selectFrom(order)
                .join(order.member, member)
                .where(builder)
                .offset(0)
                .limit(1000)
                .fetch();
    }*/

    /**
     * [주문 전체조회] <br/>
     * 동적쿼리 - queryDsl(BooleanExpression)
     */
    /*public List<Order> findAllByQuerydsl(OrderSearch orderSearch) {
        String memberName = orderSearch.getMemberName(); //회원이름
        OrderStatus orderStatus = orderSearch.getOrderStatus(); //주문상태

        QOrder order = QOrder.order;
        QMember member = QMember.member;

        return queryFactory
                .selectFrom(order)
                .join(order.member, member)
                .where(allAndEq(memberName, orderStatus, order, member))
                .offset(0)
                .limit(1000)
                .fetch();

    }
    private BooleanExpression orderStatusEq(OrderStatus orderStatus, QOrder order) { // Predicate도 가능
        return orderStatus == null ? null : order.orderStatus.eq(orderStatus); // 조건절에 null이 오면 무시된다.
    }
    private BooleanExpression memberNameEq(String memberName, QMember member) {
        return (!StringUtils.hasText(memberName)) ? null : member.name.eq(memberName);
    }
    private BooleanExpression allAndEq(OrderStatus orderStatus, String memberName, QOrder order, QMember member) {
        return orderStatusEq(orderStatus, order).and(memberNameEq(memberName, member));
    }*/

    /**
     * [주문 전체조회] <br/>
     * 동적쿼리 - queryDsl - 강의 버전
     */
    /*public List<Order> findAll(OrderSearch orderSearch) {
        QOrder order = QOrder.order;
        QMember member = QMember.member;
        return queryFactory
                .selectFrom(order)
                .join(order.member, member)
                .where(statusEq(orderStatus, order).and(nameLike(memberName, member)))
                .limit(1000)
                .fetch();
    }
    private BooleanExpression orderStatusEq(OrderStatus orderStatus, QOrder order) {
        return orderStatus == null ? null : order.orderStatus.eq(orderStatus);
    }
    private BooleanExpression nameLike(String memberName, QMember member) {
        return (!StringUtils.hasText(memberName)) ? null : member.name.like(memberName);
    }*/

}
