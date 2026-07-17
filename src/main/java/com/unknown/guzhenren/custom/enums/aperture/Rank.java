package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import java.util.Arrays;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Rank implements StringRepresentable, EnumTranslatable {

    //  rankBase: maxEssence = baseEssence * stage.multiplier * rank.rankBase
    //  lifeForm: 该境界的仙凡归属; essenceColor: 该转真元的颜色 (深浅再按小境界算)
    NONE(0L, LifeForm.MORTAL, EssenceColor.NONE),
    ONE(1L, LifeForm.MORTAL, EssenceColor.GREEN_COPPER),
    TWO(10L, LifeForm.MORTAL, EssenceColor.RED_STEEL),
    THREE(100L, LifeForm.MORTAL, EssenceColor.WHITE_SILVER),
    FOUR(1_000L, LifeForm.MORTAL, EssenceColor.YELLOW_GOLDEN),
    FIVE(10_000L, LifeForm.MORTAL, EssenceColor.PURPLE_CRYSTAL),

    //  仙人境的 rankBase 是「故意留空」而非未定: 蛊仙根本不走真元这套系统, 以后另起一套
    SIX(0L, LifeForm.IMMORTAL, EssenceColor.GREEN_GRAPE),
    SEVEN(0L, LifeForm.IMMORTAL, EssenceColor.RED_DATE),
    EIGHT(0L, LifeForm.IMMORTAL, EssenceColor.WHITE_LITCHI),
    NINE(0L, LifeForm.IMMORTAL, EssenceColor.YELLOW_APRICOT);

    public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.rank.";

    //  可设置区间: 一转 ~ 五转. NONE 在下界外 —— 那是 awaken / reset 的事
    //  六~九转在上界外: rankBase 是故意留空的 0, 设进去真元上限直接归零
    public static final Rank LOWEST = ONE;
    public static final Rank HIGHEST = FIVE;

    private final long rankBase;
    private final LifeForm lifeForm;
    private final EssenceColor essenceColor;

    Rank(long rankBase, LifeForm lifeForm, EssenceColor essenceColor) {
        this.rankBase = rankBase;
        this.lifeForm = lifeForm;
        this.essenceColor = essenceColor;
    }

    public long getRankBase() {return rankBase;}
    public LifeForm getLifeForm() {return lifeForm;}
    public EssenceColor getEssenceColor() {return essenceColor;}

    //  升降 d 档, 到边界即停 —— 五转再 up 不会溢出到六转, 那是另一套系统
    public Rank shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}
    public static Rank[] settable() {return Arrays.copyOfRange(values(), LOWEST.ordinal(), HIGHEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
