package com.guitarfactory.domain.entity;

import com.guitarfactory.domain.enums.GuitarStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "guitars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guitar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_id", nullable = false)
    private GuitarModel model;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "spec_id", nullable = false)
    private GuitarSpec spec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuitarStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
