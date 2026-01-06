package com.banking.cards.common;

import com.banking.cards.config.MaskingConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MaskedValueFactory {

    private final MaskingConfig maskingConfig;

    public MaskedCardNumber createCardNumber(String value) {
        return new MaskedCardNumber(maskingConfig, value);
    }
}