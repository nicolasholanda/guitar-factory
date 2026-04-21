package com.guitarfactory.controller;

import com.guitarfactory.domain.enums.OrderStatus;
import com.guitarfactory.dto.OrderRequest;
import com.guitarfactory.dto.OrderResponse;
import com.guitarfactory.mapper.GuitarMapper;
import com.guitarfactory.mapper.OrderMapper;
import com.guitarfactory.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final GuitarMapper guitarMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        var spec = guitarMapper.toSpec(request.spec());
        var order = orderService.createOrder(
                request.customerName(),
                request.customerEmail(),
                request.modelId(),
                spec
        );
        return orderMapper.toResponse(order);
    }

    @GetMapping
    public List<OrderResponse> findAll(@RequestParam(required = false) OrderStatus status) {
        var orders = status != null
                ? orderService.findByStatus(status)
                : orderService.findAll();
        return orders.stream().map(orderMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return orderMapper.toResponse(orderService.findById(id));
    }

    @PutMapping("/{id}/complete")
    public OrderResponse completeOrder(@PathVariable Long id) {
        return orderMapper.toResponse(orderService.completeOrder(id));
    }

    @PutMapping("/{id}/deliver")
    public OrderResponse deliverOrder(@PathVariable Long id) {
        return orderMapper.toResponse(orderService.deliverOrder(id));
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderMapper.toResponse(orderService.cancelOrder(id));
    }
}
