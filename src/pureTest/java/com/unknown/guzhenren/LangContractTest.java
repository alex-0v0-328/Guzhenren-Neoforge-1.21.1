package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract checks over the two generated lang files, read straight from the classpath.
 *
 * <p>Both languages must carry exactly the same key set, no value may be blank outside the three
 * {@code none} enum display keys (Rank.NONE, Stage.NONE and ExtremePhysique.NONE are never rendered
 * as a name, and the blank set must stay symmetric across languages), and the zh_cn bracket
 * annotations must keep the house style: whatever sits inside {@code [...]} never contains
 * whitespace.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

class LangContractTest {

    private static final Pattern BRACKETS = Pattern.compile("\\[[^\\[\\]]*\\]");

    private static final Set<String> BLANK_BY_DESIGN = Set.of(
            "guzhenren.enum.aperture.rank.none",
            "guzhenren.enum.aperture.stage.none",
            "guzhenren.enum.body.extreme_physique.none");

    @Test
    @DisplayName("en_us and zh_cn carry exactly the same key set")
    void languagesShareKeySets() {
        JsonObject en = load("en_us");
        JsonObject zh = load("zh_cn");
        List<String> onlyInEn = keysMissingFrom(en, zh);
        List<String> onlyInZh = keysMissingFrom(zh, en);
        assertTrue(onlyInEn.isEmpty() && onlyInZh.isEmpty(),
                () -> "key sets differ -- only in en_us: " + onlyInEn + ", only in zh_cn: " + onlyInZh);
        assertEquals(en.keySet(), zh.keySet());
    }

    @Test
    @DisplayName("zh_cn bracket annotations never contain whitespace")
    void zhBracketsHaveNoWhitespace() {
        JsonObject zh = load("zh_cn");
        for (Map.Entry<String, JsonElement> entry : zh.entrySet()) {
            String value = entry.getValue().getAsString();
            Matcher matcher = BRACKETS.matcher(value);
            while (matcher.find()) {
                String inside = matcher.group().substring(1, matcher.group().length() - 1);
                for (int i = 0; i < inside.length(); i++) {
                    char current = inside.charAt(i);
                    assertFalse(Character.isWhitespace(current),
                            () -> "bracket annotation carries whitespace under key " + entry.getKey() + ": ["
                                    + inside + "]");
                }
            }
        }
    }

    @Test
    @DisplayName("only the none enum keys may be blank, and identically in both languages")
    void onlyNoneKeysAreBlank() {
        for (String language : List.of("en_us", "zh_cn")) {
            JsonObject lang = load(language);
            Set<String> blanks = new TreeSet<>();
            for (Map.Entry<String, JsonElement> entry : lang.entrySet()) {
                assertTrue(entry.getValue().isJsonPrimitive(),
                        () -> "value of " + entry.getKey() + " in " + language + " is not a plain string");
                if (entry.getValue().getAsString().isBlank()) blanks.add(entry.getKey());
            }
            assertEquals(BLANK_BY_DESIGN, blanks,
                    () -> "blank values in " + language + " deviate from the none enum keys");
        }
    }

    private static List<String> keysMissingFrom(JsonObject source, JsonObject target) {
        List<String> missing = new ArrayList<>();
        for (String key : source.keySet()) {
            if (!target.has(key)) missing.add(key);
        }
        return missing;
    }

    private static JsonObject load(String language) {
        InputStream stream = LangContractTest.class.getResourceAsStream(
                "/assets/guzhenren/lang/" + language + ".json");
        assertNotNull(stream, () -> "lang file missing from the classpath: " + language);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("cannot read lang file " + language, e);
        }
    }
}
