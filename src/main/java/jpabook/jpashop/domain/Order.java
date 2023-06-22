package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id") //연관관계의 주인 - Member 테이블의 member_id를 기준으로 M:1 객체 연관관계(하나의 Member는 여러개의 Order를 갖는다)
    private Member member;

    @OneToMany(mappedBy = "order") //1:M 양방향 연관 관계 (연관관계의 주인 : order)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "delivery_id") //1:1관계에서는 fk를 어디에두냐에 따라 장단점이 있다. (주로 Access를 많이하는곳에 두는걸 추천한다.)
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // Enum Type : 주문상태 [ORDER, CANCEL]
    // 연관관계 편의 메소드 _ fk를 관리하는곳이 연관관계의 주인이 된다. (FK가 가까운곳 : Order) - 객체는 변경포인트가 두개인데 DB는 하나만 변경하면 된다.


}