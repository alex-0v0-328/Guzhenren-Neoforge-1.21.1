package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import java.util.Arrays;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Rank implements StringRepresentable, EnumTranslatable {

    //  rankBase: maxEssence = baseEssence * stage.multiplier * rank.rankBase
    //  maxHealth: the rank's HP cap, untouched by stage; lifeForm: mortal or immortal; essenceColor:
    //  the essence (真元) colour, which the stage then shades.
    NONE (      0L,  20, LifeForm.MORTAL,   EssenceColor.NONE),
    ONE  (      1L,  20, LifeForm.MORTAL,   EssenceColor.GREEN_COPPER),
    TWO  (     10L,  40, LifeForm.MORTAL,   EssenceColor.RED_STEEL),
    THREE(    100L,  60, LifeForm.MORTAL,   EssenceColor.WHITE_SILVER),
    FOUR (  1_000L,  80, LifeForm.MORTAL,   EssenceColor.YELLOW_GOLDEN),
    FIVE ( 10_000L, 100, LifeForm.MORTAL,   EssenceColor.PURPLE_CRYSTAL),

    //  The immortal ranks leave rankBase and maxHealth at 0 ON PURPOSE, not undecided: a Gu Immortal
    //  does not run on essence, and gets its own system later.
    //  ⚠ A maxHealth of 0 reads "leave HP alone", not "zero HP"  HealthService.
    SIX  (      0L,   0, LifeForm.IMMORTAL, EssenceColor.GREEN_GRAPE),
    SEVEN(      0L,   0, LifeForm.IMMORTAL, EssenceColor.RED_DATE),
    EIGHT(      0L,   0, LifeForm.IMMORTAL, EssenceColor.WHITE_LITCHI),
    NINE (      0L,   0, LifeForm.IMMORTAL, EssenceColor.YELLOW_APRICOT);

    public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.rank.";

    //  Settable range: Rank I..V. NONE sits below it -- awaken / reset own that.
    //  ⚠ Rank VI..IX sit above it: their rankBase is a deliberate 0, so setting one zeroes the cap.
    public static final Rank LOWEST = ONE;
    public static final Rank HIGHEST = FIVE;

    private final long rankBase;
    private final int maxHealth;
    private final LifeForm lifeForm;
    private final EssenceColor essenceColor;

    Rank(long rankBase, int maxHealth, LifeForm lifeForm, EssenceColor essenceColor) {
        this.rankBase = rankBase;
        this.maxHealth = maxHealth;
        this.lifeForm = lifeForm;
        this.essenceColor = essenceColor;
    }

    public long getRankBase() {return rankBase;}
    public int getMaxHealth() {return maxHealth;}
    public LifeForm getLifeForm() {return lifeForm;}
    public EssenceColor getEssenceColor() {return essenceColor;}

    //  Shift d ranks, stopping at the edge -- Rank V never spills into VI, which is another system.
    public Rank shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}
    public static Rank[] settable() {return Arrays.copyOfRange(values(), LOWEST.ordinal(), HIGHEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
