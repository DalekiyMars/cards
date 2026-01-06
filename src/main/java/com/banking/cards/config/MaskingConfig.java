package com.banking.cards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class MaskingConfig {

    @Value("${application.masking.enabled:true}")
    private boolean enable;

    @Value("${application.masking.maskSymbol:*}")
    private char maskSymbol;

    @Value("${application.masking.visibleCardTailLength:4}")
    private int visibleCardTailLength;
}
