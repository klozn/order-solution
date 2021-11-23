package com.switchfully.order.api.items;

import com.switchfully.order.ControllerIntegrationTest;
import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.ItemRepository;
import com.switchfully.order.domain.items.prices.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static com.switchfully.order.domain.items.ItemTestBuilder.anItem;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemControllerIntegrationTest extends ControllerIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMapper itemMapper;

    @Test
    void createItem() {
        ItemDto itemToCreate = new ItemDto()
                .withName("Half-Life 3")
                .withDescription("Boehoehoe...")
                .withPrice(45.50f)
                .withAmountOfStock(50510);

        ItemDto itemDto = new TestRestTemplate()
                .postForObject(format("http://localhost:%s/%s", getPort(), ItemController.RESOURCE_NAME), itemToCreate, ItemDto.class);

        assertThat(itemDto.getId()).isNotNull().isNotEmpty();
        assertThat(itemDto).isEqualToIgnoringGivenFields(itemToCreate, "id", "stockUrgency");
    }

    @Test
    void getAllItems() {
        Item item1 = itemRepository.save(anItem().withAmountOfStock(12).build());
        Item item2 = itemRepository.save(anItem().withAmountOfStock(2).build());
        Item item3 = itemRepository.save(anItem().withAmountOfStock(16).build());
        Item item4 = itemRepository.save(anItem().withAmountOfStock(8).build());
        Item item5 = itemRepository.save(anItem().withAmountOfStock(4).build());

        ItemDto[] items = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s", getPort(), ItemController.RESOURCE_NAME), ItemDto[].class);

        assertThat(items).hasSize(5);
        assertThat(items[0]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item2));
        assertThat(items[1]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item5));
        assertThat(items[2]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item4));
        assertThat(items[3]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item1));
        assertThat(items[4]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item3));
    }

    @Test
    void getAllItems_givenAStockUrgencyFilter_thenOnlyReturnItemsWithThatUrgency() {
        Item item1 = itemRepository.save(anItem().withAmountOfStock(12).build());
        Item item2 = itemRepository.save(anItem().withAmountOfStock(20).build());
        itemRepository.save(anItem().withAmountOfStock(8).build());
        Item item4 = itemRepository.save(anItem().withAmountOfStock(16).build());
        itemRepository.save(anItem().withAmountOfStock(4).build());

        ItemDto[] items = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s?stockUrgency=STOCK_HIGH", getPort(),
                        ItemController.RESOURCE_NAME), ItemDto[].class);

        assertThat(items).hasSize(3);
        assertThat(items[0]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item1));
        assertThat(items[1]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item4));
        assertThat(items[2]).isEqualToComparingFieldByFieldRecursively(itemMapper.toDto(item2));
    }

    @Test
    void updateItem() {
        Item alreadyExistingItem = itemRepository.save(anItem()
                .withName("Laptop X10")
                .withDescription("A fancy laptop")
                .withAmountOfStock(15)
                .withPrice(Price.create(BigDecimal.valueOf(1199.95)))
                .build());

        ItemDto itemToUpdate = new ItemDto()
                .withId(alreadyExistingItem.getId())
                .withName(alreadyExistingItem.getName())
                .withDescription("A very fancy laptop")
                .withPrice(alreadyExistingItem.getPrice().getAmountAsFloat())
                .withAmountOfStock(10);

        ResponseEntity<ItemDto> result = new TestRestTemplate()
                .exchange(format("http://localhost:%s/%s/%s", getPort(), ItemController.RESOURCE_NAME, alreadyExistingItem.getId().toString()),
                        HttpMethod.PUT,
                        new HttpEntity<>(itemToUpdate),
                        ItemDto.class);

        assertThat(result.getBody()).isEqualToIgnoringGivenFields(itemToUpdate, "stockUrgency");
    }

}
