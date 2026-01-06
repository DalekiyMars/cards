package com.banking.cards.common;

import com.banking.cards.config.MaskingConfig;

import java.math.BigDecimal;

public class MaskedBalanceValue extends MaskedValue<BigDecimal>{
    public MaskedBalanceValue(MaskingConfig maskingConfig, BigDecimal value) {
        super(maskingConfig, value);
    }

    @Override
    protected String getMaskedValue() {
        return "***";
    }
}
