package com.unknown.guzhenren.attachment.service.aperture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.BeastStrengthFamily;
import org.junit.jupiter.api.Test;

class ApertureTalentMarksTest {

    @Test
    void extremePhysiqueMarksSplitAcrossTalentPaths() {
        assertEquals(1_000L, ApertureService.talentMarksPerPath(ExtremePhysique.VERDANT_GREAT_SUN));
        assertEquals(500L, ApertureService.talentMarksPerPath(ExtremePhysique.NORTHERN_DARK_ICE_SOUL));
    }

    @Test
    void beastStrengthsAggregateByFamily() {
        StrengthData data = StrengthData.DEFAULT.with(BeastStrength.WHITE_BOAR)
                .with(BeastStrength.BLACK_BOAR).with(BeastStrength.BEAR);

        assertEquals(2, data.beastReadings().get(BeastStrengthFamily.BOAR));
        assertEquals(1, data.beastReadings().get(BeastStrengthFamily.BEAR));
    }
}
