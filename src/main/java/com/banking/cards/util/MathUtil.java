package com.banking.cards.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class MathUtil {
    public BigDecimal roundBalanceTo2SymbolsAfterPoint(BigDecimal balance){
        return balance.setScale(2, RoundingMode.HALF_UP);
    }
}
