package com.guitarfactory.dto;

import com.guitarfactory.domain.enums.*;

import java.math.BigDecimal;

public record GuitarSpecDto(
        BodyType bodyType,
        WoodType bodyWood,
        WoodType neckWood,
        WoodType fretboardWood,
        StringCount stringCount,
        PickupType pickupType,
        GuitarFinish finish,
        BigDecimal scaleLength,
        String color
) {}
