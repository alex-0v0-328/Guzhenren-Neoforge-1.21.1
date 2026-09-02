package com.unknown.guzhenren.gametest;

import com.mojang.authlib.GameProfile;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.attachment.service.aperture.ApertureNourishService;
import com.unknown.guzhenren.attachment.service.aperture.AperturePressureExplosionTask;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.body.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.InfoModel;
import com.unknown.guzhenren.entity.BoarGuEntity;
import com.unknown.guzhenren.entity.FlyingGuEntity;
import com.unknown.guzhenren.entity.HopeGuEntity;
import com.unknown.guzhenren.item.GuItem;
import com.unknown.guzhenren.item.gu.RefinedGuState;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.menu.ApertureStorageMenu;
import com.unknown.guzhenren.registry.attachment.ModAttachments;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
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
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "starved Gu was removed from the inventory");
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
    @GameTest(template = "empty9x9x9", timeoutTicks = 200)
    public static void boarGuVariantsWanderWithoutSeekingPlayers(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, false);
        List<BoarGuEntity> variants = List.of(
                helper.spawn(ModEntityTypes.WHITE_BOAR_GU_ENTITY.get(), new BlockPos(2, 1, 2)),
                helper.spawn(ModEntityTypes.BLACK_BOAR_GU_ENTITY.get(), new BlockPos(4, 1, 2)),
                helper.spawn(ModEntityTypes.FLOWER_BOAR_GU_ENTITY.get(), new BlockPos(6, 1, 2)));
        List<Vec3> starts = variants.stream().map(BoarGuEntity::position).toList();
        for (BoarGuEntity variant : variants) helper.assertTrue(!variant.seeks(player), "boar gu seeks player");
        helper.succeedWhen(() -> {
            for (int i = 0; i < variants.size(); i++) {
                Vec3 travel = variants.get(i).position().subtract(starts.get(i));
                helper.assertTrue(Math.sqrt(travel.x * travel.x + travel.z * travel.z) > 0.05D,
                        "boar gu did not wander horizontally");
                helper.assertTrue(Math.abs(travel.y) > 0.05D, "boar gu did not wander vertically");
            }
        });
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 200)
    public static void boarGuFleesNearbyPlayer(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);
        player.moveTo(helper.absoluteVec(new Vec3(4.5D, 1.5D, 1.5D)));
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);

        BoarGuEntity gu = helper.spawn(ModEntityTypes.WHITE_BOAR_GU_ENTITY.get(), CENTER);
        helper.assertTrue(gu.distanceTo(player) <= BoarGuEntity.FLEE_RANGE, "player started outside flee range");

        helper.succeedWhen(() -> helper.assertTrue(gu.distanceTo(player) > BoarGuEntity.FLEE_RANGE,
                "boar gu stayed within flee range of the player"));
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void boarGuVariantsCatchAsMatchingItems(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);
        catchBoar(helper, player, ModEntityTypes.WHITE_BOAR_GU_ENTITY.get(), ModItems.WHITE_BOAR_GU.get());
        catchBoar(helper, player, ModEntityTypes.BLACK_BOAR_GU_ENTITY.get(), ModItems.BLACK_BOAR_GU.get());
        catchBoar(helper, player, ModEntityTypes.FLOWER_BOAR_GU_ENTITY.get(), ModItems.FLOWER_BOAR_GU.get());
        helper.succeed();
    }
    private static void catchBoar(GameTestHelper helper, ServerPlayer player, EntityType<BoarGuEntity> type,
                                  Item item) {
        BoarGuEntity gu = helper.spawn(type, CENTER);
        helper.assertTrue(gu.interact(player, InteractionHand.MAIN_HAND).consumesAction(),
                "boar gu interaction was not consumed");
        helper.assertValueEqual(player.getInventory().countItem(item), 1, "matching gu item missing");
        helper.assertTrue(!gu.isAlive(), "caught boar gu was not discarded");
        helper.assertTrue(helper.getEntities(EntityType.ITEM).isEmpty(), "caught boar gu dropped an item entity");
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void secondOnlyThenFirstApertureFlow(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);

        ApertureService.openSecondary(player, Rank.THREE);
        helper.assertValueEqual(ApertureService.get(player).count(), 1, "second-only aperture count");
        helper.assertTrue(ApertureService.aperture(player, 0).second(), "lone aperture must be flagged second");
        helper.assertTrue(!ApertureService.isAwakened(player), "second-only holder must not read awakened");
        helper.assertTrue(ApertureService.hasAperture(player), "second-only holder must read hasAperture");

        ItemStack vital = new ItemStack(ModItems.WHITE_BOAR_GU.get());
        ApertureStorageService.setVital(player, 0, vital);
        ApertureStorageService.set(player, 0, List.of(new ItemStack(ModItems.WHITE_BOAR_GU.get())));
        ApertureNourishService.start(player, 0);
        helper.assertTrue(ApertureNourishService.isCultivating(player), "nourish started on the lone second");

        ApertureService.awaken(player, 80);
        ApertureData data = ApertureService.get(player);
        helper.assertValueEqual(data.count(), 2, "count after Hope Gu inserts the first aperture");
        helper.assertValueEqual(data.firstIndex(), 0, "first aperture takes position 0");
        helper.assertValueEqual(data.secondIndex(), 1, "second aperture slides to position 1");
        helper.assertTrue(data.get(0).rank() == Rank.ONE, "first aperture rank");
        helper.assertValueEqual(data.get(1).rank(), Rank.THREE, "second aperture kept its rank");
        helper.assertTrue(GuItem.boundAperture(ApertureStorageService.vital(player, 1)) == 1,
                "vital gu binding followed the slide");
        helper.assertTrue(ApertureStorageService.items(player, 0).isEmpty(), "old store moved off position 0");
        helper.assertValueEqual(ApertureStorageService.items(player, 1).size(), 1, "store followed the slide");
        helper.assertTrue(ApertureNourishService.isCultivating(player)
                && ApertureNourishService.targetIndex(player) == 1, "nourish target followed the slide");

        ApertureService.openSecondary(player, Rank.FIVE);
        ApertureData upgraded = ApertureService.get(player);
        helper.assertValueEqual(upgraded.count(), 2, "upgrade keeps two apertures");
        helper.assertValueEqual(upgraded.secondIndex(), 1, "upgrade overwrites in place");
        helper.assertValueEqual(upgraded.get(1).rank(), Rank.FIVE, "upgrade lands the gu rank");
        helper.assertValueEqual(upgraded.get(1).primaryPath(), GuPath.STRENGTH, "upgrade keeps the bound path");
        helper.succeed();
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void storageMousePlacementKeepsOverflow(GameTestHelper helper) {
        ServerPlayer player = storagePlayer(helper);
        ApertureStorageMenu menu = new ApertureStorageMenu(1, player.getInventory(), ApertureData.PRIMARY, 0);
        menu.setCarried(new ItemStack(ModItems.SECOND_APERTURE_GU_5.get(), 64));

        menu.clicked(0, 0, ClickType.PICKUP, player);

        helper.assertValueEqual(storedCount(player, ApertureData.PRIMARY), 8, "mouse placement accepted count");
        helper.assertValueEqual(menu.getCarried().getCount(), 56, "mouse placement kept overflow");
        helper.succeed();
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void storageShiftMoveKeepsOverflow(GameTestHelper helper) {
        ServerPlayer player = storagePlayer(helper);
        player.getInventory().setItem(0, new ItemStack(ModItems.SECOND_APERTURE_GU_5.get(), 64));
        ApertureStorageMenu menu = new ApertureStorageMenu(1, player.getInventory(), ApertureData.PRIMARY, 0);

        menu.clicked(ApertureStorageMenu.PAGE_SIZE + 27, 0, ClickType.QUICK_MOVE, player);

        helper.assertValueEqual(storedCount(player, ApertureData.PRIMARY), 8, "shift move accepted count");
        helper.assertValueEqual(player.getInventory().getItem(0).getCount(), 56, "shift move kept overflow");
        helper.succeed();
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void storageSyncedLoadLimitsSlot(GameTestHelper helper) {
        ServerPlayer player = storagePlayer(helper);
        ApertureStorageMenu menu = new ApertureStorageMenu(1, player.getInventory(), ApertureData.PRIMARY, 0);
        menu.setData(2, 224);
        ItemStack incoming = new ItemStack(ModItems.SECOND_APERTURE_GU_5.get(), 64);

        helper.assertValueEqual(menu.getSlot(0).getMaxStackSize(incoming), 1, "synced load slot limit");
        helper.succeed();
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void storageLegacyOverloadCanBeReducedBySwap(GameTestHelper helper) {
        ServerPlayer player = storagePlayer(helper);
        player.setData(ModAttachments.APERTURE_STORAGE, ApertureStorage.DEFAULT.with(ApertureData.PRIMARY, List.of(
                new ItemStack(ModItems.SECOND_APERTURE_GU_5.get(), 9),
                new ItemStack(ModItems.SECOND_APERTURE_GU_5.get()))));
        ApertureStorageMenu menu = new ApertureStorageMenu(1, player.getInventory(), ApertureData.PRIMARY, 0);
        menu.setCarried(new ItemStack(ModItems.SECOND_APERTURE_GU_1.get()));

        menu.clicked(1, 0, ClickType.PICKUP, player);

        helper.assertTrue(ApertureStorageService.items(player, ApertureData.PRIMARY).get(1)
                .is(ModItems.SECOND_APERTURE_GU_1.get()), "legacy overload replacement was blocked");
        helper.assertValueEqual(ApertureStorageService.load(player, ApertureData.PRIMARY), 290,
                "legacy overload reduced load");
        helper.assertTrue(menu.getCarried().is(ModItems.SECOND_APERTURE_GU_5.get()),
                "legacy overload kept replaced gu on cursor");
        helper.succeed();
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void healthFollowsFirstApertureOnly(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);

        helper.assertValueEqual(player.getMaxHealth(), 20.0F, "mortal max health");
        ApertureService.openSecondary(player, Rank.THREE);
        helper.assertValueEqual(player.getMaxHealth(), 20.0F, "lone second aperture keeps mortal health");
        ApertureService.awaken(player, 80);
        helper.assertValueEqual(player.getMaxHealth(), 20.0F, "rank one first aperture keeps 20");
        ApertureService.setRank(player, ApertureData.PRIMARY, Rank.THREE);
        helper.assertValueEqual(player.getMaxHealth(), 60.0F, "first aperture rank three lifts to 60");
        ApertureService.openSecondary(player, Rank.FIVE);
        helper.assertValueEqual(player.getMaxHealth(), 60.0F, "second aperture never touches health");
        helper.succeed();
    }
    @GameTest(template = "empty9x9x9", timeoutTicks = 100)
    public static void infoModelAlwaysEmitsApertureTitleRows(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);

        helper.assertValueEqual(titles(player), List.of(new InfoModel.ApertureIndex(1, 0)),
                "mortal keeps the clickable first-aperture title row");
        ApertureService.awaken(player, 80);
        helper.assertValueEqual(titles(player), List.of(new InfoModel.ApertureIndex(1, 0)),
                "first-only holder keeps the clickable title row");
        ApertureService.openSecondary(player, Rank.THREE);
        helper.assertValueEqual(titles(player), List.of(new InfoModel.ApertureIndex(1, 0),
                new InfoModel.ApertureIndex(2, 1)), "two apertures keep both clickable title rows");
        helper.succeed();
    }
    private static List<InfoModel.ApertureIndex> titles(ServerPlayer player) {
        return InfoModel.aperture(player).stream().map(InfoModel.Row::entry)
                .filter(InfoModel.ApertureIndex.class::isInstance)
                .map(InfoModel.ApertureIndex.class::cast)
                .toList();
    }
    private static int storedCount(ServerPlayer player, int aperture) {
        return ApertureStorageService.items(player, aperture).stream().mapToInt(ItemStack::getCount).sum();
    }
    private static ServerPlayer storagePlayer(GameTestHelper helper) {
        ServerPlayer player = survivalMock(helper, null, true);
        ApertureService.awaken(player, 80);
        return player;
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
