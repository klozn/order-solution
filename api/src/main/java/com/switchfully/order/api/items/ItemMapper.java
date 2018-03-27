package com.switchfully.order.api.items;

import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.prices.Price;
import com.switchfully.order.infrastructure.dto.Mapper;

import javax.inject.Named;
import java.math.BigDecimal;

import static com.switchfully.order.domain.items.Item.ItemBuilder.item;

@Named
public class ItemMapper extends Mapper<ItemDto, Item> {

    @Override
    public Item toDomain(ItemDto itemDto) {
        return item()
                .withName(itemDto.getName())
                .withDescription(itemDto.getDescription())
                .withAmountOfStock(itemDto.getAmountOfStock())
                .withPrice(Price.create(BigDecimal.valueOf(itemDto.getPrice())))
                .build();
    }

    @Override
    public ItemDto toDto(Item item) {
        return new ItemDto()
                .withId(item.getId())
                .withName(item.getName())
                .withDescription(item.getDescription())
                .withAmountOfStock(item.getAmountOfStock())
                .withPrice(item.getPrice().getAmountAsFloat());
    }
}