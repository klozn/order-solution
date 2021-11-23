package com.switchfully.order.service.items;

import com.switchfully.order.domain.items.Item;
import com.switchfully.order.domain.items.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.switchfully.order.domain.items.ItemTestBuilder.anItem;

class ItemServiceTest {

    private ItemService itemService;
    private ItemValidator itemValidatorMock;
    private ItemRepository itemRepositoryMock;

    @BeforeEach
    void setupService() {
        itemRepositoryMock = Mockito.mock(ItemRepository.class);
        itemValidatorMock = Mockito.mock(ItemValidator.class);
        itemService = new ItemService(itemRepositoryMock, itemValidatorMock);
    }

    @Test
    void createItem_happyPath() {
        Item item = anItem().build();
        Mockito.when(itemValidatorMock.isValidForCreation(item)).thenReturn(true);
        Mockito.when(itemRepositoryMock.save(item)).thenReturn(item);

        Item createdItem = itemService.createItem(item);

        Assertions.assertThat(createdItem).isNotNull();
    }

    @Test
    void createItem_givenItemThatIsNotValidForCreation_thenThrowException() {
        Item item = anItem().build();
        Mockito.when(itemValidatorMock.isValidForCreation(item)).thenReturn(false);
        Mockito.doThrow(IllegalStateException.class).when(itemValidatorMock)
                .throwInvalidStateException(item, "creation");

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> itemService.createItem(item));
    }

    @Test
    void updateItem_happyPath() {
        Item item = anItem().build();
        Mockito.when(itemValidatorMock.isValidForUpdating(item)).thenReturn(true);
        Mockito.when(itemRepositoryMock.save(item)).thenReturn(item);

        Item updatedItem = itemService.updateItem(item);

        Assertions.assertThat(updatedItem).isNotNull();
    }

    @Test
    void updateItem_givenItemThatIsNotValidForUpdating_thenThrowException() {
        Item item = anItem().build();
        Mockito.when(itemValidatorMock.isValidForUpdating(item)).thenReturn(false);
        Mockito.doThrow(IllegalStateException.class).when(itemValidatorMock)
                .throwInvalidStateException(item, "updating");

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> itemService.updateItem(item));
    }

}
