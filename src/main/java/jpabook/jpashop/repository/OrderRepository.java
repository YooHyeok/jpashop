package jpabook.jpashop.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
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
     * [주문 전체조회 API 2] - 패치조인<br/>
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d ") // 컬렉션 패치조인
                .getResultList();
    }

    /**
     * [주문 전체조회 API 3] - 컬렉션 패치조인<br/>
     * distinct : 1대 다 조인이 있으므로 데이터베이스 Row증가. <br/>
     * 같은 엔티티의 조회 수도 증가하게 된다. <br/>
     * JPA의 Distinct는 같은 엔터티가 조회되면, 에플리케이션에서 중복을 걸러준다. <br/>
     * (order가 컬렉션 페치조인때문에 중복조회되는 것을 막아준다.)
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                        "select distinct o from Order o " +
                                "join fetch o.member m " +
                                "join fetch o.delivery d " +
                                "join fetch o.orderItems oi " +
                                "join fetch oi.item i") // 컬렉션 패치조인
                .getResultList();
    }

    /**
     * [주문 전체조회 API 3.1] - 컬렉션 패치조인 페이징 <br/>
     * Order 입장에서 xToOne 관계 엔터티만 Join을 건다. <br/>
     * 나머지 xToMany 관계는 지연로딩 즉, 객체탐색을 통해 조회한다. <br/>
     * -> OrderItems를 출력할때 in(_,_)쿼리 발생 <br/>
     * hibernate.default_batch_fetch_size를 100으로 지정한다.
     * @param offset
     * @param limit
     * @return
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "select distinct o from Order o " +
                                "join fetch o.member m " +
                                "join fetch o.delivery d ") // 컬렉션 패치조인
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    //=== === === === === === === === queryDls 코드 구현=== === === === === === === ===//


    /**
     * [주문 전체조회] <br/>
     * 동적쿼리 - queryDsl(BooleanBuilder)
     */
    public List<Order> findAllByQuerydsl1(OrderSearch orderSearch) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        String memberName = orderSearch.getMemberName(); //회원이름
        OrderStatus orderStatus = orderSearch.getOrderStatus(); //주문상태

        BooleanBuilder builder = new BooleanBuilder();

        QOrder order = QOrder.order;
        QMember member = QMember.member;

        if (orderStatus != null) {
            builder.and(order.status.eq(orderStatus));
        }
        if (StringUtils.hasText(memberName)) {
            builder.and(member.name.eq(memberName));
        }
        return queryFactory
                .selectFrom(order)
                .join(order.member, member)
                .where(builder)
                .offset(0)
                .limit(1000)
                .fetch();
    }

    /**
     * [주문 전체조회] <br/>
     * 동적쿼리 - queryDsl(BooleanExpression)
     */
    public List<Order> findAllByQuerydsl2(OrderSearch orderSearch) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
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
        return orderStatus == null ? null : order.status.eq(orderStatus); // 조건절에 null이 오면 무시된다.
    }
    private BooleanExpression memberNameEq(String memberName, QMember member) {
        return (!StringUtils.hasText(memberName)) ? null : member.name.eq(memberName);
    }
    private BooleanExpression allAndEq(String memberName, OrderStatus orderStatus, QOrder order, QMember member) {
        return orderStatusEq(orderStatus, order).and(memberNameEq(memberName, member));
    }

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
