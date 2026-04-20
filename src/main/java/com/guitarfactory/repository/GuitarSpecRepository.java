package com.guitarfactory.repository;

import com.guitarfactory.domain.entity.GuitarSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuitarSpecRepository extends JpaRepository<GuitarSpec, Long> {
}
