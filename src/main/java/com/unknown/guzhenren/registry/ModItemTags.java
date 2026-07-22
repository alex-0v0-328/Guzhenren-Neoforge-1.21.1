package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

//  Keys only. The JSON is written at runData by datagen/item -- the ModDamageTypes shape.
public final class ModItemTags {

    private ModItemTags() {}

    //  What a boar Gu [豕蛊] eats: pork raw or cooked, and a live pig. A tag, so a sibling mod's pork joins.
    //  ⚠ One rate for the whole tag -- four of anything in it buy one hunger. There is no per-item value.
    public static final TagKey<Item> BOAR_FEED = key("boar_feed");

    //  What a jin strength Gu [斤力蛊] eats: raw iron, and raw iron blocks at nine times the value.
    //  ⚠ TWO tags precisely because they carry two rates -- one tag has room for exactly one.
    public static final TagKey<Item> JIN_FEED = key("jin_feed");
    public static final TagKey<Item> JIN_FEED_DENSE = key("jin_feed_dense");

    //  What a liquor worm [酒虫] drinks. ⚠ One tag, one rate -- the four ranks differ by how MANY units
    //  buy a hunger point (4/8/16/32), never by what a single liquor is worth.
    public static final TagKey<Item> LIQUOR_FEED = key("liquor_feed");

    private static TagKey<Item> key(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, name));
    }
}
