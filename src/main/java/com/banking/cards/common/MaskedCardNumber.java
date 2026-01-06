package com.banking.cards.common;

import com.banking.cards.config.MaskingConfig;
import org.apache.commons.lang3.StringUtils;

public class MaskedCardNumber extends MaskedValue<String> {

    public MaskedCardNumber(MaskingConfig maskingConfig, String value) {
        super(maskingConfig, value);
    }

    @Override
    protected String getMaskedValue() {
        if (StringUtils.isBlank(this.value())) {
            return "";
        }

        final var maskedValue = new StringBuilder(StringUtils.trimToEmpty(this.value()));

        for (int i = 0; i < maskedValue.length() - maskingConfig().getVisibleCardTailLength(); i++) {
            maskedValue.setCharAt(i, maskingConfig().getMaskSymbol());
        }

        return maskedValue.toString();
    }
}