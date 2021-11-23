package com.switchfully.order.api.orders;

import com.switchfully.order.api.orders.dtos.OrderAfterCreationDto;
import com.switchfully.order.api.orders.dtos.OrderCreationDto;
import com.switchfully.order.api.orders.dtos.OrderDto;
import com.switchfully.order.api.orders.dtos.reports.OrdersReportDto;
import com.switchfully.order.service.orders.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/" + OrderController.RESOURCE_NAME)
public class OrderController {

    public static final String RESOURCE_NAME = "orders";

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderDto> getAllOrders(@RequestParam(name = "shippableToday", required = false) boolean onlyIncludeShippableToday) {
        return orderService.getAllOrders(onlyIncludeShippableToday).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderAfterCreationDto createOrder(@RequestBody OrderCreationDto orderDto) {
        return orderMapper.toOrderAfterCreationDto(
                orderService.createOrder(
                        orderMapper.toDomain(orderDto)));
    }

    @PostMapping(path = "/{id}/reorder", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderAfterCreationDto reorderOrder(@PathVariable String id) {
        return orderMapper.toOrderAfterCreationDto(
                orderService.reorderOrder(UUID.fromString(id)));
    }

    @GetMapping(path = "/customers/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrdersReportDto getOrdersForCustomerReport(@PathVariable String customerId) {
        return orderMapper.toOrdersReportDto(
                orderService.getOrdersForCustomer(UUID.fromString(customerId)));
    }

}
