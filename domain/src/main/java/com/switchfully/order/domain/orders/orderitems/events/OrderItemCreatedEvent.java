package com.switchfully.order.domain.orders.orderitems.events;

import com.switchfully.order.domain.orders.orderitems.OrderItem;
import org.springframework.context.ApplicationEvent;

/**
 * An event that is thrown when an OrderItem is created
 */
public class OrderItemCreatedEvent extends ApplicationEvent {

    private final OrderItem orderItem;

    public OrderItemCreatedEvent(OrderItem orderItem) {
        super(orderItem);
        this.orderItem = orderItem;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }
}
