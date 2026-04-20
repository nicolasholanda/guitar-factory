package com.guitarfactory.domain.enums;

public enum StringCount {
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    TWELVE(12);

    private final int value;

    StringCount(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
