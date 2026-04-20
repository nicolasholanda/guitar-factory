package com.guitarfactory.domain.entity;

import com.guitarfactory.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "guitar_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuitarSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WoodType bodyWood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WoodType neckWood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WoodType fretboardWood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StringCount stringCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickupType pickupType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuitarFinish finish;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal scaleLength;

    @Column(nullable = false)
    private String color;
}
