package com.unknown.guzhenren.util;

//  Every Gu* enum already had an identical getTranslationKey(); this names that fact.
//
//  What it buys: anything that displays a domain value can take the constant itself rather than its
//  key. The language providers are the loudest case -- `add(GuRank.ONE, "一转")` instead of
//  `add(GuRank.ONE.getTranslationKey(), "一转")` -- which is what lets those tables stay
//  column-aligned and still read as tables.
public interface GuTranslatable {
    String getTranslationKey();
}
