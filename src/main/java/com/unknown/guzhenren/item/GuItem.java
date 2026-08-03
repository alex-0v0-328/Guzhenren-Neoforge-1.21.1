package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.registry.ModDataComponents;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuItem extends Item {

    public static final int COOLDOWN_TICKS = 2;

    private final Rank rank;
    private final GuPath path;

    protected GuItem(Properties properties, Rank rank, GuPath path) {
        super(properties);
        this.rank = rank;
        this.path = path;
    }

    protected abstract String kindKey();
    public Rank rank() {return rank;}
    public GuPath path() {return path;}

    public record Refusal(String key, Object... args) {}

    //region Vital Gu
    public static @Nullable UUID owner(ItemStack s) {return s.get(ModDataComponents.VITAL_OWNER.get());}
    public static boolean isVital(ItemStack s) {return s.has(ModDataComponents.VITAL_OWNER.get());}
    public static boolean isVitalOf(ItemStack s, Player p) {return p.getUUID().equals(owner(s));}
    public static void bind(ItemStack s, Player p) {s.set(ModDataComponents.VITAL_OWNER.get(), p.getUUID());}
    //endregion

    //region the hooks a leaf fills
    protected boolean hasUse() {return false;}
    protected @Nullable Refusal gate(Player player, ItemStack stack) {return null;}
    protected int apply(ServerPlayer player, ItemStack stack) {return 0;}
    protected int useDurationTicks(Player player, ItemStack stack) {return 0;}
    protected boolean hasSneakUse(Player player, ItemStack stack) {return false;}
    protected @Nullable Refusal sneakGate(Player player, ItemStack stack) {return null;}
    protected int sneakApply(ServerPlayer player, ItemStack stack) {return 0;}
    public @Nullable Component chargeCaption(ItemStack stack) {return null;}
    protected boolean feedsFromOffhand() {return false;}
    //endregion

    private InteractionResultHolder<ItemStack> refused(ServerPlayer player, Refusal refusal, ItemStack stack) {
        if (player != null) refuse(player, refusal.key(), refusal.args());
        return feedsFromOffhand()
                ? InteractionResultHolder.consume(stack)
                : InteractionResultHolder.fail(stack);
    }

    @Override
    public final @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                                 @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean sneak = isSneakUse(player, stack);
        if (!sneak && !hasUse()) return super.use(level, player, hand);

        Refusal refusal = sneak ? sneakGate(player, stack) : gate(player, stack);
        if (refusal != null) {
            return refused(player instanceof ServerPlayer server ? server : null, refusal, stack);
        }
        if (useDurationTicks(player, stack) > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer server) {
            spend(server, stack, sneak ? sneakApply(server, stack) : apply(server, stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public final @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level,
                                                    @NotNull LivingEntity entity) {
        if (!hasUse()) return super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof Player player)) return stack;

        boolean sneak = isSneakUse(player, stack);
        Refusal refusal = sneak ? sneakGate(player, stack) : gate(player, stack);
        if (refusal != null) {
            if (player instanceof ServerPlayer server) refuse(server, refusal.key(), refusal.args());
            return stack;
        }
        if (player instanceof ServerPlayer server) {
            spend(server, stack, sneak ? sneakApply(server, stack) : apply(server, stack));
        }
        return stack;
    }

    public static boolean crouching(Player player) {return player.isCrouching();}

    private boolean isSneakUse(Player player, ItemStack stack) {
        return crouching(player) && hasSneakUse(player, stack);
    }

    @Override
    public final int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        if (!hasUse()) return super.getUseDuration(stack, entity);
        return entity instanceof Player player ? useDurationTicks(player, stack) : 0;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return hasUse() ? UseAnim.NONE : super.getUseAnimation(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(ModDisplayText.guLine(rank, path, kindKey()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return isVital(stack) ? ModDisplayText.vital(super.getName(stack)) : super.getName(stack);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {return isVital(stack) || super.isFoil(stack);}

    protected static void refuse(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.RED), true);
    }

    protected static void inform(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    protected int cooldownTicks(ItemStack stack) {return COOLDOWN_TICKS;}

    protected void spend(ServerPlayer player, ItemStack stack, int count) {
        player.getCooldowns().addCooldown(this, cooldownTicks(stack));
        if (count > 0 && !player.hasInfiniteMaterials()) stack.shrink(count);
    }
}
