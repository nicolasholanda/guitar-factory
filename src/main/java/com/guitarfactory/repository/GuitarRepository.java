package com.guitarfactory.repository;

import com.guitarfactory.domain.entity.Guitar;
import com.guitarfactory.domain.enums.GuitarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuitarRepository extends JpaRepository<Guitar, Long> {

    Optional<Guitar> findBySerialNumber(String serialNumber);

    List<Guitar> findByStatus(GuitarStatus status);

    Optional<Guitar> findByOrderId(Long orderId);
}
