package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.ModDisplayText;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  What every Gu (蛊) item is: a rank, a path, the one tooltip line, and the shape of a right-click.
//  ⚠ use() gates on both sides (APERTURE is synced to its owner) but only writes through a ServerPlayer;
//  a subclass fills in gate() + apply() and never re-implements that plumbing.
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

    //  A plain Gu/material just sits in the inventory -- right-click passes through, as vanilla does.
    //  A usable Gu overrides hasUse() -> true and fills in gate() + apply().
    protected boolean hasUse() {return false;}

    //  Both sides compute the gate (reads work client-side); null means it may be used.
    protected @Nullable Refusal gate(Player player) {return null;}

    //  Server only, once the gate passed: do the write, return how many stacks to spend (0 = reusable).
    protected int apply(ServerPlayer player, ItemStack stack) {return 0;}

    @Override
    public final @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                                 @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasUse()) return InteractionResultHolder.pass(stack);

        Refusal refusal = gate(player);
        if (refusal != null) {
            if (player instanceof ServerPlayer server) refuse(server, refusal.key(), refusal.args());
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer server) spend(server, stack, apply(server, stack));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(ModDisplayText.guLine(rank, path, kindKey()).withStyle(ChatFormatting.GRAY));
    }

    //  Refused: red on the action bar, nothing spent. Same class as a command's red -- see CLAUDE.md "Color".
    protected static void refuse(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.RED), true);
    }

    //  The cooldown always; the stack only by what the use ate -- 0 for a reusable one.
    //  ⚠ Creative pays the cooldown, not the item.
    protected void spend(ServerPlayer player, ItemStack stack, int count) {
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        if (count > 0 && !player.hasInfiniteMaterials()) stack.shrink(count);
    }
}
