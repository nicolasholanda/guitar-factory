package com.guitarfactory.service;

import com.guitarfactory.domain.entity.*;
import com.guitarfactory.domain.enums.*;
import com.guitarfactory.exception.InsufficientInventoryException;
import com.guitarfactory.exception.ResourceNotFoundException;
import com.guitarfactory.repository.ComponentRepository;
import com.guitarfactory.repository.GuitarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuitarFactoryServiceTest {

    @Mock
    private GuitarRepository guitarRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private GuitarFactoryService guitarFactoryService;

    private GuitarModel model;
    private GuitarSpec spec;
    private Order order;

    private Component bodyBlank;
    private Component neck;
    private Component fretboard;
    private Component pickups;
    private Component tuners;
    private Component bridge;
    private Component nut;
    private Component strings;
    private Component electronics;

    @BeforeEach
    void setUp() {
        model = GuitarModel.builder()
                .id(1L)
                .name("Classic S-Type")
                .basePrice(new BigDecimal("1200.00"))
                .build();

        spec = GuitarSpec.builder()
                .bodyType(BodyType.SOLID)
                .bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE)
                .fretboardWood(WoodType.ROSEWOOD)
                .stringCount(StringCount.SIX)
                .pickupType(PickupType.SINGLE_COIL)
                .finish(GuitarFinish.GLOSS)
                .scaleLength(new BigDecimal("25.50"))
                .color("Sunburst")
                .build();

        order = Order.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status(OrderStatus.IN_PRODUCTION)
                .createdAt(LocalDateTime.now())
                .build();

        bodyBlank   = component(1L, "Alder Body Blank",           ComponentType.BODY_BLANK,  "80.00");
        neck        = component(2L, "Maple Neck",                  ComponentType.NECK,         "150.00");
        fretboard   = component(3L, "Rosewood Fretboard",          ComponentType.FRETBOARD,    "50.00");
        pickups     = component(4L, "Single Coil Set",             ComponentType.PICKUPS,      "120.00");
        tuners      = component(5L, "Standard Tuners 6-String",    ComponentType.TUNERS,       "40.00");
        bridge      = component(6L, "Standard Bridge",             ComponentType.BRIDGE,       "60.00");
        nut         = component(7L, "Bone Nut",                    ComponentType.NUT,          "15.00");
        strings     = component(8L, "6-String String Set",         ComponentType.STRINGS,      "10.00");
        electronics = component(9L, "Standard Electronics Kit",    ComponentType.ELECTRONICS,  "30.00");
    }

    private Component component(Long id, String name, ComponentType type, String price) {
        return Component.builder()
                .id(id)
                .name(name)
                .componentType(type)
                .unitPrice(new BigDecimal(price))
                .build();
    }

    private void stubAllComponents() {
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.BODY_BLANK, WoodType.ALDER))
                .thenReturn(Optional.of(bodyBlank));
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.NECK, WoodType.MAPLE))
                .thenReturn(Optional.of(neck));
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.FRETBOARD, WoodType.ROSEWOOD))
                .thenReturn(Optional.of(fretboard));
        when(componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(ComponentType.PICKUPS, "Single Coil"))
                .thenReturn(Optional.of(pickups));
        when(componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(ComponentType.TUNERS, "6-String"))
                .thenReturn(Optional.of(tuners));
        when(componentRepository.findFirstByComponentTypeAndWoodTypeIsNull(ComponentType.BRIDGE))
                .thenReturn(Optional.of(bridge));
        when(componentRepository.findFirstByComponentTypeAndWoodTypeIsNull(ComponentType.NUT))
                .thenReturn(Optional.of(nut));
        when(componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(ComponentType.STRINGS, "6-String"))
                .thenReturn(Optional.of(strings));
        when(componentRepository.findFirstByComponentTypeAndWoodTypeIsNull(ComponentType.ELECTRONICS))
                .thenReturn(Optional.of(electronics));
    }

    @Test
    void buildGuitar_savesGuitarWithCorrectStatusAndAssociations() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(true);
        when(guitarRepository.save(any(Guitar.class))).thenAnswer(inv -> inv.getArgument(0));

        Guitar result = guitarFactoryService.buildGuitar(order, model, spec);

        assertThat(result.getStatus()).isEqualTo(GuitarStatus.ORDERED);
        assertThat(result.getModel()).isEqualTo(model);
        assertThat(result.getSpec()).isEqualTo(spec);
        assertThat(result.getOrder()).isEqualTo(order);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(guitarRepository).save(any(Guitar.class));
    }

    @Test
    void buildGuitar_generatesSerialNumberMatchingExpectedFormat() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(true);
        when(guitarRepository.save(any(Guitar.class))).thenAnswer(inv -> inv.getArgument(0));

        Guitar result = guitarFactoryService.buildGuitar(order, model, spec);

        assertThat(result.getSerialNumber()).matches("GF-\\d{4}-[A-Z0-9]{8}");
    }

    @Test
    void buildGuitar_calculatesEstimatedPriceAsModelBasePlusSumOfComponents() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(true);
        when(guitarRepository.save(any(Guitar.class))).thenAnswer(inv -> inv.getArgument(0));

        Guitar result = guitarFactoryService.buildGuitar(order, model, spec);

        // 1200 + 80 + 150 + 50 + 120 + 40 + 60 + 15 + 10 + 30 = 1755
        assertThat(result.getEstimatedPrice()).isEqualByComparingTo(new BigDecimal("1755.00"));
    }

    @Test
    void buildGuitar_consumesStockForAllNineComponents() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(true);
        when(guitarRepository.save(any(Guitar.class))).thenAnswer(inv -> inv.getArgument(0));

        guitarFactoryService.buildGuitar(order, model, spec);

        verify(inventoryService, times(9)).consume(anyLong(), eq(1));
    }

    @Test
    void buildGuitar_throwsWhenBodyBlankComponentNotFound() {
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.BODY_BLANK, WoodType.ALDER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> guitarFactoryService.buildGuitar(order, model, spec))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BODY_BLANK");
    }

    @Test
    void buildGuitar_throwsWhenPickupsNotFound() {
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.BODY_BLANK, WoodType.ALDER))
                .thenReturn(Optional.of(bodyBlank));
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.NECK, WoodType.MAPLE))
                .thenReturn(Optional.of(neck));
        when(componentRepository.findByComponentTypeAndWoodType(ComponentType.FRETBOARD, WoodType.ROSEWOOD))
                .thenReturn(Optional.of(fretboard));
        when(componentRepository.findFirstByComponentTypeAndNameContainingIgnoreCase(ComponentType.PICKUPS, "Single Coil"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> guitarFactoryService.buildGuitar(order, model, spec))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PICKUPS");
    }

    @Test
    void buildGuitar_throwsWhenAnyComponentOutOfStock() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(false);

        assertThatThrownBy(() -> guitarFactoryService.buildGuitar(order, model, spec))
                .isInstanceOf(InsufficientInventoryException.class)
                .hasMessageContaining("Out of stock");
    }

    @Test
    void buildGuitar_doesNotConsumeStockWhenInventoryCheckFails() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(false);

        assertThatThrownBy(() -> guitarFactoryService.buildGuitar(order, model, spec))
                .isInstanceOf(InsufficientInventoryException.class);

        verify(inventoryService, never()).consume(anyLong(), anyInt());
        verify(guitarRepository, never()).save(any());
    }

    @Test
    void buildGuitar_checksInventoryBeforeConsumingForAllComponents() {
        stubAllComponents();
        when(inventoryService.isAvailable(anyLong(), eq(1))).thenReturn(true);
        when(guitarRepository.save(any(Guitar.class))).thenAnswer(inv -> inv.getArgument(0));

        guitarFactoryService.buildGuitar(order, model, spec);

        verify(inventoryService, times(9)).isAvailable(anyLong(), eq(1));
    }
}
