package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//  Mortal Gu [凡蛊] -- a Gu of ranks 1..5. Concrete: a plain Gu needs no class of its own.
//  ⚠⚠ The two booleans ARE the taxonomy: 一次性无需喂食 (false,false) · 多次使用需要喂食 (true,true)
//  · 一次性需要喂食 (false,true), representable and deliberately unbuilt.  CLAUDE.md "Items".
public class MortalGuItem extends GuItem {

    private static final String KIND_KEY = "guzhenren.display.gu";
    private static final String CHARGE_CAPTION = "guzhenren.hud.using_plain";

    //  ⚠⚠ A one-shot Gu [一次性蛊虫] CHARGES for half a second (2026-07-30) -- driving a Gu is never
    //  free, however small the Gu. That is why the category is no longer called 即用蛊虫.
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
    //  ⚠ CLASSIFICATION ONLY since the left-click template was deleted (2026-07-30) -- nothing reads it
    //  today. It stays because it is one of the two axes, and 一次性+需要喂食 is a real unbuilt cell.
    public boolean feedable() {return feedable;}

    //  ⚠⚠ The half second belongs to the ONE-SHOT axis, not to 凡蛊 at large: `reusable` is what says
    //  which. RefinableGuItem is the reusable side and replaces this with its rank buckets, the only
    //  place the charge length is allowed to vary.
    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        return reusable ? super.useDurationTicks(player, stack) : ONE_SHOT_CHARGE_TICKS;
    }

    //  No number to show -- the bar IS the progress. Same shape QiMaterialItem's 炼化中 caption uses.
    @Override
    public Component chargeCaption(ItemStack stack) {return Component.translatable(CHARGE_CAPTION);}

    //  ⚠ A Vital Gu cannot be thrown away. This hook covers ONLY the Q-drop of the selected hotbar slot
    //  (ServerPlayer.drop); every GUI path goes through ItemTossEvents instead. Both are needed.
    @Override
    public boolean onDroppedByPlayer(@NotNull ItemStack stack, @NotNull Player player) {return !isVital(stack);}
}
