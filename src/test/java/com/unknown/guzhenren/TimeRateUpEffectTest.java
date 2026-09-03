package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unknown.guzhenren.effect.timed.TimeRateUpEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeRateUpEffectTest {

    @Test
    @DisplayName("Second Watch Gu grows to two layers and fourfold time")
    void secondWatchCapsAtFourfoldTime() {
        TimeRateUpEffect effect = effect(2, 2);

        assertEquals(0, effect.nextAmplifier(-1));
        assertEquals(1, effect.nextAmplifier(0));
        assertEquals(1, effect.nextAmplifier(1));
        assertEquals(2, effect.timeRate(0));
        assertEquals(4, effect.timeRate(1));
        assertEquals(4, effect.timeRate(99));
    }
    @Test
    @DisplayName("Third Watch Gu grows to three layers and ninefold time")
    void thirdWatchCapsAtNinefoldTime() {
        TimeRateUpEffect effect = effect(3, 3);

        assertEquals(0, effect.nextAmplifier(-1));
        assertEquals(1, effect.nextAmplifier(0));
        assertEquals(2, effect.nextAmplifier(1));
        assertEquals(2, effect.nextAmplifier(2));
        assertEquals(3, effect.timeRate(0));
        assertEquals(6, effect.timeRate(1));
        assertEquals(9, effect.timeRate(2));
        assertEquals(9, effect.timeRate(99));
    }
    @Test
    @DisplayName("the two capped Watch Gu effects add to thirteenfold time")
    void cappedWatchGuEffectsAddToThirteenfoldTime() {
        assertEquals(13, effect(2, 2).timeRate(1) + effect(3, 3).timeRate(2));
    }
    private static TimeRateUpEffect effect(int ratePerLayer, int maxLayers) {
        return new TimeRateUpEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF, ratePerLayer, maxLayers);
    }
}
