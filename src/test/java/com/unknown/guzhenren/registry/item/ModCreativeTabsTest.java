package com.unknown.guzhenren.registry.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModCreativeTabsTest {

    @Test
    @DisplayName("the mortal Gu tab excludes strength Gu but keeps every other mortal Gu")
    void mortalGuTabExcludesStrength() {
        assertFalse(ModCreativeTabs.belongsInMortalGu(ModItems.WHITE_BOAR_GU.get()));
        assertTrue(ModCreativeTabs.belongsInMortalGu(ModItems.HOPE_GU.get()));
        assertFalse(ModCreativeTabs.belongsInMortalGu(ModItems.PRIMEVAL_STONE.get()));
    }
}
