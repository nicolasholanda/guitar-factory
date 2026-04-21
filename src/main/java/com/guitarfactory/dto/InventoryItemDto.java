package com.guitarfactory.dto;

import com.guitarfactory.domain.enums.ComponentType;
import com.guitarfactory.domain.enums.WoodType;

import java.math.BigDecimal;

public record InventoryItemDto(
        Long id,
        Long componentId,
        String componentName,
        ComponentType componentType,
        WoodType woodType,
        BigDecimal unitPrice,
        Integer quantityInStock
) {}
