package com.guitarfactory.service;

import com.guitarfactory.domain.entity.Component;
import com.guitarfactory.domain.entity.Guitar;
import com.guitarfactory.domain.entity.GuitarModel;
import com.guitarfactory.domain.entity.GuitarSpec;
import com.guitarfactory.domain.entity.Order;
import com.guitarfactory.domain.enums.ComponentType;
import com.guitarfactory.domain.enums.GuitarStatus;
import com.guitarfactory.domain.enums.PickupType;
import com.guitarfactory.domain.enums.StringCount;
import com.guitarfactory.exception.InsufficientInventoryException;
import com.guitarfactory.exception.ResourceNotFoundException;
import com.guitarfactory.repository.ComponentRepository;
import com.guitarfactory.repository.GuitarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GuitarFactoryService {

    private final GuitarRepository guitarRepository;
    private final ComponentRepository componentRepository;
    private final InventoryService inventoryService;

    public Guitar buildGuitar(Order order, GuitarModel model, GuitarSpec spec) {
        List<Component> components = resolveComponents(spec);

        for (Component component : components) {
            if (!inventoryService.isAvailable(component.getId(), 1)) {
                throw new InsufficientInventoryException(
                        "Out of stock: " + component.getName());
            }
        }

        BigDecimal componentsTotal = BigDecimal.ZERO;
        for (Component component : components) {
            inventoryService.consume(component.getId(), 1);
            componentsTotal = componentsTotal.add(component.getUnitPrice());
        }

        Guitar guitar = Guitar.builder()
                .serialNumber(generateSerialNumber())
                .model(model)
                .spec(spec)
                .order(order)
                .status(GuitarStatus.ORDERED)
                .estimatedPrice(model.getBasePrice().add(componentsTotal))
                .createdAt(LocalDateTime.now())
                .build();

        return guitarRepository.save(guitar);
    }

    private List<Component> resolveComponents(GuitarSpec spec) {
        return List.of(
                resolveByTypeAndWood(ComponentType.BODY_BLANK, spec.getBodyWood().name()),
                resolveByTypeAndWood(ComponentType.NECK, spec.getNeckWood().name()),
                resolveByTypeAndWood(ComponentType.FRETBOARD, spec.getFretboardWood().name()),
                resolvePickups(spec.getPickupType()),
                resolveTuners(spec.getStringCount()),
                resolveFirstByType(ComponentType.BRIDGE),
                resolveFirstByType(ComponentType.NUT),
                resolveStrings(spec.getStringCount()),
                resolveFirstByType(ComponentType.ELECTRONICS)
        );
    }

    private Component resolveByTypeAndWood(ComponentType type, String woodName) {
        return componentRepository.findByComponentTypeAndWoodType(
                        type, com.guitarfactory.domain.enums.WoodType.valueOf(woodName))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Component not found: " + type + " with wood " + woodName));
    }

    private Component resolveFirstByType(ComponentType type) {
        return componentRepository.findFirstByComponentTypeAndWoodTypeIsNull(type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Component not found for type: " + type));
    }

    private Component resolvePickups(PickupType pickupType) {
        String fragment = switch (pickupType) {
            case SINGLE_COIL -> "Single Coil";
            case HUMBUCKER -> "Humbucker";
            case P90 -> "P90";
        };
        return componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(
                        ComponentType.PICKUPS, fragment)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pickup set not found for type: " + pickupType));
    }

    private Component resolveTuners(StringCount stringCount) {
        String fragment = stringCount.getValue() + "-String";
        return componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(
                        ComponentType.TUNERS, fragment)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tuners not found for string count: " + stringCount));
    }

    private Component resolveStrings(StringCount stringCount) {
        String fragment = stringCount.getValue() + "-String";
        return componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(
                        ComponentType.STRINGS, fragment)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Strings not found for string count: " + stringCount));
    }

    private String generateSerialNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "GF-" + year + "-" + uid;
    }
}
