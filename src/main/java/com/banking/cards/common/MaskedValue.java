package com.banking.cards.common;

import com.banking.cards.config.MaskingConfig;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;

@Data
@Slf4j
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public abstract class MaskedValue<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final MaskingConfig maskingConfig;
    private final T value;

    protected abstract String getMaskedValue();

    @Override
    @JsonValue
    public final String toString() {
        return maskingConfig.isEnable() ? getMaskedValue() : value.toString();
    }
}
