package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.effect.FlowerBoarGuEffect;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import com.unknown.guzhenren.registry.ModEffects;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FlowerBoarGuItem extends RefinableGuItem {

    private static final String FAILED_COOLDOWN = "guzhenren.item.failed.flower_boar_cooldown";
    private static final String TOOLTIP_HUNGER = "guzhenren.item.gu.hunger_progress";

    private static final int PORK_UNITS = 1;

    private static final int REFINE_COST = 1200;
    private static final int MAX_HUNGER = 8;
    private static final int HUNGER_PER_USE = 2;

    private static final int USE_COOLDOWN_TICKS = 2 * Ticks.MINUTE;

    public FlowerBoarGuItem(Properties properties) {
        super(properties, Rank.ONE, GuPath.STRENGTH);
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return REFINE_COST;}

    @Override
    protected int maxHunger() {return MAX_HUNGER;}

    @Override
    protected int hungerPerUse(ItemStack stack) {return HUNGER_PER_USE;}

    @Override
    public int usesPerGrant() {return 1;}
    //endregion

    @Override
    protected int feedUnits(ItemStack food) {return food.is(ModItemTags.BOAR_FEED) ? PORK_UNITS : 0;}

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        return refined(stack) ? 0 : super.useDurationTicks(player, stack);
    }

    //region the cooldown -- vanilla draws it, the stamp is the truth
    private static long ticksLeft(Player player, ItemStack stack) {
        Long usedAt = stack.get(ModDataComponents.FLOWER_BOAR_USED_AT.get());
        MinecraftServer server = player.getServer();
        if (usedAt == null || server == null) return 0L;
        long elapsed = server.overworld().getGameTime() - usedAt;
        return elapsed < 0L || elapsed >= USE_COOLDOWN_TICKS ? 0L : USE_COOLDOWN_TICKS - elapsed;
    }

    @Override
    protected void spend(ServerPlayer player, ItemStack stack, int count) {
        super.spend(player, stack, count);
        long left = ticksLeft(player, stack);
        if (left > 0L) player.getCooldowns().addCooldown(this, (int) left);
    }
    //endregion

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        long left = ticksLeft(player, stack);
        if (left <= 0L) return null;
        if (player instanceof ServerPlayer server) server.getCooldowns().addCooldown(this, (int) left);
        long seconds = (left + Ticks.SECOND - 1) / Ticks.SECOND;
        return new Refusal(FAILED_COOLDOWN, Component.literal(String.valueOf(seconds)));
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.FLOWER_BOAR_GU, FlowerBoarGuEffect.DURATION_TICKS));
        stack.set(ModDataComponents.FLOWER_BOAR_USED_AT.get(), player.server.overworld().getGameTime());
    }

    @Override
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_HUNGER, state(stack).hunger(), maxHunger())
                : super.progressLine(stack);
    }
}
