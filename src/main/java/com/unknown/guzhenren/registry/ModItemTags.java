package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * The item tags this mod declares, most of them the larders a Gu feeds from.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class ModItemTags {

    private ModItemTags() {}

    public static final TagKey<Item> BOAR_FEED = key("boar_feed");
    public static final TagKey<Item> BEAR_FEED = key("bear_feed");
    public static final TagKey<Item> BEEF_FEED = key("beef_feed");
    public static final TagKey<Item> RABBIT_FEED = key("rabbit_feed");

    public static final TagKey<Item> JIN_FEED = key("jin_feed");
    public static final TagKey<Item> JIN_FEED_DENSE = key("jin_feed_dense");
    public static final TagKey<Item> JIN_FEED_SMELTED = key("jin_feed_smelted");
    public static final TagKey<Item> JIN_FEED_SMELTED_DENSE = key("jin_feed_smelted_dense");

    public static final TagKey<Item> LIQUOR_FEED = key("liquor_feed");

    public static final TagKey<Item> ALL_OUT_FEED = key("all_out_feed");

    public static final TagKey<Item> ZOMBIE_FEED = key("zombie_feed");

    private static TagKey<Item> key(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, name));
    }
}
