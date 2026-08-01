package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.MortalGuItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LifespanGuItem extends MortalGuItem {

    private static final String MSG_GAINED = "guzhenren.item.gu.lifespan_gained";

    private final int minYears;
    private final int maxYears;

    public LifespanGuItem(Properties properties, int minYears, int maxYears) {
        super(properties, Rank.ONE, GuPath.HEAVEN, false, false);
        this.minYears = minYears;
        this.maxYears = maxYears;
    }

    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int years = minYears + player.getRandom().nextInt(maxYears - minYears + 1);
        BodyService.addLifespan(player, years);
        inform(player, MSG_GAINED, years);
        return 1;
    }
}
