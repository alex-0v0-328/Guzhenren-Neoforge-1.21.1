package com.unknown.guzhenren.recipe;

import com.unknown.guzhenren.custom.enums.aperture.Rank;

/**
 * The refinement method [炼法], derived from the ranks involved and never stored.
 *
 * <p>⚠ This is one of two independent axes; the other is how many Gu went in. They overlap without
 * either containing the other, so they must never be collapsed into one enum.
 *
 * @author Alex
 * @since 1.0.0
 */
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
