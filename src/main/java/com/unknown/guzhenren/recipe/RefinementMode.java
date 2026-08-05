package com.unknown.guzhenren.recipe;

import com.unknown.guzhenren.custom.enums.aperture.Rank;

public enum RefinementMode {
    STANDARD,
    ASCENDANT,
    REVERSE;

    public static RefinementMode between(Rank input, Rank result) {
        int step = result.ordinal() - input.ordinal();
        if (step > 0) return ASCENDANT;
        return step < 0 ? REVERSE : STANDARD;
    }
}
