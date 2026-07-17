package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.ModDisplayText;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

//  What every 蛊 item is: a rank, a path, and the one tooltip line -- plus what a use costs.
//  ⚠ A use gates on both sides (APERTURE is synced to its owner), but only writes through a ServerPlayer.
public abstract class GuItem extends Item {

    public static final int COOLDOWN_TICKS = 2;

    private final Rank rank;
    private final GuPath path;

    protected GuItem(Properties properties, Rank rank, GuPath path) {
        super(properties);
        this.rank = rank;
        this.path = path;
    }

    //  蛊虫 / 蛊材 -- the last word of 一转人道蛊虫. The class says which; nothing else may.
    protected abstract String kindKey();

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(ModDisplayText.guLine(rank, path, kindKey()).withStyle(ChatFormatting.GRAY));
    }

    //  Refused: red on the action bar, nothing spent. Same class as a command's red -- see CLAUDE.md "Color".
    protected static void refuse(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
    }

    //  The cooldown always; the stack only by what the use ate -- 0 for a reusable one.
    //  ⚠ Creative pays the cooldown, not the item.
    protected void spend(ServerPlayer player, ItemStack stack, int count) {
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        if (count > 0 && !player.hasInfiniteMaterials()) stack.shrink(count);
    }
}
