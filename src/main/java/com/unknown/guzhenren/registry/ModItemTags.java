package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    private ModItemTags() {}

    public static final TagKey<Item> BOAR_FEED = key("boar_feed");

    public static final TagKey<Item> JIN_FEED = key("jin_feed");
    public static final TagKey<Item> JIN_FEED_DENSE = key("jin_feed_dense");
    public static final TagKey<Item> JIN_FEED_SMELTED = key("jin_feed_smelted");
    public static final TagKey<Item> JIN_FEED_SMELTED_DENSE = key("jin_feed_smelted_dense");

    public static final TagKey<Item> LIQUOR_FEED = key("liquor_feed");

    private static TagKey<Item> key(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, name));
    }
}
