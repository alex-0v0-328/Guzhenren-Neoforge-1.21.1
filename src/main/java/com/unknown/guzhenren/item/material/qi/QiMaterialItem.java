package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QiMaterialItem extends GuMaterialItem {

    private static final String CHARGE_CAPTION = "guzhenren.hud.refining_plain";
    private static final String FAILED_ESSENCE = "guzhenren.item.failed.essence";

    private static final long[] SPECKS = {1L, 4L, 16L, 64L, 256L};
    private static final long[] ESSENCE_COST = {50L, 500L, 5_000L, 50_000L, 500_000L};

    private final MarkTag tag;

    public QiMaterialItem(Properties properties, Rank rank, MarkTag tag) {
        super(properties, rank, GuPath.QI);
        this.tag = tag;
    }

    public MarkTag tag() {return tag;}

    protected long specks() {return SPECKS[tier()];}
    protected long essenceCost() {return ESSENCE_COST[tier()];}

    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {return useChargeByGap(player);}

    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        return essenceCost() > 0 && EssenceService.spendable(player) < essenceCost()
                ? new Refusal(FAILED_ESSENCE)
                : null;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack,
                          int remaining) {
        if (!(entity instanceof ServerPlayer player)) return;

        int duration = useDurationTicks(player, stack);
        if (duration <= 0 || essenceCost() <= 0) return;

        int tick = duration - remaining + 1;
        long step = paidBy(tick, duration) - paidBy(tick - 1, duration);
        if (step > 0 && !EssenceService.consume(player, step)) player.stopUsingItem();
    }

    private long paidBy(int ticks, int duration) {return essenceCost() * ticks / duration;}

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
