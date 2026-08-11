package com.unknown.guzhenren.item.gu.mortal.zombie;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The shared class behind every zombie Gu [僵尸蛊]; the form's length and health come from registration.
 *
 * <p>⚠ Using one again too soon is not refused -- it is the penalty, and it makes the form permanent.
 * That branch leaves early, so anything both outcomes need has to sit ahead of it.
 *
 * @author Alex
 * @since 1.0.0
 */
public class ZombieGuItem extends TendedGuItem {

    private static final String FAILED_ALREADY_ZOMBIE = "guzhenren.item.failed.zombie_already";

    private final int maxHealth;
    private final int halfZombieTicks;

    public ZombieGuItem(Properties properties, int maxHealth, int halfZombieTicks, GuSpec spec) {
        super(properties, spec);
        this.maxHealth = maxHealth;
        this.halfZombieTicks = halfZombieTicks;
    }

    @Override
    public int maxHealth() {return maxHealth;}

    private int rung() {return rank().ordinal() - Rank.TWO.ordinal();}

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return BodyService.isZombie(player) ? new Refusal(FAILED_ALREADY_ZOMBIE) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        if (BodyService.wouldRelapse(player)) {
            BodyService.turnZombie(player, rung());
            return;
        }
        BodyService.enterHalfZombie(player, rung(), halfZombieTicks);
    }
}
