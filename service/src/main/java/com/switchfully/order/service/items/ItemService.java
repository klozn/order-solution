package com.switchfully.order.service.items;

import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @Autowired
    public ItemService(ItemRepository itemRepository, ItemValidator itemValidator) {
        this.itemRepository = itemRepository;
        this.itemValidator = itemValidator;
    }

    public Item createItem(Item item) {
        if (!itemValidator.isValidForCreation(item)) {
            itemValidator.throwInvalidStateException(item, "creation");
        }
        return itemRepository.save(item);
    }

    public Item updateItem(Item item) {
        if (!itemValidator.isValidForUpdating(item)) {
            itemValidator.throwInvalidStateException(item, "updating");
        }
        return itemRepository.save(item);
    }

    public Item getItem(UUID itemId) {
        return itemRepository.getOne(itemId);
    }

    public void decrementStockForItem(UUID itemId, int amountToDecrement) {
        Item item = itemRepository.getOne(itemId);
        item.decrementStock(amountToDecrement);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
}
