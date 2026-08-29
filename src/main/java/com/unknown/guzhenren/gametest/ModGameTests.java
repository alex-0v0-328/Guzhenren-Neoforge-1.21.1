package com.unknown.guzhenren.gametest;

import com.mojang.authlib.GameProfile;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.AperturePressureExplosionTask;
import com.unknown.guzhenren.custom.enums.body.ExtremePhysique;
import com.unknown.guzhenren.entity.FlyingGuEntity;
import com.unknown.guzhenren.entity.HopeGuEntity;
import com.unknown.guzhenren.item.gu.RefinedGuState;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.registry.entity.ModEntityTypes;
import com.unknown.guzhenren.registry.item.ModDataComponents;
import com.unknown.guzhenren.registry.item.ModItems;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Runtime behavior tests executed inside a real server tick loop via {@code runGameTestServer}.
 * Every method pins one load-bearing mechanic that plain unit tests cannot reach: the Gu hunger
 * day clock, the frame-spread pressure crater, and the entity seek window. Scenes use the
 * committed all-air structure {@code empty9x9x9}; bigger scenes need a bigger committed template.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

@GameTestHolder(Guzhenren.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ModGameTests {

    private static final BlockPos CENTER = new BlockPos(4, 1, 4);
    private static final String HUNGRY_KEY = "guzhenren.item.gu.hungry";
    private static final String STARVED_KEY = "guzhenren.item.gu.starved";

    private ModGameTests() {}

    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void sceneSupportsBlockPlacement(GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.DIRT);
        helper.succeedWhen(() -> helper.assertBlockState(CENTER, state -> state.is(Blocks.DIRT), () -> "dirt missing"));
    }

    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void tendedGuHungerClock(GameTestHelper helper) {
        List<Component> inbox = new ArrayList<>();
        ServerPlayer player = survivalMock(helper, inbox, false);
        ItemStack gu = new ItemStack(ModItems.WHITE_BOAR_GU.get());
        gu.set(ModDataComponents.REFINED_GU_STATE.get(), new RefinedGuState(true, 0, 0, 3, 0));
        player.getInventory().setItem(0, gu);

        TendedGuItem.tickCarried(player, 0L);
        helper.assertValueEqual(TendedGuItem.state(gu).hunger(), 3, "hunger without a day boundary");
        helper.assertTrue(messages(inbox, HUNGRY_KEY).isEmpty(), "hungry broadcast without a day boundary");

        TendedGuItem.tickCarried(player, 1L);
        helper.assertValueEqual(TendedGuItem.state(gu).hunger(), 2, "hunger after one day");
        helper.assertTrue(messages(inbox, HUNGRY_KEY).isEmpty(), "hungry broadcast while above the threshold");

        TendedGuItem.tickCarried(player, 1L);
        helper.assertValueEqual(TendedGuItem.state(gu).hunger(), 1, "hunger at the hungry threshold");
        helper.assertValueEqual(messages(inbox, HUNGRY_KEY).size(), 1, "hungry broadcast count");

        TendedGuItem.tickCarried(player, 0L);
        helper.assertValueEqual(messages(inbox, HUNGRY_KEY).size(), 1, "hungry broadcast latched to once per day");

        TendedGuItem.tickCarried(player, 1L);
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "starved Gu stayed in the inventory");
        helper.assertValueEqual(TendedGuItem.state(gu).hunger(), 0, "hunger after starving");
        helper.assertValueEqual(messages(inbox, STARVED_KEY).size(), 1, "starved broadcast count");
        helper.succeed();
    }

    @GameTest(template = "empty9x9x9", timeoutTicks = 200)
    public static void pressureExplosionSpreadsOverTicks(GameTestHelper helper) {
        for (int x = 2; x <= 6; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = 2; z <= 6; z++) helper.setBlock(new BlockPos(x, y, z), Blocks.DIRT);
            }
        }
        Vec3 center = helper.absoluteVec(new Vec3(4.5D, 1.5D, 4.5D));
        AperturePressureExplosionTask.start(helper.getLevel(), center.x, center.y, center.z, 2, ExtremePhysique.NONE);

        helper.succeedWhen(() -> {
            for (int x = 2; x <= 6; x++) {
                for (int y = 0; y <= 2; y++) {
                    for (int z = 2; z <= 6; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        boolean inner = x >= 3 && x <= 5 && z >= 3 && z <= 5;
                        helper.assertBlockState(pos, state -> inner == state.isAir(),
                                () -> inner ? "crater block survived" : "rim block was cleared");
                    }
                }
            }
            helper.assertTrue(helper.getEntities(EntityType.ITEM).isEmpty(), "crater dropped item entities");
        });
    }

    @GameTest(template = "empty9x9x9", timeoutTicks = 200)
    public static void hopeGuSeeksUnawakenedPlayer(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);
        Vec3 spot = helper.absoluteVec(new Vec3(4.5D, 1.5D, 0.5D));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);

        HopeGuEntity gu = helper.spawn(ModEntityTypes.HOPE_GU_ENTITY.get(), CENTER);
        Vec3 start = gu.position();
        Vec3 towardPlayer = player.position().subtract(start);
        double startDistance = towardPlayer.length();
        helper.assertTrue(startDistance > FlyingGuEntity.HOVER_RANGE, "player started inside hover range");
        helper.assertTrue(startDistance <= FlyingGuEntity.DETECT_RANGE, "player started outside detect range");

        helper.succeedWhen(() -> {
            helper.assertTrue(gu.isAlive(), "hope gu vanished");
            Vec3 travel = gu.position().subtract(start);
            helper.assertTrue(travel.dot(towardPlayer) > 1.0D, "hope gu did not fly toward the player");
            helper.assertTrue(gu.distanceTo(player) < startDistance - 1.0D, "hope gu distance did not shrink");
        });
    }

    private static List<Component> messages(List<Component> inbox, String key) {
        List<Component> hits = new ArrayList<>();
        for (Component message : inbox) {
            if (message.getContents() instanceof TranslatableContents content && content.getKey().equals(key)) {
                hits.add(message);
            }
        }
        return hits;
    }

    private static ServerPlayer survivalMock(GameTestHelper helper, @Nullable List<Component> inbox, boolean connect) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "gzr-gametest-mock"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public boolean isSpectator() {return false;}
            @Override
            public boolean isCreative() {return false;}
            @Override
            public void sendSystemMessage(Component message) {if (inbox != null) inbox.add(message);}
        };
        if (!connect) {
            player.getAbilities().instabuild = false;
            return player;
        }
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
            @Override
            public void send(Packet<?> packet, @Nullable PacketSendListener listener) {}
        };
        helper.getLevel().addNewPlayer(player);
        player.getAbilities().instabuild = false;
        return player;
    }
}
