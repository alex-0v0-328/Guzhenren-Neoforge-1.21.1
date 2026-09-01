package com.unknown.guzhenren.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.body.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.body.Physique;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class HudDisplayTest {

    @Test
    void hudHeaderCombinesRealmTitleAndAptitude() {
        Component header = ModDisplayText.hudHeader(Aperture.openedAt(80), BodyData.DEFAULT);

        assertEquals("guzhenren.display.realm_title", key(header));
        assertEquals(2, header.getSiblings().size());
        assertEquals("guzhenren.display.aptitude_line", key(header.getSiblings().get(1)));
    }

    @Test
    void gradeAAptitudeShowsItsBaseFraction() {
        Component line = ModDisplayText.hudAptitude(Aperture.openedAt(80), BodyData.DEFAULT);

        assertEquals("guzhenren.display.aptitude_line", key(line));
        assertEquals(List.of("guzhenren.enum.aperture.talent.first", "guzhenren.display.base_round"),
                argumentKeys(line));
    }

    @Test
    void extremeAptitudeShowsTheConcreteBodyPhysique() {
        BodyData body = BodyData.DEFAULT.withPhysiques(java.util.Set.of(Physique.EXTREME))
                .withExtremePhysique(ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL);

        Component line = ModDisplayText.hudAptitude(Aperture.openedAt(100), body);

        assertEquals(List.of("guzhenren.enum.aperture.talent.extreme",
                        "guzhenren.enum.body.extreme_physique.great_strength_true_martial"),
                argumentKeys(line));
    }

    @Test
    void zombieDoesNotChangeTheExtremeAptitudeLine() {
        BodyData body = BodyData.DEFAULT.withPhysiques(java.util.Set.of(Physique.EXTREME, Physique.ZOMBIE))
                .withExtremePhysique(ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL);

        Component line = ModDisplayText.hudAptitude(Aperture.openedAt(100), body);

        assertEquals(List.of("guzhenren.enum.aperture.talent.extreme",
                        "guzhenren.enum.body.extreme_physique.great_strength_true_martial"),
                argumentKeys(line));
    }

    private static List<String> argumentKeys(Component component) {
        return Arrays.stream(((TranslatableContents) component.getContents()).getArgs())
                .filter(Component.class::isInstance)
                .map(Component.class::cast)
                .map(HudDisplayTest::key)
                .toList();
    }

    private static String key(Component component) {
        assertTrue(component.getContents() instanceof TranslatableContents);
        return ((TranslatableContents) component.getContents()).getKey();
    }
}
