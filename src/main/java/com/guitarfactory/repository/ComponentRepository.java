package com.guitarfactory.repository;

import com.guitarfactory.domain.entity.Component;
import com.guitarfactory.domain.enums.ComponentType;
import com.guitarfactory.domain.enums.WoodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Long> {

    List<Component> findByComponentType(ComponentType componentType);

    Optional<Component> findByComponentTypeAndWoodType(ComponentType componentType, WoodType woodType);

    Optional<Component> findFirstByComponentTypeAndWoodTypeIsNull(ComponentType componentType);

    Optional<Component> findFirstByComponentTypeAndNameContainingIgnoreCase(ComponentType componentType, String name);
}
