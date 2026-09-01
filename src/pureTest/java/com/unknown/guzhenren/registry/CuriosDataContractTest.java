package com.unknown.guzhenren.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Snapshot of the Curios data files datagen writes, checked against ModCuriosProvider's declarations.
 *
 * <p>The provider creates one slot ({@code hands}, size 2) and attaches the player entity to
 * {@code hands}, {@code back}, {@code body} and {@code head} in that order. The tests list the
 * generated folders on the classpath and pin those exact shapes; back/body/head have no slot files
 * in this namespace because Curios itself ships them.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

class CuriosDataContractTest {

    private static final String ROOT = "/data/guzhenren/curios/";

    @Test
    @DisplayName("the slots folder holds exactly the hands slot at size 2")
    void slotsFolderMatchesProvider() {
        assertEquals(List.of("hands.json"), listJson("slots"));
        JsonObject hands = parse("slots/hands.json");
        assertEquals(Set.of("size"), hands.keySet());
        assertEquals(2, hands.get("size").getAsInt());
    }

    @Test
    @DisplayName("the player entity carries the provider's slot list in order")
    void playerEntityMatchesProvider() {
        assertEquals(List.of("player.json"), listJson("entities"));
        JsonObject player = parse("entities/player.json");
        assertEquals(Set.of("entities", "slots"), player.keySet());
        JsonArray entities = player.getAsJsonArray("entities");
        assertEquals(1, entities.size());
        assertEquals("minecraft:player", entities.get(0).getAsString());
        JsonArray slots = player.getAsJsonArray("slots");
        assertEquals(List.of("hands", "back", "body", "head"),
                slots.asList().stream().map(JsonElement::getAsString).toList());
    }

    @Test
    @DisplayName("the hands slot the provider creates is referenced by the player entity")
    void providerSlotIsReferenced() {
        JsonArray slots = parse("entities/player.json").getAsJsonArray("slots");
        List<String> referenced = slots.asList().stream().map(JsonElement::getAsString).toList();
        assertTrue(referenced.contains("hands"), () -> "hands missing from player slots: " + referenced);
    }

    private static List<String> listJson(String folder) {
        URL url = CuriosDataContractTest.class.getResource(ROOT + folder);
        assertNotNull(url, () -> "curios folder missing from the classpath: " + ROOT + folder);
        try (Stream<Path> entries = Files.list(Path.of(url.toURI()))) {
            return entries.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (Exception e) {
            throw new AssertionError("cannot list " + ROOT + folder + " at " + url, e);
        }
    }

    private static JsonObject parse(String path) {
        InputStream stream = CuriosDataContractTest.class.getResourceAsStream(ROOT + path);
        assertNotNull(stream, () -> "curios file missing from the classpath: " + ROOT + path);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("cannot read curios file " + path, e);
        }
    }
}
