package com.banking.cards.constants;

public class TestConstants {
    public static final String BASE_CONTROLLER_PATH = "src/test/resources/controller";
    public static final String FIRST_CARD_PATTERN_PART = "^(\\d{4}[- ]?){3}\\d{4}$";
    public static final String SECOND_CARD_PATTERN_PART = "|^\\d{13,19}$";
    public static final String CARD_PATTERN = FIRST_CARD_PATTERN_PART + SECOND_CARD_PATTERN_PART;
}
