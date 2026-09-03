package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.registry.item.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StrengthPathBranchTooltipTest {

    @Test
    @DisplayName("all Beast Strength Phantom Gu show their branch below the Gu identity")
    void beastStrengthPhantomBranchIsShown() {
        for (Item item : List.of(ModItems.WHITE_BOAR_GU.get(), ModItems.BLACK_BOAR_GU.get(),
                ModItems.FLOWER_BOAR_GU.get(), ModItems.BEAR_STRENGTH_GU.get(),
                ModItems.DRAGONPILL_CRICKET_GU.get(), ModItems.BRUTE_FORCE_LONGHORN_BEETLE_GU.get())) {
            assertBranch(item, "guzhenren.enum.strength.strength_path_branch.beast_strength_phantom");
        }
    }
    @Test
    @DisplayName("all Human Jun Strength Gu show their branch below the Gu identity")
    void humanJunStrengthBranchIsShown() {
        for (Item item : List.of(ModItems.JIN_STRENGTH_GU.get(), ModItems.TENS_JIN_STRENGTH_GU.get(),
                ModItems.JUN_STRENGTH_GU.get(), ModItems.TENS_JUN_STRENGTH_GU.get())) {
            assertBranch(item, "guzhenren.enum.strength.strength_path_branch.human_jun_strength");
        }
    }
    @Test
    @DisplayName("Normal Strength Path Gu do not repeat their branch")
    void normalStrengthPathBranchIsHidden() {
        assertFalse(hasStrengthPathBranch(ModItems.ALL_OUT_EFFORT_GU_3.get()));
    }
    @Test
    @DisplayName("non-Strength Path Gu never show a Strength Path branch")
    void nonStrengthPathGuNeverShowStrengthPathBranch() {
        assertFalse(hasStrengthPathBranch(ModItems.SECOND_WATCH_GU.get()));
    }
    private static boolean hasStrengthPathBranch(Item item) {
        return keys(item).stream().anyMatch(key -> key.startsWith(
                "guzhenren.enum.strength.strength_path_branch."));
    }
    private static void assertBranch(Item item, String key) {
        List<String> tooltipKeys = keys(item);
        assertTrue(tooltipKeys.size() > 1, item.toString());
        assertEquals(key, tooltipKeys.get(1), item.toString());
    }
    private static List<String> keys(Item item) {
        List<Component> tooltip = new ArrayList<>();
        item.appendHoverText(new ItemStack(item), null, tooltip, null);
        return tooltip.stream()
                .map(Component::getContents)
                .filter(TranslatableContents.class::isInstance)
                .map(TranslatableContents.class::cast)
                .map(TranslatableContents::getKey)
                .toList();
    }
}
