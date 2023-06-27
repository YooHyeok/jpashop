package jpabook.jpashop;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * [주문 API 조회용 샘플데이터] <br/>
 * 총 주문 2개 <br/>
 * * userA <br/>
 * * * JPA1 BOOK <br/>
 * * * JPA2 BOOK <br/>
 * * userB <br/>
 * * * SPRING1 BOOK <br/>
 * * * SPRING2 BOOK
 */
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        /**
         * 초기화 메소드1
         * userA 회원이 JPA1BOOK과 JPA2BOOK을 구매한다.
         * 1. Member 저장
         * 2. Book 저장
         * 3. OrderItem 세팅 - 주문한 상품이 무엇인지 등록해 준다.
         * 4. Order 세팅 및 저장 - 회원 정보, 배송지 주소, 주문아이템을 등록하고 저장 -> 주문
         */
        public void dbInit1() {
            Member member = createMember("userA",
                                                new Address("서울",
                                                            "1",
                                                            "12345"));
            em.persist(member);
            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);
            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);
            /* 주문상품 생성 */
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);
            /* 주문 생성 */
            Order order = Order.createOrder(member, createDelivery(member), orderItem1, orderItem2);
            em.persist(order);
        }

        /**
         * 초기화 메소드2
         * userB 회원이 SPRING1BOOK과 SPRING2BOOK을 구매한다.
         */
        public void dbInit2() {
            Member member = createMember("userB",
                                                new Address("진주",
                                                            "2",
                                                            "54321"));
            em.persist(member);
            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);
            Book book2 = createBook("SPRING1 BOOK", 40000, 300);
            em.persist(book2);
            /* 주문상품 생성 */
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);
            /* 주문 생성 */
            Order order = Order.createOrder(member, createDelivery(member), orderItem1, orderItem2);
            em.persist(order);
        }

        /**
         * Member 생성 메소드
         * @param name
         * @param address
         * @return
         */
        private Member createMember(String name, Address address) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(address);
            return member;
        }

        /**
         * Book 생성 메소드
         * @param name
         * @param price
         * @param stockQuantity
         * @return
         */
        private Book createBook(String name, int price, int stockQuantity) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);
            return book;
        }

        /**
         * Delivery 생성 메소드 <br/>
         * 주소를 초기화 한다. <br/>
         * 배송은 주문이 진행된 후 발생하므로 주문한 회원의 주소정보를 주입한다.
         * @param member
         * @return
         */
        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }
    }


}
