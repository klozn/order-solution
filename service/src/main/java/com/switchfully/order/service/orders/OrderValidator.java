package com.switchfully.order.service.orders;

import com.switchfully.order.domain.EntityValidator;
import com.switchfully.order.domain.orders.Order;
import com.switchfully.order.domain.orders.orderitems.OrderItem;

import javax.inject.Named;
import java.util.List;

/**
 * Instead of our custom validator(s),
 * we could also use Javax Validation (we didn't use it here, to not over-complexify the solution).
 * That way, we can indicate on our Domain classes (such as Customer and Item)
 * what fields are required and what the valid values are for each field.
 */
@Named
public class OrderValidator extends EntityValidator<Order> {

    @Override
    protected boolean isAFieldEmptyOrNull(Order order) {
        return isNull(order)
                || isNull(order.getCustomer())
                || order.getOrderItems().size() < 1
                || isAnOrderItemInvalid(order.getOrderItems());
    }

    private boolean isAnOrderItemInvalid(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem ->
                        isNull(orderItem.getItemId())
                                || isNull(orderItem.getItemPrice())
                                || orderItem.getItemPrice().getAmountAsFloat() <= 0
                                || orderItem.getOrderedAmount() <= 0
                                || isNull(orderItem.getShippingDate()))
                .filter(isInvalidOrderItem -> isInvalidOrderItem)
                .findFirst()
                .orElse(false);
    }

}
