package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

//  Essence Qi [元气]: essence regen runs faster while it lasts.
//  ⚠ A pure MARKER -- EssenceService.regenStep reads the amplifier. Nothing happens in here, on purpose:
//  regen is one formula in one place, and a second writer is how a rate starts disagreeing with itself.
public class EssenceQiEffect extends MobEffect {

    //  Indexed by amplifier, which is the Gu material's rank offset: Rank I..V.
    private static final double[] REGEN_BONUS = {0.10, 0.15, 0.20, 0.30, 0.50};

    public EssenceQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    //  Clamped, so an amplifier from a command can never index out of the table.
    public static double bonus(int amplifier) {
        return REGEN_BONUS[Math.clamp(amplifier, 0, REGEN_BONUS.length - 1)];
    }
}
