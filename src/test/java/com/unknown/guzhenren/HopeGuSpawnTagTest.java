package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HopeGuSpawnTagTest {

    @Test
    @DisplayName("wild Gu spawn tag contains the exact land biome contract")
    void containsExactLandBiomeSet() {
        try (InputStreamReader reader = new InputStreamReader(
                getResource(),
                StandardCharsets.UTF_8)) {
            JsonArray values = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("values");
            Set<String> expected = Set.of(
                    "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "minecraft:cherry_grove",
                    "minecraft:flower_forest", "minecraft:forest", "minecraft:birch_forest", "minecraft:dark_forest",
                    "minecraft:old_growth_birch_forest", "minecraft:windswept_forest", "minecraft:taiga",
                    "minecraft:snowy_taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
                    "minecraft:grove", "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:windswept_savanna",
                    "minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle", "minecraft:desert",
                    "minecraft:badlands", "minecraft:wooded_badlands", "minecraft:eroded_badlands", "minecraft:snowy_plains",
                    "minecraft:ice_spikes", "minecraft:snowy_slopes", "minecraft:frozen_peaks", "minecraft:jagged_peaks",
                    "minecraft:stony_peaks", "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills",
                    "minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:beach", "minecraft:snowy_beach",
                    "minecraft:stony_shore", "minecraft:mushroom_fields");
            Set<String> actual = values.asList().stream().map(JsonPrimitive.class::cast)
                    .map(JsonPrimitive::getAsString).collect(Collectors.toSet());
            assertEquals(expected.size(), values.size());
            assertEquals(expected, actual);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private InputStream getResource() {
        InputStream stream = getClass().getResourceAsStream(
                "/data/guzhenren/tags/worldgen/biome/hope_gu_spawns.json");
        assertNotNull(stream);
        return stream;
    }
}
