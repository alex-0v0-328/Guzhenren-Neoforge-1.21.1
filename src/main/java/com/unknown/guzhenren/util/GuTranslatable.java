package com.unknown.guzhenren.util;

//  Every Gu* enum already had an identical getTranslationKey(); this names that fact.
//
//  What it buys: a display can take the constant itself, not its key. Loudest in the lang providers --
//  `add(GuRank.ONE, "一转")` is what keeps those tables short enough to stay column-aligned.
public interface GuTranslatable {
    String getTranslationKey();
}
