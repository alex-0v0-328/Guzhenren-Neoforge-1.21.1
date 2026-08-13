package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unknown.guzhenren.effect.timed.MaliciousThoughtEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MaliciousThoughtEffectTest {

    private static final long[] EVIL_PER_SECOND = {2L, 20L, 200L, 2_000L};

    @Test
    @DisplayName("the duration is 240 ticks -- twelve real seconds at 20 ticks each")
    void duration() {
        assertEquals(240, MaliciousThoughtEffect.DURATION_TICKS);
    }

    @Test
    @DisplayName("twelve seconds fires exactly twelve times -- 240 divides 20, no more no less")
    void twelveTicks() {
        MaliciousThoughtEffect effect = new MaliciousThoughtEffect(
                MobEffectCategory.BENEFICIAL, 0, EVIL_PER_SECOND);
        int count = 0;
        for (int duration = MaliciousThoughtEffect.DURATION_TICKS; duration > 0; duration--) {
            if (effect.shouldApplyEffectTickThisTick(duration, 0)) count++;
        }
        assertEquals(12, count);
    }
}
