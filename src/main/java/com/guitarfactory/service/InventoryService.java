package com.guitarfactory.service;

import com.guitarfactory.domain.entity.InventoryItem;
import com.guitarfactory.exception.InsufficientInventoryException;
import com.guitarfactory.exception.ResourceNotFoundException;
import com.guitarfactory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional(readOnly = true)
    public List<InventoryItem> findAll() {
        return inventoryItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InventoryItem findByComponentId(Long componentId) {
        return inventoryItemRepository.findByComponentId(componentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for component id: " + componentId));
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(Long componentId, int requiredQuantity) {
        return inventoryItemRepository.findByComponentId(componentId)
                .map(item -> item.getQuantityInStock() >= requiredQuantity)
                .orElse(false);
    }

    public void consume(Long componentId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByComponentId(componentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for component id: " + componentId));

        if (item.getQuantityInStock() < quantity) {
            throw new InsufficientInventoryException(
                    "Insufficient stock for '" + item.getComponent().getName() + "'. " +
                    "Required: " + quantity + ", available: " + item.getQuantityInStock());
        }

        item.setQuantityInStock(item.getQuantityInStock() - quantity);
        inventoryItemRepository.save(item);
    }

    public InventoryItem restock(Long componentId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByComponentId(componentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for component id: " + componentId));

        item.setQuantityInStock(item.getQuantityInStock() + quantity);
        return inventoryItemRepository.save(item);
    }
}
