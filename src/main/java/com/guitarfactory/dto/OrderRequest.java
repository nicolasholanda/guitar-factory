package com.guitarfactory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotBlank String customerName,
        @NotBlank @Email String customerEmail,
        @NotNull Long modelId,
        @NotNull @Valid GuitarSpecRequest spec
) {}
