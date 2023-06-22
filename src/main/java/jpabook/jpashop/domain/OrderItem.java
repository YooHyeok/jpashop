package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id") //연관관계 주인 : item
    private Item item;

    @ManyToOne
    @JoinColumn(name = "order_id") // 연관관계의 주인 : order
    private Order order;

    private int orderPrice; //주문 가격
    private int count; //주문 수량
}
