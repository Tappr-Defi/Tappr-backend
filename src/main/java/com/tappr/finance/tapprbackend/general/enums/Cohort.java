package com.tappr.finance.tapprbackend.general.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Cohort {
    COHORT_17("Cohort 17"),
    COHORT_18("Cohort 18"),
    COHORT_19("Cohort 19"),
    COHORT_20("Cohort 20"),
    COHORT_21("Cohort 21"),
    COHORT_22("Cohort 22"),
    COHORT_23("Cohort 23"),
    COHORT_24("Cohort 24"),
    COHORT_25("Cohort 25"),
    COHORT_26("Cohort 26"),
    COHORT_27("Cohort 27"),
    COHORT_28("Cohort 28"),
    COHORT_29("Cohort 29"),
    COHORT_30("Cohort 30");

    private final String displayName;

    Cohort(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Cohort fromDisplayName(String displayName) {
        for (Cohort cohort : values()) {
            if (cohort.displayName.equalsIgnoreCase(displayName)) {
                return cohort;
            }
        }
        throw new IllegalArgumentException("Invalid cohort: " + displayName);
    }
}
