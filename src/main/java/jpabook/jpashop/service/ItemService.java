package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final EntityManager em;

    @Transactional  // 저장이므로 추가해야됨
    public void save(Item item) {
        itemRepository.save(item);
    }

    /**
     * 변경 감지 기능 사용 - merge()를 사용하는 것보다 훨씬 안전하므로 실무에서는 변경 감지를 사용할 것
     * @param itemId
     * @param param
     */
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
        //itemRepository.save(findItem);  // 을 할 필요가 없음 -> 이게 없어도 @Transactional에 의해 자동으로 데이터가 수정됨
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
