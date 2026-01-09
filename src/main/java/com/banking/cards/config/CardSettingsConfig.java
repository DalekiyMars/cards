package com.banking.cards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class CardSettingsConfig {

    @Value("${application.card-settings.prefix:}")
    private String prefix;
}
