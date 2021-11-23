package com.switchfully.order.api.orders;

import com.switchfully.order.ControllerIntegrationTest;
import com.switchfully.order.api.orders.dtos.ItemGroupDto;
import com.switchfully.order.api.orders.dtos.OrderAfterCreationDto;
import com.switchfully.order.api.orders.dtos.OrderCreationDto;
import com.switchfully.order.api.orders.dtos.OrderDto;
import com.switchfully.order.api.orders.dtos.reports.OrdersReportDto;
import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.CustomerRepository;
import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.ItemRepository;
import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.domain.orders.Order;
import com.switchfully.order.domain.orders.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

import static com.switchfully.order.domain.customers.CustomerTestBuilder.aCustomer;
import static com.switchfully.order.domain.items.ItemTestBuilder.anItem;
import static com.switchfully.order.domain.orders.OrderTestBuilder.anOrder;
import static com.switchfully.order.domain.orders.orderitems.OrderItemTestBuilder.anOrderItem;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

class OrderControllerIntegrationTest extends ControllerIntegrationTest {

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private ItemRepository itemRepository;

    @Inject
    private OrderRepository orderRepository;

    @Test
    void createOrder() {
        Customer customer = customerRepository.save(aCustomer().build());
        Item itemOne = itemRepository.save(anItem()
                .withAmountOfStock(10)
                .withPrice(Price.create(BigDecimal.valueOf(10)))
                .build());
        Item itemTwo = itemRepository.save(anItem()
                .withAmountOfStock(7)
                .withPrice(Price.create(BigDecimal.valueOf(2.5)))
                .build());


        OrderCreationDto orderDto = new OrderCreationDto()
                .withCustomerId(customer.getId().toString())
                .withItemGroups(
                        new ItemGroupDto()
                                .withItemId(itemOne.getId().toString())
                                .withOrderedAmount(8),
                        new ItemGroupDto()
                                .withItemId(itemTwo.getId().toString())
                                .withOrderedAmount(5)
                );

        OrderAfterCreationDto orderAfterCreationDto = new TestRestTemplate()
                .postForObject(format("http://localhost:%s/%s", getPort(), OrderController.RESOURCE_NAME), orderDto,
                        OrderAfterCreationDto.class);

        assertThat(orderAfterCreationDto).isNotNull();
        assertThat(orderAfterCreationDto.getOrderId()).isNotNull().isNotEmpty();
        assertThat(orderAfterCreationDto.getTotalPrice()).isEqualTo(92.5f);
    }

    @Test
    void getAllOrders_includeOnlyShippableToday() {
        Customer existingCustomer1 = customerRepository.save(aCustomer().build());
        Item existingItem1 = itemRepository.save(anItem().build());
        Item existingItem2 = itemRepository.save(anItem().build());
        orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem1.getId()).build(),
                        anOrderItem().withItemId(existingItem2.getId()).build())
                .withCustomer(existingCustomer1).build());
        orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem2.getId()).build())
                .withCustomer(existingCustomer1).build());

        OrderDto[] orders = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s?shippableToday=true", getPort(),
                        OrderController.RESOURCE_NAME), OrderDto[].class);

        assertThat(orders).hasSize(2);
        assertThat(orders[0].getItemGroups()).isEmpty();
        assertThat(orders[1].getItemGroups()).isEmpty();
    }

    @Test
    void reorderOrder() {
        Customer customer = customerRepository.save(aCustomer().build());
        Item itemOne = itemRepository.save(anItem()
                .withAmountOfStock(12)
                .withPrice(Price.create(BigDecimal.valueOf(10)))
                .build());
        Order order = orderRepository.save(anOrder()
                .withCustomer(customer)
                .withOrderItems(anOrderItem()
                        .withShippingDateBasedOnAvailableItemStock(itemOne.getAmountOfStock())
                        .withOrderedAmount(6)
                        .withItemPrice(itemOne.getPrice())
                        .withItemId(itemOne.getId())
                        .build())
                .build());

        OrderAfterCreationDto orderAfterCreationDto = new TestRestTemplate()
                .postForObject(format("http://localhost:%s/%s/%s/%s", getPort(), OrderController.RESOURCE_NAME,
                        order.getId(), "reorder"), null, OrderAfterCreationDto.class);

        assertThat(orderAfterCreationDto).isNotNull();
        assertThat(orderAfterCreationDto.getOrderId()).isNotNull().isNotEmpty().isNotEqualTo(order.getId());
        assertThat(orderAfterCreationDto.getTotalPrice()).isEqualTo(60.0f);
        assertThat(orderRepository.getOne(UUID.fromString(orderAfterCreationDto.getOrderId()))).isNotNull();

    }

    @Test
    void getOrdersForCustomerReport() {
        Customer existingCustomer1 = customerRepository.save(aCustomer().build());
        Item existingItem1 = itemRepository.save(anItem().build());
        Item existingItem2 = itemRepository.save(anItem().build());
        Order order1 = orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem1.getId()).build(),
                        anOrderItem().withItemId(existingItem2.getId()).build())
                .withCustomer(existingCustomer1).build());
        Order order2 = orderRepository.save(anOrder()
                .withOrderItems(anOrderItem().withItemId(existingItem2.getId()).build())
                .withCustomer(existingCustomer1).build());

        OrdersReportDto ordersReportDto = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s/%s/%s", getPort(), OrderController.RESOURCE_NAME,
                        "customers", existingCustomer1.getId()), OrdersReportDto.class);

        assertThat(ordersReportDto).isNotNull();
        assertThat(ordersReportDto.getTotalPriceOfAllOrders())
                .isEqualTo(Price.add(order1.getTotalPrice(), order2.getTotalPrice()).getAmountAsFloat());
        assertThat(ordersReportDto.getOrders()).hasSize(2);
        ordersReportDto.getOrders().forEach(order -> {
                    assertThat(order.getOrderId()).isNotEmpty().isNotNull();
                    assertThat(order.getItemGroups()).isNotEmpty();
                });
    }

}
