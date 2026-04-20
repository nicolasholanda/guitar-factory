package com.guitarfactory.repository;

import com.guitarfactory.domain.entity.GuitarModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuitarModelRepository extends JpaRepository<GuitarModel, Long> {

    Optional<GuitarModel> findByName(String name);
}
