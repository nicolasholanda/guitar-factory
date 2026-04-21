package com.guitarfactory.controller;

import com.guitarfactory.dto.GuitarModelDto;
import com.guitarfactory.mapper.GuitarMapper;
import com.guitarfactory.repository.GuitarModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guitar-models")
@RequiredArgsConstructor
public class GuitarModelController {

    private final GuitarModelRepository guitarModelRepository;
    private final GuitarMapper guitarMapper;

    @GetMapping
    public List<GuitarModelDto> findAll() {
        return guitarModelRepository.findAll().stream()
                .map(guitarMapper::toModelDto)
                .toList();
    }

    @GetMapping("/{id}")
    public GuitarModelDto findById(@PathVariable Long id) {
        return guitarModelRepository.findById(id)
                .map(guitarMapper::toModelDto)
                .orElseThrow(() -> new com.guitarfactory.exception.ResourceNotFoundException(
                        "Guitar model not found: " + id));
    }
}
