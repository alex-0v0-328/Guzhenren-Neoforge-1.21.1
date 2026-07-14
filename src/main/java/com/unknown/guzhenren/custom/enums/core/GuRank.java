package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.Arrays;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuRank implements StringRepresentable, EnumTranslatable {

    //  rankBase: maxEssence = baseEssence * stage.multiplier * rank.rankBase
    //  lifeForm: 该境界的仙凡归属; essenceColor: 该转真元的颜色 (深浅再按小境界算)
    NONE  (     0L, GuLifeForm.MORTAL,   GuEssenceColor.NONE),
    ONE   (     1L, GuLifeForm.MORTAL,   GuEssenceColor.GREEN_COPPER),
    TWO   (    10L, GuLifeForm.MORTAL,   GuEssenceColor.RED_STEEL),
    THREE (   100L, GuLifeForm.MORTAL,   GuEssenceColor.WHITE_SILVER),
    FOUR  ( 1_000L, GuLifeForm.MORTAL,   GuEssenceColor.YELLOW_GOLDEN),
    FIVE  (10_000L, GuLifeForm.MORTAL,   GuEssenceColor.PURPLE_CRYSTAL),

    //  仙人境的 rankBase 是「故意留空」而非未定: 蛊仙根本不走真元这套系统, 以后另起一套
    SIX   (     0L, GuLifeForm.IMMORTAL, GuEssenceColor.GREEN_GRAPE),
    SEVEN (     0L, GuLifeForm.IMMORTAL, GuEssenceColor.RED_DATE),
    EIGHT (     0L, GuLifeForm.IMMORTAL, GuEssenceColor.WHITE_LITCHI),
    NINE  (     0L, GuLifeForm.IMMORTAL, GuEssenceColor.YELLOW_APRICOT);

    public static final Codec<GuRank> CODEC = StringRepresentable.fromEnum(GuRank::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.rank.";

    //  可设置区间: 一转 ~ 五转. NONE 在下界外 —— 那是 awaken / reset 的事
    //  六~九转在上界外: rankBase 是故意留空的 0, 设进去真元上限直接归零
    public static final GuRank LOWEST = ONE;
    public static final GuRank HIGHEST = FIVE;

    private final long rankBase;
    private final GuLifeForm lifeForm;
    private final GuEssenceColor essenceColor;

    GuRank(long rankBase, GuLifeForm lifeForm, GuEssenceColor essenceColor) {
        this.rankBase = rankBase;
        this.lifeForm = lifeForm;
        this.essenceColor = essenceColor;
    }

    public long getRankBase() {return rankBase;}
    public GuLifeForm getLifeForm() {return lifeForm;}
    public GuEssenceColor getEssenceColor() {return essenceColor;}

    //  升降 d 档, 到边界即停 —— 五转再 up 不会溢出到六转, 那是另一套系统
    public GuRank shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}
    public static GuRank[] settable() {return Arrays.copyOfRange(values(), LOWEST.ordinal(), HIGHEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
