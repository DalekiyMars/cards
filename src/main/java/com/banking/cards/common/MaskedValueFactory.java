package com.banking.cards.common;

import com.banking.cards.config.MaskingConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
public class MaskedValueFactory {

    private final MaskingConfig maskingConfig;

    public MaskedCardNumber createCardNumber(String value) {
        return new MaskedCardNumber(maskingConfig, value);
    }

    public MaskedBalanceValue createCardNumber(BigDecimal value) {
        return new MaskedBalanceValue(maskingConfig, value);
    }
}