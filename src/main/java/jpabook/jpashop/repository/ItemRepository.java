package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /**
     * 상품 등록
     */
    public void save(Item item) {
        if (item.getId() == null) { //item id가 존재하지 않으면 영속성 컨텍스트에 등록
            em.persist(item);
        } else { //item id가 존재한다면 merge(수정)?
            em.merge(item);
        }
    }

    /**
     * 상품 단건 검색
     */
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    /**
     * 상품 전체 검색
     */
    public List<Item> findAll() {
        return em.createQuery("select i from Item i",Item.class).getResultList();
    }
}
