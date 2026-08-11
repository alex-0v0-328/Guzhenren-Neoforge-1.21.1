package com.unknown.guzhenren.custom.enums;

/**
 * The one thing every enum in this mod implements: it can name its own translation key.
 *
 * <p>⚠ Both language providers take this interface rather than a String, so the key an enum ships and
 * the key it is registered under can never drift into two different literals.
 *
 * @author Alex
 * @since 1.0.0
 */
public interface EnumTranslatable {
    String getTranslationKey();
}
