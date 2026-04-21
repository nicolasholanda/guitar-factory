package com.guitarfactory.dto;

import com.guitarfactory.domain.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        GuitarResponse guitar
) {}
