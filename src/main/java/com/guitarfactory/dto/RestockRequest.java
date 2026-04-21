package com.guitarfactory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RestockRequest(
        @NotNull Long componentId,
        @Min(1) int quantity
) {}
