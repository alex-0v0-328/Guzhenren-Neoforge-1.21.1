package com.unknown.guzhenren.registry.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.Test;

class ModEffectsInstanceTest {

    @Test
    void singleArgBuildsParticlesOffIconOn() {
        MobEffectInstance i = ModEffects.instance(ModEffects.LIFE_QI, 100);
        assertFalse(i.isAmbient());
        assertFalse(i.isVisible());
        assertTrue(i.showIcon());
        assertEquals(0, i.getAmplifier());
        assertEquals(100, i.getDuration());
    }
    @Test
    void amplifiedBuildKeepsParticlesOffIconOn() {
        MobEffectInstance i = ModEffects.instance(ModEffects.LIFE_QI, 40, 2);
        assertFalse(i.isVisible());
        assertTrue(i.showIcon());
        assertEquals(2, i.getAmplifier());
        assertEquals(40, i.getDuration());
    }
}
