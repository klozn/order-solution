package com.switchfully.order.api.orders;

import com.switchfully.order.api.orders.dtos.ItemGroupDto;
import com.switchfully.order.api.orders.dtos.reports.ItemGroupReportDto;
import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.domain.orders.orderitems.OrderItem;
import com.switchfully.order.infrastructure.dto.Mapper;
import com.switchfully.order.infrastructure.exceptions.EntityNotFoundException;
import com.switchfully.order.service.items.ItemService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.UUID;

import static com.switchfully.order.domain.orders.orderitems.OrderItem.OrderItemBuilder.orderItem;

@Named
public class OrderItemMapper extends Mapper<ItemGroupDto, OrderItem> {

    private final ItemService itemService;

    @Inject
    public OrderItemMapper(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public OrderItem toDomain(ItemGroupDto itemGroupDto) {
        return orderItem()
                .withItemId(UUID.fromString(itemGroupDto.getItemId()))
                .withOrderedAmount(itemGroupDto.getOrderedAmount())
                .withItemPrice(enrichWithItemPrice(itemGroupDto))
                .withShippingDateBasedOnAvailableItemStock(enrichWithItemAmountOfStock(itemGroupDto))
                .build();
    }

    @Override
    public ItemGroupDto toDto(OrderItem orderItem) {
        return new ItemGroupDto()
                .withItemId(orderItem.getItemId().toString())
                .withOrderedAmount(orderItem.getOrderedAmount());
    }

    public ItemGroupReportDto toItemGroupReportDto(OrderItem orderItem) {
        return new ItemGroupReportDto()
                .withItemId(orderItem.getItemId().toString())
                .withOrderedAmount(orderItem.getOrderedAmount())
                .withName(enrichWithItemName(orderItem))
                .withTotalPrice(orderItem.getTotalPrice().getAmountAsFloat());
    }

    private Price enrichWithItemPrice(ItemGroupDto itemGroupDto) {
        return getItemForId(itemGroupDto.getItemId()).getPrice();
    }

    private int enrichWithItemAmountOfStock(ItemGroupDto itemGroupDto) {
        return getItemForId(itemGroupDto.getItemId()).getAmountOfStock();
    }

    private String enrichWithItemName(OrderItem orderItem) {
        return getItemForId(orderItem.getItemId().toString()).getName();
    }

    private Item getItemForId(String itemIdAsString) {
        Item item = itemService.getItem(UUID.fromString(itemIdAsString));
        if (item == null) {
            throw new EntityNotFoundException("mapping to an order of an item group (for creating a new order)",
                    Item.class, UUID.fromString(itemIdAsString));
        }
        return item;
    }

}
