package com.guitarfactory.service;

import com.guitarfactory.domain.entity.Component;
import com.guitarfactory.domain.entity.InventoryItem;
import com.guitarfactory.domain.enums.ComponentType;
import com.guitarfactory.domain.enums.WoodType;
import com.guitarfactory.exception.InsufficientInventoryException;
import com.guitarfactory.exception.ResourceNotFoundException;
import com.guitarfactory.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Component component;
    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        component = Component.builder()
                .id(1L)
                .name("Alder Body Blank")
                .componentType(ComponentType.BODY_BLANK)
                .woodType(WoodType.ALDER)
                .unitPrice(new BigDecimal("80.00"))
                .build();

        inventoryItem = InventoryItem.builder()
                .id(1L)
                .component(component)
                .quantityInStock(50)
                .build();
    }

    @Test
    void findAll_returnsAllItems() {
        when(inventoryItemRepository.findAll()).thenReturn(List.of(inventoryItem));

        List<InventoryItem> result = inventoryService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComponent().getName()).isEqualTo("Alder Body Blank");
    }

    @Test
    void findByComponentId_returnsItemWhenFound() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));

        InventoryItem result = inventoryService.findByComponentId(1L);

        assertThat(result.getQuantityInStock()).isEqualTo(50);
    }

    @Test
    void findByComponentId_throwsWhenNotFound() {
        when(inventoryItemRepository.findByComponentId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.findByComponentId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void isAvailable_returnsTrueWhenSufficientStock() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));

        assertThat(inventoryService.isAvailable(1L, 50)).isTrue();
    }

    @Test
    void isAvailable_returnsTrueWhenExactStock() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));

        assertThat(inventoryService.isAvailable(1L, 50)).isTrue();
    }

    @Test
    void isAvailable_returnsFalseWhenInsufficientStock() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));

        assertThat(inventoryService.isAvailable(1L, 51)).isFalse();
    }

    @Test
    void isAvailable_returnsFalseWhenItemNotFound() {
        when(inventoryItemRepository.findByComponentId(99L)).thenReturn(Optional.empty());

        assertThat(inventoryService.isAvailable(99L, 1)).isFalse();
    }

    @Test
    void consume_reducesStockByGivenQuantity() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.consume(1L, 5);

        assertThat(inventoryItem.getQuantityInStock()).isEqualTo(45);
        verify(inventoryItemRepository).save(inventoryItem);
    }

    @Test
    void consume_throwsWhenItemNotFound() {
        when(inventoryItemRepository.findByComponentId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.consume(99L, 1))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    void consume_throwsWhenInsufficientStock() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));

        assertThatThrownBy(() -> inventoryService.consume(1L, 100))
                .isInstanceOf(InsufficientInventoryException.class)
                .hasMessageContaining("Insufficient stock");

        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    void consume_throwsWhenStockIsZero() {
        inventoryItem.setQuantityInStock(0);
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));

        assertThatThrownBy(() -> inventoryService.consume(1L, 1))
                .isInstanceOf(InsufficientInventoryException.class);
    }

    @Test
    void restock_increasesStockByGivenQuantity() {
        when(inventoryItemRepository.findByComponentId(1L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem result = inventoryService.restock(1L, 10);

        assertThat(result.getQuantityInStock()).isEqualTo(60);
        verify(inventoryItemRepository).save(inventoryItem);
    }

    @Test
    void restock_throwsWhenItemNotFound() {
        when(inventoryItemRepository.findByComponentId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.restock(99L, 10))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inventoryItemRepository, never()).save(any());
    }
}
