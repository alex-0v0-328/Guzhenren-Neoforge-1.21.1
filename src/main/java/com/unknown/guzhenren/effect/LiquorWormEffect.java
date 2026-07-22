package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

//  What a Liquor Worm [酒虫] leaves behind: one in-game day during which the ordinary essence [真元] pool
//  stays empty, regen is redirected into the distilled pool [精炼真元], and every distilled point spends
//  as two. Amplifier 0..3 is the worm's rank I..IV.
//  ⚠ A MARKER, deliberately -- it carries no applyEffectTick. All three phases live in EssenceService,
//  because they are writes to an attachment and a service is the only thing allowed to make those.
//  ⚠⚠ 1.21.1's MobEffect has NO expiry hook, so the 1:2 payback cannot hang here. PlayerTickEvents
//  watches for "pool is non-empty but the effect is gone" instead -- which also catches milk, /effect
//  clear and death, none of which would ever have called an expiry hook.
public class LiquorWormEffect extends MobEffect {

    public LiquorWormEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
