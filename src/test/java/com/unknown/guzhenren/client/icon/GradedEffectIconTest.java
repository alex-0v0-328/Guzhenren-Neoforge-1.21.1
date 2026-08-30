package com.unknown.guzhenren.client.icon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.Test;

class GradedEffectIconTest {

    private static String texture(GradedEffectIcon icon, int amplifier) {
        return icon.textureFor(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, amplifier));
    }

    @Test
    void defaultOffsetMapsAmplifierPlusOne() {
        GradedEffectIcon icon = GradedEffectIcon.mobEffect("essence_qi", 1, 5);
        assertEquals("mob_effect/essence_qi_1", texture(icon, 0));
        assertEquals("mob_effect/essence_qi_3", texture(icon, 2));
        assertEquals("mob_effect/essence_qi_5", texture(icon, 4));
        assertEquals("mob_effect/essence_qi_5", texture(icon, 9));
    }

    @Test
    void maliciousThoughtCountsAmplifierFromRankTwo() {
        GradedEffectIcon icon = GradedEffectIcon.item("malicious_thought_gu", 2, 5, 2);
        assertEquals("item/malicious_thought_gu_2", texture(icon, 0));
        assertEquals("item/malicious_thought_gu_3", texture(icon, 1));
        assertEquals("item/malicious_thought_gu_4", texture(icon, 2));
        assertEquals("item/malicious_thought_gu_5", texture(icon, 3));
    }

    @Test
    void rangeClampsBothEnds() {
        GradedEffectIcon icon = GradedEffectIcon.item("self_reliance_gu", 2, 4);
        assertEquals("item/self_reliance_gu_2", texture(icon, 0));
        assertEquals("item/self_reliance_gu_2", texture(icon, 1));
        assertEquals("item/self_reliance_gu_3", texture(icon, 2));
        assertEquals("item/self_reliance_gu_4", texture(icon, 3));
        assertEquals("item/self_reliance_gu_4", texture(icon, 10));
    }
}
