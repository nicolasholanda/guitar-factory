package com.guitarfactory.controller;

import com.guitarfactory.domain.enums.GuitarStatus;
import com.guitarfactory.dto.GuitarResponse;
import com.guitarfactory.exception.ResourceNotFoundException;
import com.guitarfactory.mapper.GuitarMapper;
import com.guitarfactory.repository.GuitarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guitars")
@RequiredArgsConstructor
public class GuitarController {

    private final GuitarRepository guitarRepository;
    private final GuitarMapper guitarMapper;

    @GetMapping
    public List<GuitarResponse> findAll(@RequestParam(required = false) GuitarStatus status) {
        var guitars = status != null
                ? guitarRepository.findByStatus(status)
                : guitarRepository.findAll();
        return guitars.stream().map(guitarMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public GuitarResponse findById(@PathVariable Long id) {
        return guitarRepository.findById(id)
                .map(guitarMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Guitar not found: " + id));
    }

    @GetMapping("/serial/{serialNumber}")
    public GuitarResponse findBySerialNumber(@PathVariable String serialNumber) {
        return guitarRepository.findBySerialNumber(serialNumber)
                .map(guitarMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Guitar not found with serial: " + serialNumber));
    }
}
