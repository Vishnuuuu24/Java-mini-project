package com.example.stmgt.domain.enums;

public enum AcademicLevel {
    UG("UG"),
    PG("PG");

    private final String value;

    AcademicLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AcademicLevel fromValue(String value) {
        for (AcademicLevel level : values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown AcademicLevel value: " + value);
    }
}
