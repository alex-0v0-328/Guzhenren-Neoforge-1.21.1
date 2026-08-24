package com.unknown.guzhenren.custom.enums.strength;

import com.unknown.guzhenren.custom.enums.EnumTranslatable;

public enum BeastStrengthFamily implements EnumTranslatable {

    BOAR,
    BEAR;

    private static final String KEY_PREFIX = "guzhenren.enum.strength.beast_family.";

    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
