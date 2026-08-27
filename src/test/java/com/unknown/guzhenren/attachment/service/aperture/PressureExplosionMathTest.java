package com.unknown.guzhenren.attachment.service.aperture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PressureExplosionMathTest {

    @Test
    @DisplayName("column jitter stays within [0, 0.1) for every column and seed")
    void jitterRange() {
        for (long seed = 0L; seed < 64L; seed++) {
            for (int bx = -96; bx <= 96; bx += 7) {
                for (int bz = -96; bz <= 96; bz += 11) {
                    double jitter = PressureExplosionTask.columnJitter(bx, bz, seed);
                    assertTrue(jitter >= 0.0D, "jitter below floor at " + bx + "," + bz);
                    assertTrue(jitter < 0.1D, "jitter above ceiling at " + bx + "," + bz);
                }
            }
        }
    }

    @Test
    @DisplayName("the same column and seed always jitter identically")
    void jitterDeterministic() {
        for (long seed = 0L; seed < 32L; seed++) {
            assertEquals(PressureExplosionTask.columnJitter(3, -7, seed),
                    PressureExplosionTask.columnJitter(3, -7, seed));
            assertEquals(PressureExplosionTask.columnJitter(-40, 55, seed),
                    PressureExplosionTask.columnJitter(-40, 55, seed));
        }
    }

    @Test
    @DisplayName("different seeds actually reshape the crater wall")
    void jitterVariesBySeed() {
        int different = 0;
        for (int i = 0; i < 1000; i++) {
            if (PressureExplosionTask.columnJitter(11, -23, i) != PressureExplosionTask.columnJitter(11, -23, i + 1)) {
                different++;
            }
        }
        assertTrue(different > 900, "seeds collapse to the same wall, only " + different + " differ");
    }

    @Test
    @DisplayName("the noisy radius keeps the sphere between 0.9r and r")
    void noisyRadiusBand() {
        int radius = 80;
        for (long seed = 0L; seed < 64L; seed++) {
            for (int bx = -96; bx <= 96; bx += 7) {
                for (int bz = -96; bz <= 96; bz += 11) {
                    double rr = PressureExplosionTask.columnRadius(radius, bx, bz, seed);
                    assertTrue(rr >= radius * 0.9D, "radius under floor at " + bx + "," + bz);
                    assertTrue(rr < radius, "radius at or above full r at " + bx + "," + bz);
                }
            }
        }
    }

    @Test
    @DisplayName("the floor sits exactly one block under the noisy sphere bottom of its column")
    void floorSitsUnderSphere() {
        double centerY = 64.0D;
        double columnRadius = 80.0D;
        for (int step = 0; step < 200; step++) {
            double horizontalSquared = (step / 200.0D) * (columnRadius * columnRadius * 0.9D);
            int floorY = PressureExplosionTask.columnFloorY(centerY, columnRadius, horizontalSquared);
            double depthSquared = columnRadius * columnRadius - horizontalSquared;

            double dyFloor = floorY + 0.5D - centerY;
            assertTrue(dyFloor * dyFloor + horizontalSquared > columnRadius * columnRadius,
                    "floor block is inside the sphere at step " + step);

            double dyAbove = floorY + 1.5D - centerY;
            assertTrue(dyAbove * dyAbove + horizontalSquared <= columnRadius * columnRadius,
                    "block above the floor fell outside the sphere at step " + step);
            assertTrue(depthSquared > 0.0D);
        }
    }

    @Test
    @DisplayName("the (dx,dz) and (-dx,-dz) columns do not collapse into one jitter value")
    void jitterIsNotMirrored() {
        int mirrored = 0;
        for (long seed = 0L; seed < 256L; seed++) {
            if (PressureExplosionTask.columnJitter(30, 12, seed) == PressureExplosionTask.columnJitter(-30, -12, seed)) {
                mirrored++;
            }
        }
        assertTrue(mirrored < 32, "mirrored columns jitter identically in " + mirrored + " of 256 seeds");
    }

    @Test
    @DisplayName("ice tiers go deeper = harder to melt: blue under -32, packed under 0, plain ice above")
    void iceTiers() {
        assertEquals(2, PressureExplosionTask.iceTier(-33));
        assertEquals(1, PressureExplosionTask.iceTier(-32));
        assertEquals(1, PressureExplosionTask.iceTier(-1));
        assertEquals(0, PressureExplosionTask.iceTier(0));
        assertEquals(0, PressureExplosionTask.iceTier(200));
    }

    @Test
    @DisplayName("gold tiers go low to high: deepslate ore, gold ore, gold block")
    void goldTiers() {
        assertEquals(0, PressureExplosionTask.goldTier(-1));
        assertEquals(1, PressureExplosionTask.goldTier(0));
        assertEquals(1, PressureExplosionTask.goldTier(31));
        assertEquals(2, PressureExplosionTask.goldTier(32));
        assertEquals(2, PressureExplosionTask.goldTier(200));
    }

    @Test
    @DisplayName("roughly a quarter of the crater floor columns are lava")
    void lavaShare() {
        int lava = 0;
        int columns = 10_000;
        for (int i = 0; i < columns; i++) {
            if (PressureExplosionTask.isLavaColumn(i % 100, i / 100, 42L)) lava++;
        }
        assertTrue(lava > columns / 5 && lava < columns * 3 / 10,
                "lava share out of band: " + lava + " of " + columns);
    }

    @Test
    @DisplayName("roughly one column in eight carries scattered gold")
    void goldShare() {
        int gold = 0;
        int columns = 10_000;
        for (int i = 0; i < columns; i++) {
            if (PressureExplosionTask.isGoldColumn(i % 100, i / 100, 42L)) gold++;
        }
        assertTrue(gold > columns / 10 && gold < columns * 3 / 20,
                "gold share out of band: " + gold + " of " + columns);
    }

    @Test
    @DisplayName("the ring mixes snow blocks and powder snow roughly half and half")
    void powderSnowShare() {
        int powder = 0;
        int columns = 10_000;
        for (int i = 0; i < columns; i++) {
            if (PressureExplosionTask.isPowderSnowColumn(i % 100, i / 100, 42L)) powder++;
        }
        assertTrue(powder > columns * 9 / 20 && powder < columns * 11 / 20,
                "powder snow share out of band: " + powder + " of " + columns);
    }

    @Test
    @DisplayName("the ring's outer edge stays between radius + 8 and radius + 16")
    void ringOuterBand() {
        int radius = 160;
        for (long seed = 0L; seed < 32L; seed++) {
            for (int bx = -200; bx <= 200; bx += 13) {
                for (int bz = -200; bz <= 200; bz += 17) {
                    double outer = PressureExplosionTask.ringOuterRadius(radius, bx, bz, seed);
                    assertTrue(outer >= radius + 8.0D, "ring too thin at " + bx + "," + bz);
                    assertTrue(outer < radius + 16.0D, "ring too wide at " + bx + "," + bz);
                }
            }
        }
    }
}
