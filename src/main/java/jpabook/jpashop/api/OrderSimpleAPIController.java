package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne(ManyToOne, OneToOne)
 * - Order
 * - Order -> Member
 * - Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleAPIController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5JakartaModule 모듈 등록. LAZY=null 처리
     * 주의 및 참고 :
     * - 양방향 관계 문제 발생 -> 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳 중 한 곳에 @JsonIgnore 처리해야 됨.(추가하지 않으면 양쪽에서 서로 후출하면서 무한루프 발생함)
     * - 엔티티를 API 응답으로 외부로 노출하면 안됨. -> Hibernate5JakartaModule를 사용하는 것 보다는 DTO로 변환해서 반환하는 것이 좋음
     * - 항상 지연 로딩(LAZY)를 기본으로 설정해라. -> 즉시 로딩(EAGER) 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제 발생 가능성이 있다.
     *    따라서 성능 최적화가 필요한 경우에는(fetch join)을 사용할 것(v3에서 설명)
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();  // Lazy 강제 초기화
            order.getDelivery().getAddress();  // Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        // Order 2개
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();  // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();  // LAZY 초기화
        }
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        // repository는 순수한 엔티티를 조회하는데 사용
        // orderSimpleQueryRepository.findOrderDtos()는 특별한 경우이므로 따로 분리(repository에 있으면 용도가 애매해짐)
        return orderSimpleQueryRepository.findOrderDtos();
    }
}
