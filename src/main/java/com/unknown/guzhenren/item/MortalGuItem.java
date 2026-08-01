package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MortalGuItem extends GuItem {

    private static final String KIND_KEY = "guzhenren.display.gu";
    private static final String CHARGE_CAPTION = "guzhenren.hud.using_plain";

    private static final int ONE_SHOT_CHARGE_TICKS = 10;

    private final boolean reusable;
    private final boolean feedable;

    public MortalGuItem(Properties properties, Rank rank, GuPath path, boolean reusable, boolean feedable) {
        super(properties, rank, path);
        this.reusable = reusable;
        this.feedable = feedable;
    }

    @Override
    protected String kindKey() {return KIND_KEY;}
    public boolean reusable() {return reusable;}
    public boolean feedable() {return feedable;}

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        return reusable ? super.useDurationTicks(player, stack) : ONE_SHOT_CHARGE_TICKS;
    }

    @Override
    public Component chargeCaption(ItemStack stack) {return Component.translatable(CHARGE_CAPTION);}

    @Override
    public boolean onDroppedByPlayer(@NotNull ItemStack stack, @NotNull Player player) {return !isVital(stack);}
}
