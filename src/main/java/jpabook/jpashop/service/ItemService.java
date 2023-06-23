package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
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
