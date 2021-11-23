package com.switchfully.order.service.orders;

import com.switchfully.order.domain.orders.orderitems.OrderItemTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.switchfully.order.domain.orders.OrderTestBuilder.anOrder;
import static org.assertj.core.api.Assertions.assertThat;

class OrderValidatorTest {

    @Test
    void isValidForCreation_happyPath() {
        assertThat(new OrderValidator()
                .isValidForCreation(anOrder().build()))
                .isTrue();
    }

    @Test
    void isValidForCreation_givenAnId_thenNotValidForCreation() {
        assertThat(new OrderValidator()
                .isValidForCreation(anOrder()
                        .withId(UUID.randomUUID())
                        .build()))
                .isFalse();
    }

    /**
     * To have this code properly tested,
     * we should create a test for each individual empty or null value.
     * However, since this is not an application intended for production, we didn't.
     * Check the ItemValidatorTest for a better example.
     */
    @Test
    void isValidForCreation_givenSomeMissingValues_thenNotValidForCreation() {
        assertThat(new OrderValidator()
                .isValidForCreation(anOrder()
                        .withCustomer(null)
                        .withOrderItems(OrderItemTestBuilder.anOrderItem()
                                .withItemPrice(null)
                                .withOrderedAmount(0)
                                .build())
                        .build()))
                .isFalse();
    }

}
