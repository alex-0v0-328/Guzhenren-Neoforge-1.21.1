package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.item.material.GuMaterialItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QiMaterialItem extends GuMaterialItem {

    private static final String CHARGE_CAPTION = "guzhenren.hud.refining_plain";
    private static final String FAILED_ESSENCE = "guzhenren.item.failed.essence";

    private static final long[] ESSENCE_COST = {50L, 500L, 5_000L, 50_000L, 500_000L};

    private final QiKind kind;

    public QiMaterialItem(Properties properties, Rank rank, QiKind kind) {
        super(properties, rank, GuPath.QI);
        this.kind = kind;
    }

    public QiKind kind() {return kind;}

    protected long qiAmount() {return QiKind.tierAmount(tier());}
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
        QiService.add(player, kind, qiAmount());
        return 1;
    }
}
