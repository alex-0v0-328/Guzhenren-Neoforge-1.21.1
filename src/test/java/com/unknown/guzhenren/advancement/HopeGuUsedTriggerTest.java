package com.unknown.guzhenren.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HopeGuUsedTriggerTest {

    @Test
    @DisplayName("Instance codec roundtrips through an empty conditions map")
    void codecRoundtripsEmptyConditions() {
        HopeGuUsedTrigger.Instance instance = new HopeGuUsedTrigger.Instance(Optional.empty());
        var encoded = HopeGuUsedTrigger.Instance.CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow();
        assertTrue(encoded instanceof JsonObject json && json.size() == 0,
                "no business fields, the conditions map must serialize empty");
        HopeGuUsedTrigger.Instance back = HopeGuUsedTrigger.Instance.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(instance, back);
    }

    @Test
    @DisplayName("codec() serves the Instance codec, criterion() carries this trigger")
    void codecAndCriterionWiring() {
        HopeGuUsedTrigger trigger = new HopeGuUsedTrigger();
        assertSame(HopeGuUsedTrigger.Instance.CODEC, trigger.codec());
        var criterion = trigger.criterion();
        assertSame(trigger, criterion.trigger());
        assertEquals(new HopeGuUsedTrigger.Instance(Optional.empty()), criterion.triggerInstance());
    }
}
