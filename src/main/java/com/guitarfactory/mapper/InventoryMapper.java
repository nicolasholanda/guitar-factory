package com.guitarfactory.mapper;

import com.guitarfactory.domain.entity.InventoryItem;
import com.guitarfactory.dto.InventoryItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "component.id", target = "componentId")
    @Mapping(source = "component.name", target = "componentName")
    @Mapping(source = "component.componentType", target = "componentType")
    @Mapping(source = "component.woodType", target = "woodType")
    @Mapping(source = "component.unitPrice", target = "unitPrice")
    InventoryItemDto toDto(InventoryItem item);

    List<InventoryItemDto> toDtoList(List<InventoryItem> items);
}
