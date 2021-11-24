package com.switchfully.order.domain.orders.orderitems;

import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.infrastructure.builder.Builder;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * OrderItem is a fabricated (value) object consisting of the original Item's id and price, enriched with
 * order-specific information (the ordered amount and the shipping date).
 */
@Entity
@Table(name = "order_item")
public final class OrderItem extends com.switchfully.order.domain.Entity {

    private UUID itemId;
    private Price itemPrice;
    private int orderedAmount;
    private LocalDate shippingDate;

    public OrderItem() {
    }

    public OrderItem(OrderItemBuilder orderItemBuilder, Clock clock) {
        itemId = orderItemBuilder.itemId;
        itemPrice = orderItemBuilder.itemPrice;
        orderedAmount = orderItemBuilder.orderedAmount;
        shippingDate = calculateShippingDate(orderItemBuilder.availableItemStock, clock);
    }

    private LocalDate calculateShippingDate(int availableItemStock, Clock clock) {
        if(availableItemStock - orderedAmount >= 0) {
            return LocalDate.now(clock).plusDays(1);
        } return LocalDate.now(clock).plusDays(7);
    }

    public UUID getItemId() {
        return itemId;
    }

    public Price getItemPrice() {
        return itemPrice;
    }

    public int getOrderedAmount() {
        return orderedAmount;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public Price getTotalPrice() {
        return Price.create(itemPrice.getAmount()
                .multiply(BigDecimal.valueOf(orderedAmount)));
    }

    @Override
    public String toString() {
        return "OrderItem{" + "itemId=" + itemId +
                ", itemPrice=" + itemPrice +
                ", orderedAmount=" + orderedAmount +
                ", shippingDate=" + shippingDate +
                '}';
    }

    public static class OrderItemBuilder extends Builder<OrderItem> {

        private UUID itemId;
        private Price itemPrice;
        private int orderedAmount;
        private int availableItemStock;

        private OrderItemBuilder() {
        }

        public static OrderItemBuilder orderItem() {
            return new OrderItemBuilder();
        }

        @Override
        public OrderItem build() {
            return new OrderItem(this, Clock.system(ZoneId.systemDefault()));
        }

        public OrderItemBuilder withItemId(UUID itemId) {
            this.itemId = itemId;
            return this;
        }

        public OrderItemBuilder withItemPrice(Price itemPrice) {
            this.itemPrice = itemPrice;
            return this;
        }

        public OrderItemBuilder withOrderedAmount(int orderedAmount) {
            this.orderedAmount = orderedAmount;
            return this;
        }

        public OrderItemBuilder withShippingDateBasedOnAvailableItemStock(int availableItemStock) {
            this.availableItemStock = availableItemStock;
            return this;
        }
    }

}
