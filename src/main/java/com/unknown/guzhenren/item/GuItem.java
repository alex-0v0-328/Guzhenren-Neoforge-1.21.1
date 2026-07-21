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

//  What every Gu item is: a rank, a path, one tooltip line, and the shape of its two clicks.
//  ⚠ Each template gates on both sides but writes only through a ServerPlayer; a subclass never re-implements that.
public abstract class GuItem extends Item {

    public static final int COOLDOWN_TICKS = 2;

    private final Rank rank;
    private final GuPath path;

    protected GuItem(Properties properties, Rank rank, GuPath path) {
        super(properties);
        this.rank = rank;
        this.path = path;
    }

    //  Gu (蛊虫) or Gu Material (蛊材) -- the tooltip's last word. The class says which; nothing else may.
    protected abstract String kindKey();
    public Rank rank() {return rank;}
    public GuPath path() {return path;}

    //  Why a use was refused: a red action-bar message. args feed the key's placeholders.
    public record Refusal(String key, Object... args) {}

    //region Vital Gu
    //  ⚠ Written once, by the aperture's Vital Gu slot, and NEVER cleared -- taking it back out does not
    //  unbind it. The owner is stored so a Gu handed to someone else still bills its loss to him.
    public static @Nullable UUID owner(ItemStack s) {return s.get(ModDataComponents.VITAL_OWNER.get());}
    public static boolean isVital(ItemStack s) {return s.has(ModDataComponents.VITAL_OWNER.get());}
    public static boolean isVitalOf(ItemStack s, Player p) {return p.getUUID().equals(owner(s));}
    public static void bind(ItemStack s, Player p) {s.set(ModDataComponents.VITAL_OWNER.get(), p.getUUID());}
    //endregion

    //  A plain Gu/material just sits in the inventory -- right-click passes through, as vanilla does.
    //  A usable Gu overrides hasUse() -> true and fills in gate() + apply().
    protected boolean hasUse() {return false;}

    //  Both sides compute the gate (reads work client-side); null means it may be used.
    protected @Nullable Refusal gate(Player player, ItemStack stack) {return null;}

    //  Server only, once the gate passed: do the write, return how many stacks to spend (0 = reusable).
    protected int apply(ServerPlayer player, ItemStack stack) {return 0;}

    //  0 = the click applies at once, which is what every Gu but a refinable one does. Above 0, the
    //  click starts vanilla's hold instead and finishUsingItem is what applies it.
    protected int useDurationTicks(Player player, ItemStack stack) {return 0;}

    @Override
    public final @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                                 @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasUse()) return InteractionResultHolder.pass(stack);

        Refusal refusal = gate(player, stack);
        if (refusal != null) {
            if (player instanceof ServerPlayer server) refuse(server, refusal.key(), refusal.args());
            //  ⚠ consume, not fail: FAIL does not consumesAction(), so Minecraft.startUseItem would fall
            //  through and use the OTHER hand -- a refused Gu would eat the food held for it.
            return InteractionResultHolder.consume(stack);
        }
        //  A charged use only STARTS here; letting go early simply applies nothing.
        if (useDurationTicks(player, stack) > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer server) spend(server, stack, apply(server, stack));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    //  The charged use landed. ⚠ Re-gates: four seconds is long enough for the essence to run dry or
    //  the Gu to go hungry, and the gate that passed on the click is stale by now.
    @Override
    public final @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level,
                                                    @NotNull LivingEntity entity) {
        if (!hasUse() || !(entity instanceof Player player)) return stack;

        Refusal refusal = gate(player, stack);
        if (refusal != null) {
            if (player instanceof ServerPlayer server) refuse(server, refusal.key(), refusal.args());
            return stack;
        }
        if (player instanceof ServerPlayer server) spend(server, stack, apply(server, stack));
        return stack;
    }

    @Override
    public final int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return entity instanceof Player player ? useDurationTicks(player, stack) : 0;
    }

    //  ⚠ NONE, not BOW: a Gu is not drawn like a weapon, and the hotbar bar is the whole feedback.
    //  The movement slowdown vanilla applies while using still lands, which suits a four-second channel.
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {return UseAnim.NONE;}

    //  ⚠ No gate and no refusal, unlike use(): left-click is also mining and fighting, so hasSwing must
    //  answer the whole question and a swing it cannot serve passes silently. Takes the player for that.
    protected boolean hasSwing(Player player, ItemStack stack) {return false;}
    protected int swingApply(ServerPlayer player, ItemStack stack) {return 0;}

    //  ⚠ ItemCooldowns gates use() but not this, and continueAttack swings every tick while left-click is
    //  held -- without the guard below a feeding Gu would eat twenty items a second while mining.
    @Override
    public final boolean onEntitySwing(@NotNull ItemStack stack, @NotNull LivingEntity entity,
                                       @NotNull InteractionHand hand) {
        if (!(entity instanceof Player player) || !hasSwing(player, stack)) return false;
        if (player.getCooldowns().isOnCooldown(this)) return false;
        if (player instanceof ServerPlayer server) spend(server, stack, swingApply(server, stack));

        //  ⚠ Never true: that would cancel the arm animation and the ClientboundAnimatePacket with it.
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(ModDisplayText.guLine(rank, path, kindKey()).withStyle(ChatFormatting.GRAY));
    }

    //  本命·黑豕蛊 once bound. ⚠ RefinableGuItem's Wild prefix wraps THIS, never the other way round.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return isVital(stack) ? ModDisplayText.vital(super.getName(stack)) : super.getName(stack);
    }

    //  The glint is the mark -- a bound Gu has to be findable in a full inventory at a glance.
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {return isVital(stack) || super.isFoil(stack);}

    //  Refused: red on the action bar, nothing spent. Same class as a command's red --  CLAUDE.md "Color".
    protected static void refuse(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.RED), true);
    }

    //  Stack-sensitive: one Gu's actions can differ in weight -- a slow refine, a quick use.
    protected int cooldownTicks(ItemStack stack) {return COOLDOWN_TICKS;}

    //  The cooldown always; the stack only by what the use ate -- 0 for a reusable one.
    //  ⚠ Creative pays the cooldown, not the item.
    protected void spend(ServerPlayer player, ItemStack stack, int count) {
        player.getCooldowns().addCooldown(this, cooldownTicks(stack));
        if (count > 0 && !player.hasInfiniteMaterials()) stack.shrink(count);
    }
}
