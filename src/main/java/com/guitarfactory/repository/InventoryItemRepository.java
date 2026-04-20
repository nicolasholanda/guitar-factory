package com.guitarfactory.repository;

import com.guitarfactory.domain.entity.InventoryItem;
import com.guitarfactory.domain.enums.ComponentType;
import com.guitarfactory.domain.enums.WoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByComponentId(Long componentId);

    List<InventoryItem> findByComponent_ComponentType(ComponentType componentType);

    @Query("SELECT i FROM InventoryItem i WHERE i.component.componentType = :type AND i.component.woodType = :woodType")
    Optional<InventoryItem> findByComponentTypeAndWoodType(
            @Param("type") ComponentType type,
            @Param("woodType") WoodType woodType);

    @Query("SELECT i FROM InventoryItem i WHERE i.component.componentType = :type AND i.component.woodType IS NULL")
    Optional<InventoryItem> findByComponentTypeWithNoWood(@Param("type") ComponentType type);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantityInStock > 0")
    List<InventoryItem> findAllInStock();
}
