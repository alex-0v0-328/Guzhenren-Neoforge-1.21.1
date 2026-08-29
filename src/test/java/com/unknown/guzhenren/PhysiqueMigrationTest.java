package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.body.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.body.Physique;
import org.junit.jupiter.api.Test;

class PhysiqueMigrationTest {

    @Test
    void legacyLifeFormsMapToPhysiques() {
        assertEquals(SetHolder.ZOMBIE, decodeBody("zombie").physiques());
        assertEquals(SetHolder.HALF_ZOMBIE, decodeBody("half_zombie").physiques());
        assertTrue(decodeBody("alive").physiques().isEmpty());
        assertTrue(decodeBody("dead").physiques().isEmpty());
    }

    @Test
    void bodyCodecWritesOnlyTheNewFields() {
        JsonObject encoded = BodyData.CODEC.encodeStart(JsonOps.INSTANCE, new BodyData(
                SetHolder.EXTREME, ExtremePhysique.VERDANT_GREAT_SUN, BodyData.DEFAULT.race(),
                BodyData.DEFAULT.ageParts(), BodyData.DEFAULT.lifespanParts(), BodyData.UNTRACKED, 0L,
                BodyData.UNTRACKED, BodyData.NO_ZOMBIE_TIER, BodyData.UNTRACKED)).getOrThrow().getAsJsonObject();

        assertTrue(encoded.has("physiques"));
        assertTrue(encoded.has("extreme_physique"));
        assertFalse(encoded.has("life_form"));
    }

    @Test
    void legacyApertureCarriesExtremeOnlyUntilJoinMigration() {
        JsonObject legacy = JsonParser.parseString("""
                {"base_essence":100,"extreme_physique":"verdant_great_sun","zombie_opened":true}
                """).getAsJsonObject();

        Aperture decoded = Aperture.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow();
        assertEquals(100, decoded.baseEssence());
        assertEquals(ExtremePhysique.VERDANT_GREAT_SUN, decoded.legacyExtremePhysique());

        JsonObject encoded = Aperture.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow().getAsJsonObject();
        assertFalse(encoded.has("extreme_physique"));
        assertFalse(encoded.has("zombie_opened"));
        assertNull(decoded.clearLegacyExtremePhysique().legacyExtremePhysique());
    }

    private static BodyData decodeBody(String lifeForm) {
        JsonObject legacy = new JsonObject();
        legacy.addProperty("life_form", lifeForm);
        return BodyData.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow();
    }

    private static final class SetHolder {
        private static final java.util.Set<Physique> ZOMBIE = java.util.Set.of(Physique.ZOMBIE);
        private static final java.util.Set<Physique> HALF_ZOMBIE = java.util.Set.of(Physique.HALF_ZOMBIE);
        private static final java.util.Set<Physique> EXTREME = java.util.Set.of(Physique.EXTREME);
    }
}
