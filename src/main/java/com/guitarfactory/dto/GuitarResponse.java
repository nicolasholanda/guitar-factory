package com.guitarfactory.dto;

import com.guitarfactory.domain.enums.GuitarStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GuitarResponse(
        Long id,
        String serialNumber,
        String modelName,
        GuitarStatus status,
        BigDecimal estimatedPrice,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        GuitarSpecDto spec
) {}
