package com.switchfully.order.service.orders;

import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.CustomerRepository;
import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.ItemRepository;
import com.switchfully.order.domain.orders.Order;
import com.switchfully.order.domain.orders.OrderRepository;
import com.switchfully.order.domain.orders.orderitems.OrderItem;
import com.switchfully.order.domain.orders.orderitems.events.OrderItemCreatedEvent;
import com.switchfully.order.infrastructure.exceptions.EntityNotFoundException;
import com.switchfully.order.infrastructure.exceptions.EntityNotValidException;
import com.switchfully.order.infrastructure.exceptions.NotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.switchfully.order.domain.orders.Order.OrderBuilder.order;

@Service
@Transactional
public class OrderService {

    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderService(CustomerRepository customerRepository,
                        ItemRepository itemRepository,
                        OrderRepository orderRepository,
                        OrderValidator orderValidator,
                        ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
        this.eventPublisher = eventPublisher;
    }

    public Order createOrder(Order order) {
        assertOrderIsValidForCreation(order);
        assertOrderingCustomerExists(order);
        assertAllOrderedItemsExist(order);
        publishCreationOfOrderItems(order);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersForCustomer(UUID customerId) {
        Customer customer = customerRepository.getOne(customerId);
        return orderRepository.findAllByCustomer(customer);
    }

    public Order reorderOrder(UUID orderId) {
        Order orderToReorder = orderRepository.getOne(orderId);
        assertCustomerIsOwnerOfOrderToReorder(orderId, orderToReorder);
        publishCreationOfOrderItems(orderToReorder);
        return orderRepository.save(order()
                .withCustomer(orderToReorder.getCustomer())
                .withOrderItems(copyOrderItemsWithRecentPrice(orderToReorder.getOrderItems()))
                .build());
    }

    public List<Order> getAllOrders(boolean onlyIncludeShippableToday) {
        if (onlyIncludeShippableToday) {
            return getOrdersOnlyContainingOrderItemsShippingToday();
        }
        return orderRepository.findAll();
    }

    private void publishCreationOfOrderItems(Order order) {
        order.getOrderItems().forEach(orderItem -> eventPublisher.publishEvent(new OrderItemCreatedEvent(orderItem)));
    }

    private List<Order> getOrdersOnlyContainingOrderItemsShippingToday() {
        return orderRepository.findAll().stream()
                .map(order -> order()
                        .withCustomer(order.getCustomer())
                        .withId(order.getId())
                        .withOrderItems(getOrderItemsShippingToday(order))
                        .build())
                .collect(Collectors.toList());
    }

    private List<OrderItem> getOrderItemsShippingToday(Order order) {
        return order.getOrderItems().stream()
                .filter(orderItem -> orderItem.getShippingDate().isEqual(LocalDate.now()))
                .collect(Collectors.toList());
    }

    private void assertAllOrderedItemsExist(Order order) {
        if (!doAllOrderItemsReferenceAnExistingItem(order.getOrderItems())) {
            throw new EntityNotValidException("creation of a new order when checking if all the ordered items exist",
                    order);
        }
    }

    private boolean doAllOrderItemsReferenceAnExistingItem(List<OrderItem> orderItems) {
        return orderItems.stream()
                .filter(orderItem -> itemRepository.existsById(orderItem.getItemId()))
                .map(nonExistingItem -> false)
                .findFirst()
                .orElse(true);
    }

    private void assertOrderingCustomerExists(Order order) {
        if (!doesCustomerExist(order)) {
            throw new EntityNotFoundException("creation of a new order when checking if the referenced customer exists",
                    Customer.class, order.getCustomer().getId());
        }
    }

    private boolean doesCustomerExist(Order order) {
        return customerRepository.existsById(order.getCustomer().getId());
    }

    private void assertOrderIsValidForCreation(Order order) {
        if (!orderValidator.isValidForCreation(order)) {
            orderValidator.throwInvalidStateException(order, "creation");
        }
    }

    private List<OrderItem> copyOrderItemsWithRecentPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem -> {
                            Item item = itemRepository.getOne(orderItem.getItemId());
                            return OrderItem.OrderItemBuilder.orderItem()
                                    .withItemId(orderItem.getItemId())
                                    .withOrderedAmount(orderItem.getOrderedAmount())
                                    .withShippingDateBasedOnAvailableItemStock(item.getAmountOfStock())
                                    .withItemPrice(item.getPrice())
                                    .build();
                        }
                ).collect(Collectors.toList());
    }

    private void assertCustomerIsOwnerOfOrderToReorder(UUID orderId, Order orderToReorder) {
        if (!doesOrderToReorderBelongToAuthenticatedUser(orderToReorder.getCustomer())) {
            throw new NotAuthorizedException("Customer " + orderToReorder.getCustomer().toString() + " is not allowed " +
                    "to reorder the Order " + orderId.toString() + " because he's not the owner of that order!");
        }
    }

    /**
     * Normally, when using Spring Authentication, we could check here if the customerId is equal to the
     * id of the authenticated (logged-in) customer. Since Spring Security is out of scope for this solution, therefore,
     * we simply check if the customer exists.
     */
    private boolean doesOrderToReorderBelongToAuthenticatedUser(Customer customer) {
        return customerRepository.existsById(customer.getId());
    }
}
