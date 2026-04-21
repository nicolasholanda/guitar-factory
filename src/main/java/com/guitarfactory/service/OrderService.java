package com.guitarfactory.service;

import com.guitarfactory.domain.entity.Guitar;
import com.guitarfactory.domain.entity.GuitarModel;
import com.guitarfactory.domain.entity.GuitarSpec;
import com.guitarfactory.domain.entity.Order;
import com.guitarfactory.domain.enums.GuitarStatus;
import com.guitarfactory.domain.enums.OrderStatus;
import com.guitarfactory.exception.InvalidOrderStateException;
import com.guitarfactory.exception.ResourceNotFoundException;
import com.guitarfactory.repository.GuitarModelRepository;
import com.guitarfactory.repository.GuitarRepository;
import com.guitarfactory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final GuitarRepository guitarRepository;
    private final GuitarModelRepository guitarModelRepository;
    private final GuitarFactoryService guitarFactoryService;

    public Order createOrder(String customerName, String customerEmail, Long modelId, GuitarSpec spec) {
        GuitarModel model = guitarModelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Guitar model not found: " + modelId));

        Order order = Order.builder()
                .customerName(customerName)
                .customerEmail(customerEmail)
                .status(OrderStatus.IN_PRODUCTION)
                .createdAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        guitarFactoryService.buildGuitar(order, model, spec);

        return orderRepository.findById(order.getId()).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order completeOrder(Long id) {
        Order order = findById(id);
        requireStatus(order, OrderStatus.IN_PRODUCTION, "complete");

        Guitar guitar = guitarRepository.findByOrderId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guitar not found for order: " + id));

        guitar.setStatus(GuitarStatus.COMPLETED);
        guitar.setCompletedAt(LocalDateTime.now());
        guitarRepository.save(guitar);

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order deliverOrder(Long id) {
        Order order = findById(id);
        requireStatus(order, OrderStatus.COMPLETED, "deliver");

        Guitar guitar = guitarRepository.findByOrderId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guitar not found for order: " + id));

        guitar.setStatus(GuitarStatus.DELIVERED);
        guitarRepository.save(guitar);

        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long id) {
        Order order = findById(id);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private void requireStatus(Order order, OrderStatus required, String action) {
        if (order.getStatus() != required) {
            throw new InvalidOrderStateException(
                    "Cannot " + action + " order with status " + order.getStatus() +
                    ". Required: " + required);
        }
    }
}
