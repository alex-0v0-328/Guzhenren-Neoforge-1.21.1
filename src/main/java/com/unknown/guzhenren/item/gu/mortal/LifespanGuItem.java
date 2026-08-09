package com.unknown.guzhenren.item.gu.mortal;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.OneShotGuItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LifespanGuItem extends OneShotGuItem {

    private static final String MSG_GAINED = "guzhenren.item.gu.lifespan_gained";

    private final int minYears;
    private final int maxYears;

    public LifespanGuItem(Properties properties, int minYears, int maxYears, GuSpec spec) {
        super(properties, spec);
        this.minYears = minYears;
        this.maxYears = maxYears;
    }

    @Override
    protected int useApply(ServerPlayer player, ItemStack stack) {
        int years = minYears + player.getRandom().nextInt(maxYears - minYears + 1);
        BodyService.addLifespan(player, years);
        inform(player, MSG_GAINED, years);
        return 1;
    }
}
