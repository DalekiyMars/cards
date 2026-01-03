package com.banking.cards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter
@Component
public class CardNumberConverter implements AttributeConverter<String, String> {
    // В реальности здесь должна быть логика AES шифрования
    @Override
    public String convertToDatabaseColumn(String attribute) {
        // TODO: Реализовать шифрование перед записью в БД
        return "ENC_" + attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // TODO: Реализовать расшифровку при чтении
        return dbData.replace("ENC_", "");
    }
}