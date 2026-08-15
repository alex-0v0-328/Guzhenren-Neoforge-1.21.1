package com.unknown.guzhenren.item.gu.mortal.soul;

import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.OneShotGuItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The Guts Gu [胆识蛊]: a one-shot soul Gu that raises the soul cap [魂魄上限] by ten.
 *
 * @author Alex
 * @since 1.0.0
 */
public class GutsGuItem extends OneShotGuItem {

    private static final int SOUL_BONUS = 10;

    public GutsGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected int useApply(ServerPlayer player, ItemStack stack) {
        SoulService.addMax(player, SOUL_BONUS);
        return 1;
    }
}
