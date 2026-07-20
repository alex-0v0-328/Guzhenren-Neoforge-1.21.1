package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import java.util.Arrays;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Rank implements StringRepresentable, EnumTranslatable {

    //  rankBase: maxEssence = baseEssence * stage.multiplier * rank.rankBase
    //  maxHealth: 该转的生命上限, 小境界不参与; lifeForm: 仙凡归属; essenceColor: 真元颜色 (深浅再按小境界算)
    NONE (      0L,  20, LifeForm.MORTAL,   EssenceColor.NONE),
    ONE  (      1L,  20, LifeForm.MORTAL,   EssenceColor.GREEN_COPPER),
    TWO  (     10L,  40, LifeForm.MORTAL,   EssenceColor.RED_STEEL),
    THREE(    100L,  60, LifeForm.MORTAL,   EssenceColor.WHITE_SILVER),
    FOUR (  1_000L,  80, LifeForm.MORTAL,   EssenceColor.YELLOW_GOLDEN),
    FIVE ( 10_000L, 100, LifeForm.MORTAL,   EssenceColor.PURPLE_CRYSTAL),

    //  仙人境的 rankBase 与 maxHealth 都是「故意留空」而非未定: 蛊仙不走真元这套, 以后另起一套
    //  ⚠ maxHealth 的 0 读作「不动血量」, 不是 0 点血 —— 见 HealthService
    SIX  (      0L,   0, LifeForm.IMMORTAL, EssenceColor.GREEN_GRAPE),
    SEVEN(      0L,   0, LifeForm.IMMORTAL, EssenceColor.RED_DATE),
    EIGHT(      0L,   0, LifeForm.IMMORTAL, EssenceColor.WHITE_LITCHI),
    NINE (      0L,   0, LifeForm.IMMORTAL, EssenceColor.YELLOW_APRICOT);

    public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.rank.";

    //  可设置区间: 一转 ~ 五转. NONE 在下界外 —— 那是 awaken / reset 的事
    //  六~九转在上界外: rankBase 是故意留空的 0, 设进去真元上限直接归零
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

    //  升降 d 档, 到边界即停 —— 五转再 up 不会溢出到六转, 那是另一套系统
    public Rank shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}
    public static Rank[] settable() {return Arrays.copyOfRange(values(), LOWEST.ordinal(), HIGHEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
