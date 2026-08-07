package com.unknown.guzhenren.item;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MortalGuItem extends GuItem {

    private static final String KIND_KEY = "guzhenren.display.gu";

    protected static final String FAILED_REFINE_ESSENCE = "guzhenren.item.failed.refine_essence";
    protected static final String FAILED_ESSENCE = "guzhenren.item.failed.essence";

    protected static final String CAPTION_USING_PLAIN = "guzhenren.hud.using_plain";
    private static final String CAPTION_REFINING_PLAIN = "guzhenren.hud.refining_plain";
    private static final String CAPTION_REFINING = "guzhenren.hud.refining";

    protected final GuSpec spec;

    protected MortalGuItem(Properties properties, GuSpec spec) {
        super(properties, spec.rank(), spec.path());
        this.spec = spec;
        spec.validate(rank().name() + " " + getClass().getSimpleName());
    }

    @Override
    protected String kindKey() {return KIND_KEY;}
    public int refineCost() {return spec.refineCost();}

    //region refining [炼化] -- every Gu answers it, and only the price differs
    protected final @Nullable Refusal essenceGate(Player player, long required, String key) {
        if (required <= 0) return null;
        return EssenceService.spendable(player) < required ? new Refusal(key) : null;
    }

    protected final void payRefineCost(ServerPlayer player) {
        if (refineCost() > 0) EssenceService.consume(player, refineCost());
    }
    //endregion

    //region the click -- a Gu always answers the right click, if only to be refined
    @Override
    protected final boolean hasUse() {return true;}

    protected @Nullable Refusal useGate(Player player, ItemStack stack) {return null;}
    protected int useApply(ServerPlayer player, ItemStack stack) {return 0;}
    //endregion

    //region display
    protected final Component refineCaptionPlain() {return Component.translatable(CAPTION_REFINING_PLAIN);}

    protected final Component refineCaption(int invested) {
        return refineCost() > 0
                ? Component.translatable(CAPTION_REFINING, invested, refineCost())
                : refineCaptionPlain();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        MutableComponent line = progressLine(stack);
        if (line != null) tooltip.add(line.withStyle(ChatFormatting.GRAY));
    }

    protected @Nullable MutableComponent progressLine(ItemStack stack) {return null;}
    //endregion

    @Override
    public boolean onDroppedByPlayer(@NotNull ItemStack stack, @NotNull Player player) {return !isVital(stack);}
}
