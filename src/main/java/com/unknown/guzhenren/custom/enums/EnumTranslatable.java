package com.unknown.guzhenren.custom.enums;

//  The getTranslationKey() every Gu* enum shares -- lets a display take the constant, not its key.
public interface EnumTranslatable {
    String getTranslationKey();
}
