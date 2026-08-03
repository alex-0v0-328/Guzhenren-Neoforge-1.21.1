package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.item.GuSpec;
import com.unknown.guzhenren.item.MortalGuItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HopeGuItem extends MortalGuItem {

    private static final String FAILED_AWAKENED = "guzhenren.item.failed.awakened";
    private static final String CAPTION_TENTH = "guzhenren.display.tenth.";

    private static final int FILLING_TICKS = 3 * Ticks.SECOND;
    private static final int HELD_AT_FULL_TICKS = Ticks.SECOND;
    private static final int RITUAL_TICKS = FILLING_TICKS + HELD_AT_FULL_TICKS;

    private static final int LOWEST_BASE = Talent.LOWEST.getMinPercent();
    private static final int FILLING_COLOR = 0xFFCCCCCC;
    private static final int LOCKED_COLOR = CHARGE_COLOR_DEFAULT;

    public HopeGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected @Nullable Refusal useGate(Player player, ItemStack stack) {
        return ApertureService.isAwakened(player) ? new Refusal(FAILED_AWAKENED) : null;
    }

    @Override
    protected final @Nullable Refusal gate(Player player, ItemStack stack) {return useGate(player, stack);}

    @Override
    protected final int useDurationTicks(Player player, ItemStack stack) {return RITUAL_TICKS;}

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack,
                          int remaining) {
        if (remaining != RITUAL_TICKS || !(entity instanceof ServerPlayer)) return;

        stack.set(ModDataComponents.AWAKEN_BASE.get(), Talent.randomPercent(Talent.randomTalent()));
    }

    @Override
    protected final int apply(ServerPlayer player, ItemStack stack) {
        ApertureService.awaken(player, rolledBase(stack));
        ModCommandSupport.refreshCommands(player);
        return 1;
    }

    //region the ritual bar
    private static int rolledBase(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AWAKEN_BASE.get(), 0);
    }

    private static int filledTicks(int remaining) {return Math.min(RITUAL_TICKS - remaining, FILLING_TICKS);}

    private static int climbingBase(ItemStack stack, int remainingTicks) {
        int rolled = rolledBase(stack);
        return LOWEST_BASE + (rolled - LOWEST_BASE) * filledTicks(remainingTicks) / FILLING_TICKS;
    }

    @Override
    public @Nullable Float chargeFraction(ItemStack stack, int remainingTicks) {
        return rolledBase(stack) <= 0 ? 0.0F : climbingBase(stack, remainingTicks) / 100.0F;
    }

    @Override
    public int chargeColor(ItemStack stack, int remainingTicks) {
        return isHeldAtFull(remainingTicks) ? LOCKED_COLOR : FILLING_COLOR;
    }

    private static boolean isHeldAtFull(int remainingTicks) {return remainingTicks <= HELD_AT_FULL_TICKS;}

    @Override
    public @Nullable Component chargeCaption(ItemStack stack, int remainingTicks) {
        if (rolledBase(stack) <= 0) return null;
        return Component.translatable(CAPTION_TENTH + climbingBase(stack, remainingTicks) / 10);
    }
    //endregion
}
