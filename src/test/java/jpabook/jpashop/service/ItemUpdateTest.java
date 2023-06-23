package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@Transactional
public class ItemUpdateTest {
    
    @PersistenceContext
    EntityManager em;

    /**
     * 변경감지 테스트
     * @throws Exception
     */
    @Test
    @Rollback(false)
    public void updateTest() throws Exception {

        Book book = em.find(Book.class, 1L);

        //Tx
        book.setName("asdasda"); // 이름 변경

        //Tx commit - 변경 감지(DirtyChecking) : Transaction commit시점에 엔티티의 변경을 감지해서 updateQuery를 날려준다.

        /**
         * [준영속 엔터티] - merge();
         * 단점 :  영속엔터티의 경우 값이 그대로 반영되는 반면, 준영속 엔터티는 데이터가 null인경우 null로 업데이트 해버린다.
         * Book book = new Book();
         * book.setId(form.getId());
         * book.setName(form.getName());
         * book.setPrice(form.getPrice());
         * book.setStockQuantity(form.getStockQuantity());
         * book.setAuthor(form.getAuthor());
         * book.setIsbn(form.getIsbn());
         * em.merge();
         *
         * [영속 엔터티] - @Transactional에 의해 service계층에 값이 넘어감과 동시에 updateQuery를 날려준 뒤
         * Repository에서 persist한번 더함(이미 merge하면서 영속성 컨텍스트에 영속화 되므로 의미없음)
         *
         * Book book = (Book) itemService.findOne(itemId); //영속 엔티티 - @Transactional에 의해 변경감지확인.
         * book.setName(form.getName());
         * book.setPrice(form.getPrice());
         * book.setStockQuantity(form.getStockQuantity());
         * book.setAuthor(form.getAuthor());
         * book.setIsbn(form.getIsbn());
         * itemService.saveItem(book);
         * @Transactional
         * saveItem(Item item){}
         */

    }
}
