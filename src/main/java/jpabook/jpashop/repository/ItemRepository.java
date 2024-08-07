package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /**
     * item 저장
     * @param item
     */
    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);  // item의 id가 없다? -> 새로 생성하는 객체이다.
        } else {
            em.merge(item);  // 있다? -> update 느낌..? -> ItemService의 updateItem 역할을 함
        }
    }

    /**
     * item 하나만 조회
     * @param id
     * @return
     */
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    /**
     * 상품 전체 조회
     * @return
     */
    public List<Item> findAll() {
        return em.createQuery("SELECT i FROM Item i", Item.class)
                .getResultList();
    }
}
