package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.MortalGuItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

//  Lifespan Gu [寿蛊]: one click hands back a random span of years, and the Gu is gone.
//  ⚠ ONE class, three items -- the range is what registration chose, not what the class is, exactly as
//  the Primeval Stone's essence value is.
//  ⚠ No refining, no feeding, and NO gate: a mortal ages too, so lifespan is ungated everywhere in this
//  mod -- see CLAUDE.md, all of /gzr body is ungated for the same reason.
public class LifespanGuItem extends MortalGuItem {

    private static final String MSG_GAINED = "guzhenren.item.gu.lifespan_gained";

    private final int minYears;
    private final int maxYears;

    //  Rank I, Heaven Path. Neither reusable nor feedable -- it is spent the moment it works.
    public LifespanGuItem(Properties properties, int minYears, int maxYears) {
        super(properties, Rank.ONE, GuPath.HEAVEN, false, false);
        this.minYears = minYears;
        this.maxYears = maxYears;
    }

    @Override
    protected boolean hasUse() {return true;}

    //  ⚠ Inclusive on both ends: nextInt's bound is exclusive, so the span needs the +1. 1..9 is nine
    //  outcomes, not eight.
    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int years = minYears + player.getRandom().nextInt(maxYears - minYears + 1);
        BodyService.addLifespan(player, years);
        //  ⚠ The roll is why this one talks. 86 -> 87 on the HUD is unreadable, and it is the FIRST item
        //  whose outcome the player cannot see for himself.  CLAUDE.md "Items".
        inform(player, MSG_GAINED, years);
        return 1;
    }
}
