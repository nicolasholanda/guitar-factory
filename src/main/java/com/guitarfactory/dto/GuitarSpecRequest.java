package com.guitarfactory.dto;

import com.guitarfactory.domain.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record GuitarSpecRequest(
        @NotNull BodyType bodyType,
        @NotNull WoodType bodyWood,
        @NotNull WoodType neckWood,
        @NotNull WoodType fretboardWood,
        @NotNull StringCount stringCount,
        @NotNull PickupType pickupType,
        @NotNull GuitarFinish finish,
        @NotNull @DecimalMin("23.00") @DecimalMax("30.00") BigDecimal scaleLength,
        @NotBlank String color
) {}
