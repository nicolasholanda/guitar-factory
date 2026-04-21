package com.guitarfactory.mapper;

import com.guitarfactory.domain.entity.Order;
import com.guitarfactory.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GuitarMapper.class})
public interface OrderMapper {

    @Mapping(source = "guitar", target = "guitar")
    OrderResponse toResponse(Order order);
}
