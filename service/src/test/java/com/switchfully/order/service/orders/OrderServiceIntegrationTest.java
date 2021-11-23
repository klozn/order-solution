package com.switchfully.order.service.orders;

import com.switchfully.order.IntegrationTest;
import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.CustomerRepository;
import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.ItemRepository;
import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.domain.orders.Order;
import com.switchfully.order.domain.orders.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.switchfully.order.domain.customers.CustomerTestBuilder.aCustomer;
import static com.switchfully.order.domain.items.ItemTestBuilder.anItem;
import static com.switchfully.order.domain.orders.OrderTestBuilder.anOrder;
import static com.switchfully.order.domain.orders.orderitems.OrderItemTestBuilder.anOrderItem;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createOrder() {
        Item existingItem = itemRepository.save(anItem()
                .withAmountOfStock(20)
                .build());
        Customer existingCustomer = customerRepository.save(aCustomer().build());
        Order orderToCreate = anOrder()
                .withCustomer(existingCustomer)
                .withOrderItems(anOrderItem()
                        .withItemId(existingItem.getId())
                        .withOrderedAmount(15)
                        .build())
                .build();

        Order createdOrder = orderService.createOrder(orderToCreate);

        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder).isEqualToComparingFieldByFieldRecursively(orderToCreate);
        assertThat(itemRepository.getOne(existingItem.getId()).getAmountOfStock()).isEqualTo(5);
    }

    @Test
    void getAllOrders() {
        Item item = itemRepository.save(anItem().build());
        Order order1 = orderRepository.save(anOrder().withOrderItems(anOrderItem().withItemId(item.getId()).build()).build());
        Order order2 = orderRepository.save(anOrder().withOrderItems(anOrderItem().withItemId(item.getId()).build()).build());
        Order order3 = orderRepository.save(anOrder().withOrderItems(anOrderItem().withItemId(item.getId()).build()).build());

        List<Order> allOrders = orderService.getAllOrders(false);

        assertThat(allOrders).containsExactlyInAnyOrder(order1, order2, order3);
    }

    @Test
    void getAllOrders_onlyIncludeShippableToday() {
        Item item = itemRepository.save(anItem().build());
        Order order1 = orderRepository.save(anOrder().withOrderItems(anOrderItem().withItemId(item.getId()).build()).build());

        List<Order> allOrders = orderService.getAllOrders(true);

        assertThat(allOrders).hasSize(1);
        assertThat(allOrders.get(0)).isEqualToIgnoringGivenFields(order1, "orderItems");
        assertThat(allOrders.get(0).getOrderItems()).isEmpty();
    }

    @Test
    void getOrdersForCustomer(){
        Customer existingCustomer1 = customerRepository.save(aCustomer().build());
        Customer existingCustomer2 = customerRepository.save(aCustomer().build());
        Item existingItem1 = itemRepository.save(anItem().build());
        Item existingItem2 = itemRepository.save(anItem().build());
        Order order1 = orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem1.getId()).build(),
                        anOrderItem().withItemId(existingItem2.getId()).build())
                .withCustomer(existingCustomer1).build());
        Order order2 = orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem2.getId()).build())
                .withCustomer(existingCustomer2).build());
        Order order3 = orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem1.getId()).build())
                .withCustomer(existingCustomer2).build());

        List<Order> ordersForCustomer = orderService.getOrdersForCustomer(existingCustomer2.getId());

        assertThat(ordersForCustomer).containsExactlyInAnyOrder(order2, order3);
    }

    @Test
    void reorderOrder() {
        Customer customerOfOrder = customerRepository.save(aCustomer().build());
        Item itemFromOrder = itemRepository.save(anItem()
                .withPrice(Price.create(BigDecimal.valueOf(8.0)))
                .withAmountOfStock(12)
                .build());
        Order originalOrder = orderRepository.save(anOrder()
                .withCustomer(customerOfOrder)
                .withOrderItems(anOrderItem()
                        .withItemId(itemFromOrder.getId())
                        .withItemPrice(Price.create(BigDecimal.valueOf(9.95)))
                        .withOrderedAmount(5)
                        .withShippingDateBasedOnAvailableItemStock(10)
                        .build())
                .build());

        Order orderFromReorder = orderService.reorderOrder(originalOrder.getId());

        assertThat(orderFromReorder).isNotNull();
        assertThat(orderFromReorder.getTotalPrice().sameAs(Price.create(BigDecimal.valueOf(40.0)))).isTrue();
        assertThat(orderFromReorder.getCustomer()).isEqualTo(customerOfOrder);
        assertThat(orderFromReorder.getOrderItems()).hasSize(1);
        assertThat(orderFromReorder.getOrderItems().get(0).getShippingDate().isAfter(LocalDate.now().plusDays(2)));
        assertThat(orderFromReorder.getOrderItems().get(0).getOrderedAmount()).isEqualTo(5);
        assertThat(orderFromReorder.getOrderItems().get(0).getTotalPrice().sameAs(Price.create(BigDecimal.valueOf(40.0)))).isTrue();
        assertThat(orderFromReorder.getOrderItems().get(0).getItemPrice().sameAs(Price.create(BigDecimal.valueOf(8.0)))).isTrue();
        assertThat(orderFromReorder.getOrderItems().get(0).getItemId()).isEqualTo(itemFromOrder.getId());
        assertThat(itemRepository.getOne(itemFromOrder.getId()).getAmountOfStock()).isEqualTo(2);
    }

}
