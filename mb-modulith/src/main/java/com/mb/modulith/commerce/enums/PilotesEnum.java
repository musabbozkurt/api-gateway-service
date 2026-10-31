package com.mb.modulith.commerce.enums;

import lombok.Getter;

import java.util.Arrays;

public enum PilotesEnum {
    FIVE(5),
    TEN(10),
    FIFTEEN(15);

    @Getter
    private final int pilotes;

    PilotesEnum(int pilotes) {
        this.pilotes = pilotes;
    }

    public static PilotesEnum getStateById(Integer pilotes) {
        return Arrays.stream(PilotesEnum.values())
                .filter(enumStateValue -> enumStateValue.pilotes == pilotes)
                .findFirst()
                .orElse(null);
    }
}
