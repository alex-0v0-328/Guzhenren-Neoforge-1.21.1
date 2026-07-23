package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

//  What a Liquor Worm [酒虫] leaves behind: one in-game day where the ordinary essence [真元] pool empties,
//  regen redirects into the distilled pool [精炼真元], each point spends as two. Amplifier 0..3 = rank I..IV.
//  ⚠ A MARKER, deliberately -- it carries no applyEffectTick. All three phases live in EssenceService,
//  because they are writes to an attachment and a service is the only thing allowed to make those.
//  ⚠⚠ 1.21.1's MobEffect has NO expiry hook, so the 1:2 payback cannot hang here -- PlayerTickEvents
//  watches "pool non-empty, effect gone" instead, which also catches milk, /effect clear and death.
public class LiquorWormEffect extends MobEffect {

    public LiquorWormEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
