package com.banking.cards.common;

import com.banking.cards.config.MaskingConfig;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class MaskedBalanceValue extends MaskedValue<BigDecimal>{
    public MaskedBalanceValue(MaskingConfig maskingConfig, BigDecimal value) {
        super(maskingConfig, value);
    }

    @Override
    protected String getMaskedValue() {
        final var maskedValue = new StringBuilder(StringUtils.trimToEmpty(this.value().toPlainString()));

        for (int i = 0; i < maskedValue.length(); i++) {
            maskedValue.setCharAt(i, maskingConfig().getMaskSymbol());
        }

        return maskedValue.toString();
    }
}
