package com.banking.cards.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SideServiceRequest(
        @NotBlank String service,
        @NotBlank String apiKey
) {}
