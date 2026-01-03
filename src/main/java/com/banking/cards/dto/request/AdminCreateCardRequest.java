package com.banking.cards.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AdminCreateCardRequest(

        @NotNull
        UUID userId,

        @NotNull
        @Future
        LocalDate validityPeriod,

        @NotNull
        BigDecimal initialBalance

) {}