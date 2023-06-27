package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 주문 조회 V4 엔티티를 VO객체로 바로 조회하기 위한 DTO클래스
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    /**
     * JPQL에서 new 연산자를 사용해서 DTO로 즉시 변환 <br/>
     * -> DTO클래스의 풀패키지 경로를 기재해줘야한다.
     * -> DTO클래스의 생성자 매개변수에 맞게 모든 컬럼을 매핑해줘야한다.
     * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB->애플리케이션 네트워크 용량 최적화(생각보다 미비)
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select " +
                        "new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
