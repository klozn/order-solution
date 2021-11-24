package com.switchfully.order.domain.orders;

import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.CustomerRepository;
import com.switchfully.order.domain.customers.CustomerTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.switchfully.order.domain.orders.OrderTestBuilder.anOrder;
import static org.mockito.Mockito.when;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void getOrdersForCustomer()  {
        Customer customer = CustomerTestBuilder.aCustomer().withId(UUID.randomUUID()).build();
        Order order1 = anOrder().withCustomer(customer).withId(UUID.randomUUID()).build();
        Order order2 = anOrder().withCustomer(CustomerTestBuilder.anEmptyCustomer().build()).withId(UUID.randomUUID()).build();
        Order order3 = anOrder().withCustomer(customer).withId(UUID.randomUUID()).build();
        //customerRepository.save(customer);
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        List<Order> ordersForCustomer = orderRepository.findAllByCustomer(customer);

        Assertions.assertThat(ordersForCustomer).containsExactlyInAnyOrder(order1, order3);

    }

}
