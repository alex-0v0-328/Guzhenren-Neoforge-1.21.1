package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.item.GuMaterialItem;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  A Qi Path [气道] material: hold to refine, and the moment it finishes it is used and gone.
//  ⚠ NOT a RefinableGuItem -- there is no wild/refined state, no hunger and no binding, which is exactly
//  what lets these stack to 64. "Refinable" here means only that the click is charged.
//  ⚠ NO awakening gate -- a mortal may earn qi specks and may be cursed by Death Qi [死气]. Path marks
//  and specks live on the body, which everyone has, so the write always lands.  CLAUDE.md.
public class QiMaterialItem extends GuMaterialItem {

    private static final String CHARGE_CAPTION = "guzhenren.hud.refining_plain";

    //  Per item, and the whole pacing: two seconds at its own rank, a blink above it, five below.
    private static final int CHARGE_SAME_RANK = 40;
    private static final int CHARGE_ABOVE = 2;
    private static final int CHARGE_BELOW = 100;

    //  One item's worth of specks, by rank. ⚠ All five kinds share this ladder; only the tag differs.
    private static final long[] SPECKS = {1L, 4L, 16L, 64L, 256L};

    private final MarkTag tag;

    public QiMaterialItem(Properties properties, Rank rank, MarkTag tag) {
        super(properties, rank, GuPath.QI);
        this.tag = tag;
    }

    public MarkTag tag() {return tag;}

    //  0-indexed rank offset, the exponent every ladder here is built on (Rank I = 0).
    protected int tier() {return rank().ordinal() - Rank.ONE.ordinal();}
    protected long specks() {return SPECKS[tier()];}

    @Override
    protected boolean hasUse() {return true;}

    //  ⚠ Faster means a bigger 转数, not a bigger 阶段 -- Rank.ordinal(), against this material's own.
    //  ⚠ A mortal reads Rank.NONE (ordinal 0), which is below Rank I, so he simply gets the slow charge.
    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        int gap = ApertureService.rank(player).ordinal() - rank().ordinal();
        if (gap > 0) return CHARGE_ABOVE;
        return gap == 0 ? CHARGE_SAME_RANK : CHARGE_BELOW;
    }

    //  No numbers: there is no progress to report, only a bar filling. RefinableGuItem overrides with its own.
    @Override
    public @Nullable Component chargeCaption(ItemStack stack) {
        return Component.translatable(CHARGE_CAPTION);
    }

    //  Every kind pays specks under its own tag; a leaf adds its effect on top and still returns 1.
    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        PathService.addSpeck(player, GuPath.QI, tag, specks());
        return 1;
    }

    //  ⚠ Never stacks. A second use takes the HIGHER grade and refreshes the clock, and the duration
    //  never shortens -- so eating a Rank I on top of a Rank V can only ever help.
    protected static void applyGraded(ServerPlayer player, Holder<MobEffect> effect, int grade, int ticks) {
        MobEffectInstance current = player.getEffect(effect);
        int amplifier = current == null ? grade : Math.max(grade, current.getAmplifier());
        int duration = current == null ? ticks : Math.max(ticks, current.getDuration());
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }
}
