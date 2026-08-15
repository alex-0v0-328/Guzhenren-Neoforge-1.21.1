package com.unknown.guzhenren.recipe;

import com.unknown.guzhenren.custom.enums.aperture.Rank;

/**
 * The refinement method [炼法], derived from the ranks involved and never stored.
 *
 * <p>One of two independent axes describing a {@link com.unknown.guzhenren.recipe.GuRecipe}; the other
 * is how many Gu went in ({@code composite}). They overlap without either containing the other: 合练
 * [Composite] need not raise the rank, and 升炼 [Ascendant] may use a single Gu. They must never be
 * collapsed into one enum.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.recipe.GuRecipe
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
