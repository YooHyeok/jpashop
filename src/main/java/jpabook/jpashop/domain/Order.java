package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id") //연관관계의 주인 - Member 테이블의 member_id를 기준으로 M:1 객체 연관관계(하나의 Member는 여러개의 Order를 갖는다)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //1:M 양방향 연관 관계 (연관관계의 주인 : order)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id") //1:1관계에서는 fk를 어디에두냐에 따라 장단점이 있다. (주로 Access를 많이하는곳에 두는걸 추천한다.)
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // Enum Type : 주문상태 [ORDER, CANCEL]
    // 연관관계 편의 메소드 _ fk를 관리하는곳이 연관관계의 주인이 된다. (FK가 가까운곳 : Order) - 객체는 변경포인트가 두개인데 DB는 하나만 변경하면 된다.


    /**
     * 연관관계 편의 메소드(양방향)
     */
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this); // member엔터티에 존재하는 Orders List에 값을 추가한다. 각각의 Member객체당 하나 씩...
    }
    public void addOrderItem(OrderItem orderITem) {
        orderItems.add(orderITem); // (다)OrderItems에 주문한 상품을 추가
        orderITem.setOrder(this); // (일)orderItem에 주문한 상품 주입
    }
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery; // (일) delivery(배송정보)에 배송정보 주입
        delivery.setOrder(this); //(일) delivery의 order에 주문정보 주입
    }

    //=================== 생성 메서드 ===================//
    /**
     * [주문 생성] <br/>
     * 주문에 대한 정보를 주입한다(회원, 배송정보, 주문된 상품들의 목록, 주문상태, 주문일자 등)<br/>*/
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member); // 주문에 회원정보 주입
        order.setDelivery(delivery); // 주문에 배송정보 주입
        for (OrderItem orderItem : orderItems) { // (다) 쪽에 주문상품 주입
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER); // 주문상태 주입
        order.setOrderDate(LocalDateTime.now()); // 주문일자 주입
        return order;
    }

    //===================비즈니스 로직===================//
    /**
     * [주문 취소]<br/>
     * 배송 여부에 따라 취소 가능 판단 <br/>
     * 상품상태를 취소로 변경 <br/>
     * 주문한 상품들을 반복하여 cancel() 한다. <br/>
     * (cancel()은 재고를 원상복구 하는 기능으로 주문 상품에서 재고를 관리하기 때문에 OrderItem으로 부터 호출)*/
    public void cance() {
        if (delivery.getStatus() == DeliveryStatus.COMP) { // 배송이 출발 되었다면
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다."); //취소 불가
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : this.orderItems) {
            orderItem.cancel();
        }
    }
    
    //===================조회 로직===================//
    /**
     * [전체 주문 가격 조회]<br/>
     * 전체 주문에 대한 토탈 가격을 조회한다. <br/>
     * 주문 상품들로 부터 주문 가격을 가져와서 누적으로 합해야 한다. */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : this.orderItems) {
            totalPrice += orderItem.getOrderPrice();
        }
//      return  this.orderItems.stream().mapToInt(OrderItem::getOrderPrice).sum(); //한줄로 처리가 가능
        return totalPrice;
    }
}