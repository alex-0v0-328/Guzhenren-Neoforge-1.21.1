package com.unknown.guzhenren.registry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ModEffectsColorTest {

    @Test
    void guEffectsUseWhiteParticles() {
        assertAll(
                () -> assertEquals(0xFFFFFF, ModEffects.VITALITY_LEAF.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.LIQUOR_WORM.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.FLOWER_BOAR_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.ALL_OUT_EFFORT.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.DRAGONPILL_CRICKET_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.BRUTE_FORCE_LONGHORN_BEETLE_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.HORIZONTAL_CRASH_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.VERTICAL_CRASH_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.CHARGING_CRASH_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.SELF_RELIANCE_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.HARDSHIP_STRENGTH_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.SECOND_WATCH_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.THIRD_WATCH_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.MALICIOUS_THOUGHT_GU.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.CASUAL_GU.get().getColor()));
    }

    @Test
    void poolAndHalfZombieUseWhiteParticles() {
        assertAll(
                () -> assertEquals(0xFFFFFF, ModEffects.LIFE_QI.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.ESSENCE_QI.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.DEATH_QI.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.STRENGTH_QI.get().getColor()),
                () -> assertEquals(0xFFFFFF, ModEffects.HALF_ZOMBIE.get().getColor()));
    }
}
