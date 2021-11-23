package com.switchfully.order.domain.orders;

import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.domain.orders.orderitems.OrderItem;
import com.switchfully.order.infrastructure.builder.Builder;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends com.switchfully.order.domain.Entity {

    @Transient
    private List<OrderItem> orderItems;
    @ManyToOne
    @JoinColumn(name = "customerId", referencedColumnName = "id")
    private Customer customer;

    public Order() {
    }

    public Order(OrderBuilder orderBuilder) {
        super.setId(orderBuilder.id);
        orderItems = orderBuilder.orderItems;
        customer = orderBuilder.customer;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public Customer getCustomer() {
        return customer;
    }

    public Price getTotalPrice() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Price.create(BigDecimal.ZERO),
                        (price1, price2) -> Price.create(price1.getAmount().add(price2.getAmount())));
    }

    @Override
    public String toString() {
        return "Order{"
                + "id=" + getId() +
                ", orderItems=" + orderItems +
                ", customerId=" + customer.getId() +
                '}';
    }

    public static class OrderBuilder extends Builder<Order> {

        private UUID id;
        private List<OrderItem> orderItems;
        private Customer customer;

        private OrderBuilder() {
        }

        public static OrderBuilder order() {
            return new OrderBuilder();
        }

        @Override
        public Order build() {
            return new Order(this);
        }

        public OrderBuilder withId(UUID id) {
            this.id = id;
            return this;
        }

        public OrderBuilder withOrderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public OrderBuilder withCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }
    }

}
