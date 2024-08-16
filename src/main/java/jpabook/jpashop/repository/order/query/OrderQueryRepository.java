package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    /**
     * 화면과 밀접한 것들은 쿼리랑 밀접한 것들이 많음. -> query 에 넣음
     * 핵심 비즈니스 로직들을 참조하면서 진행 -> 관심사 분리
     */

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        // 루트 조회(ToOne 코드를 모두 한 번에 조회)
        List<OrderQueryDto> result = findOrders();  // query 1번 -> 1개

        // 루프를 돌면서 컬렉션 추가(추가 쿼리 실행)
        result.forEach(o -> {  // 루프를 돌면서 직접 처리
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());  // query 2번 -> N개
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();

    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "SELECT new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                "FROM Order o " +
                                "JOIN o.member m " +
                                "JOIN o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        // 루트 조회(toOne 코드를 모두 한 번에 조회)
        List<OrderQueryDto> result = findOrders();

        // 현재 주문서와 관련된 orderItems를 한번에 조회
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        // 루프를 돌면서 컬렉션 추가(추가 쿼리 실행 X)
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                                "from OrderItem oi " +
                                "join oi.item i " +
                                "where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }
}
