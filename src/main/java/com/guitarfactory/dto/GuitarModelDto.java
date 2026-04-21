package com.guitarfactory.dto;

import java.math.BigDecimal;

public record GuitarModelDto(
        Long id,
        String name,
        String description,
        BigDecimal basePrice
) {}
