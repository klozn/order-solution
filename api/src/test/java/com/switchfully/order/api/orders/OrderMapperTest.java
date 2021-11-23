package com.switchfully.order.api.orders;

import com.switchfully.order.api.customers.addresses.AddressDto;
import com.switchfully.order.api.customers.addresses.AddressMapper;
import com.switchfully.order.api.orders.dtos.ItemGroupDto;
import com.switchfully.order.api.orders.dtos.OrderAfterCreationDto;
import com.switchfully.order.api.orders.dtos.OrderCreationDto;
import com.switchfully.order.api.orders.dtos.OrderDto;
import com.switchfully.order.api.orders.dtos.reports.ItemGroupReportDto;
import com.switchfully.order.api.orders.dtos.reports.OrdersReportDto;
import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.addresses.Address;
import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.domain.orders.Order;
import com.switchfully.order.domain.orders.orderitems.OrderItem;
import com.switchfully.order.service.customers.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static com.switchfully.order.domain.customers.CustomerTestBuilder.aCustomer;
import static com.switchfully.order.domain.customers.addresses.AddressTestBuilder.anAddress;
import static com.switchfully.order.domain.orders.OrderTestBuilder.anOrder;
import static com.switchfully.order.domain.orders.orderitems.OrderItemTestBuilder.anOrderItem;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class OrderMapperTest {

    private OrderItemMapper orderItemMapperMock;
    private AddressMapper addressMapper;
    private CustomerService customerService;
    private OrderMapper orderMapper;

    @BeforeEach
    void setupService() {
        orderItemMapperMock = Mockito.mock(OrderItemMapper.class);
        addressMapper = Mockito.mock(AddressMapper.class);
        customerService = Mockito.mock(CustomerService.class);
        orderMapper = new OrderMapper(orderItemMapperMock, addressMapper, customerService);
    }

    @Test
    void toDomain() {
        OrderItem orderItem = anOrderItem().build();
        when(orderItemMapperMock.toDomain(any(ItemGroupDto.class))).thenReturn(orderItem);

        String customerId = UUID.randomUUID().toString();
        Order order = orderMapper.toDomain(new OrderCreationDto()
                .withCustomerId(customerId)
                .withItemGroups(new ItemGroupDto()
                                .withItemId(UUID.randomUUID().toString())
                                .withOrderedAmount(5),
                        new ItemGroupDto()
                                .withItemId(UUID.randomUUID().toString())
                                .withOrderedAmount(1)));

        assertThat(order).isNotNull();
        assertThat(order.getCustomer().getId().toString()).isEqualTo(customerId);
        assertThat(order.getOrderItems()).containsExactlyInAnyOrder(orderItem, orderItem);
    }

    @Test
    void toDto() {
        Customer customer = aCustomer().withId(UUID.randomUUID()).build();
        UUID orderId = UUID.randomUUID();
        Order order = anOrder().withCustomer(customer).withId(orderId).build();
        Address address = anAddress().build();
        when(customerService.getCustomer(customer.getId()))
                .thenReturn(aCustomer()
                        .withAddress(address)
                        .build());
        AddressDto addressDto = new AddressDto().withStreetName("Hellostreet");
        when(addressMapper.toDto(address)).thenReturn(addressDto);

        OrderDto orderDto = orderMapper.toDto(order);

        assertThat(orderDto).isNotNull();
        assertThat(orderDto.getOrderId()).isEqualTo(orderId.toString());
        assertThat(orderDto.getAddress()).isEqualToComparingFieldByField(addressDto);
        assertThat(orderDto.getItemGroups()).isNotEmpty();

    }

    @Test
    void toOrderAfterCreationDto() {
        Order order = anOrder().withId(UUID.randomUUID()).build();

        OrderAfterCreationDto orderAfterCreationDto = orderMapper.toOrderAfterCreationDto(order);

        assertThat(orderAfterCreationDto).isNotNull();
        assertThat(orderAfterCreationDto.getOrderId()).isEqualTo(order.getId().toString());
        assertThat(orderAfterCreationDto.getTotalPrice()).isEqualTo(order.getTotalPrice().getAmountAsFloat());
    }

    @Test
    void toOrdersReportDto() {
        OrderItem orderItem1 = anOrderItem().build();
        Order order1 = anOrder().withId(UUID.randomUUID()).withOrderItems(orderItem1).build();
        ItemGroupReportDto itemGroupReportDto = new ItemGroupReportDto();
        when(orderItemMapperMock.toItemGroupReportDto(orderItem1))
                .thenReturn(itemGroupReportDto);

        OrdersReportDto ordersReportDto = orderMapper.toOrdersReportDto(asList(order1));

        assertThat(ordersReportDto).isNotNull();
        assertThat(ordersReportDto.getTotalPriceOfAllOrders())
                .isEqualTo(orderItem1.getTotalPrice().getAmountAsFloat());
        assertThat(ordersReportDto.getOrders()).hasSize(1);
        assertThat(ordersReportDto.getOrders().get(0)).isNotNull();
        assertThat(ordersReportDto.getOrders().get(0).getOrderId()).isEqualTo(order1.getId().toString());
        assertThat(ordersReportDto.getOrders().get(0).getItemGroups()).containsExactly(itemGroupReportDto);
    }

    @Test
    void toOrdersReportDto_multipleOrderItems() {
        OrderItem orderItem1 = anOrderItem().withItemPrice(Price.create(BigDecimal.valueOf(35))).build();
        OrderItem orderItem2 = anOrderItem().withItemPrice(Price.create(BigDecimal.valueOf(45))).build();
        OrderItem orderItem3 = anOrderItem().withItemPrice(Price.create(BigDecimal.valueOf(40))).build();
        Order order1 = anOrder().withId(UUID.randomUUID()).withOrderItems(orderItem1, orderItem2).build();
        Order order2 = anOrder().withId(UUID.randomUUID()).withOrderItems(orderItem3).build();
        ItemGroupReportDto itemGroupReportDto = new ItemGroupReportDto();
        when(orderItemMapperMock.toItemGroupReportDto(any(OrderItem.class))).thenReturn(eq(itemGroupReportDto));

        OrdersReportDto ordersReportDto = orderMapper.toOrdersReportDto(asList(order1, order2));

        assertThat(ordersReportDto).isNotNull();
        assertThat(ordersReportDto.getTotalPriceOfAllOrders())
                .isEqualTo(Price.add(orderItem3.getTotalPrice(),
                        Price.add(orderItem1.getTotalPrice(), orderItem2.getTotalPrice()))
                        .getAmountAsFloat());
        assertThat(ordersReportDto.getOrders()).hasSize(2);
    }

}
