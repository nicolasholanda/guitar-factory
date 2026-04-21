package com.guitarfactory.controller;

import com.guitarfactory.dto.InventoryItemDto;
import com.guitarfactory.dto.RestockRequest;
import com.guitarfactory.mapper.InventoryMapper;
import com.guitarfactory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @GetMapping
    public List<InventoryItemDto> findAll() {
        return inventoryMapper.toDtoList(inventoryService.findAll());
    }

    @GetMapping("/component/{componentId}")
    public InventoryItemDto findByComponentId(@PathVariable Long componentId) {
        return inventoryMapper.toDto(inventoryService.findByComponentId(componentId));
    }

    @PostMapping("/restock")
    public InventoryItemDto restock(@Valid @RequestBody RestockRequest request) {
        return inventoryMapper.toDto(
                inventoryService.restock(request.componentId(), request.quantity()));
    }
}
