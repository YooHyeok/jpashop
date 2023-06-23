package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.form.BookForm;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService { // 위임만 하게된다면 바로 Repository에 접근해도 문제가 없다고 생각.
    private final ItemRepository itemRepository;

    /**
     * 상품 등록
     */
    @Transactional(readOnly = false)
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /**
     * 상품 수정 [transaction 변경감지]
     * @param itemId form
     * @param form
     */

    @Transactional(readOnly = false)
    public void updateItem(Long itemId, BookForm form) {
        Book findBook = (Book) itemRepository.findOne(itemId); //영속 엔티티 - @Transactional에 의해 변경감지확인.
        findBook.setId(itemId);
        findBook.setName(form.getName());
        findBook.setPrice(form.getPrice());
        findBook.setStockQuantity(form.getStockQuantity());
        findBook.setAuthor(form.getAuthor());
        findBook.setIsbn(form.getIsbn());
    }

    /**
     * 상품 단건 조회
     */
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    /**
     * 상품 전체 조회
     */
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

}
