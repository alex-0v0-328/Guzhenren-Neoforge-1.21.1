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

public class QiMaterialItem extends GuMaterialItem {

    private static final String CHARGE_CAPTION = "guzhenren.hud.refining_plain";

    private static final int CHARGE_SAME_RANK = 40;
    private static final int CHARGE_ABOVE = 2;
    private static final int CHARGE_BELOW = 100;

    private static final long[] SPECKS = {1L, 4L, 16L, 64L, 256L};

    private final MarkTag tag;

    public QiMaterialItem(Properties properties, Rank rank, MarkTag tag) {
        super(properties, rank, GuPath.QI);
        this.tag = tag;
    }

    public MarkTag tag() {return tag;}

    protected long specks() {return SPECKS[tier()];}

    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        int gap = ApertureService.rank(player).ordinal() - rank().ordinal();
        if (gap > 0) return CHARGE_ABOVE;
        return gap == 0 ? CHARGE_SAME_RANK : CHARGE_BELOW;
    }

    @Override
    public @Nullable Component chargeCaption(ItemStack stack, int remainingTicks) {
        return Component.translatable(CHARGE_CAPTION);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        PathService.addSpeck(player, GuPath.QI, tag, specks());
        return 1;
    }

    protected static void applyGraded(ServerPlayer player, Holder<MobEffect> effect, int grade, int ticks) {
        MobEffectInstance current = player.getEffect(effect);
        int amplifier = current == null ? grade : Math.max(grade, current.getAmplifier());
        int duration = current == null ? ticks : Math.max(ticks, current.getDuration());
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }
}
