package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id") //연관관계 주인 : item
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id") // 연관관계의 주인 : order
    private Order order;

    private int orderPrice; //주문 가격
    private int count; //주문 수량

    //=================== 생성 메서드 ===================//
    /**
     * [주문 상품 생성] <br/>
     * 주문에대한 상품을 관리하는 중간 테이블인 OrderItem에서는 주문 수량을 관리한다 <br/>
     * 따라서 상품 주문시 상품 재고를 이곳에서 깎는다. */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        item.removeStock(count); // 아이템이 주문이 되면 아이템의 수량을 주문한만큼 삭제한다.
        //
        return orderItem;
    }

    //=================== 비즈니스 로직 ===================//
    /**
     * [주문 취소] <br/>
     * 주문을 취소하기 때문에 상품 재고를 주문한만큼 원상복구 시킨다. */
    public void cancel() {
        getItem().addStock(count);
    }
    
    //=================== 조회 로직 ===================//
    /**
     * [주문 상품 전체 가격 조회] <br/>
     * 주문가격인 OrderPrice를 가져와서 주문한 수량만큼 곱한다.
     * @return
     */
    public int getTotalPrice() {
        return this.getOrderPrice() * getCount();
    }
}
