package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /**
     * [주문] <br/>
     * 주문하기 위해 필요한 정보들은 회원정보, 배송정보, 주문상품 이다.<br/>
     * 따라서 해당 객체들을 만들고 주문에 주입한 뒤 저장한다. <br/>
     * 이때, 주문상품에 대한 정보는 상품정보가 들어가야하므로 상품 객체도 만든 후 주문상품에 주입한다.<br/>
     * @Param 회원ID, 아이템ID, 주문수량
     * @return 주문 완료된 주문ID
     */
    @Transactional(readOnly = false)
    public Long order(Long memberId, Long itemId, int count) {
        // 엔터티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);
        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);
        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        orderRepository.save(order); // casecade.All에 의해서 order가 persist될때 delivery와 orderItem도 자동으로 함께 persist된다.
        return order.getId();
    }
    /**
     * [주문취소] <br/>
     * 주문을 취소하기 위해서 주문한 객체를 조회해와야 한다. <br/>
     * 조회한 주문 객체에서 cancel() 메서드를 호출하여 주문을 취소한다. <br/>
     * JPA의 강점 : 트랜잭션이 commit되는 시점에 변경내역을 감지해서 update 쿼리를 날린다. <br/>
     * 단, Transaction 내에서 DirtyChecking이 일어나므로 @Transaction(readOnly=false)를 선언해줘야만 한다.
     */
    @Transactional(readOnly = false)
    public void cancelOrder(Long orderId) {
        //주문 엔터티 조회
        Order order = orderRepository.findOne(orderId);
        order.cancel();
    }
    /**
     * [주문검색]
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
