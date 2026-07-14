package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.util.GuTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  真元的颜色, 每一转一色 (映射见 GuRank.getEssenceColor)
//  凡人境同转内按小境界由浅变深, 基色只存一个, 深浅由 shade(stage) 算; 仙人境是固定色
//  ⚠ 目前无使用处: HUD 的真元条用固定天蓝色, 见 CLAUDE.md "HUD". 留给粒子/物品/界面, 别当死代码删了
public enum GuEssenceColor implements StringRepresentable, GuTranslatable {

    //  凡人 / 未开窍: 灰
    NONE          (0xFF808080, false),

    //  一转 ~ 五转 (凡人境): 括号里是中阶色, 初阶提亮 / 巅峰压暗
    GREEN_COPPER  (0xFF3EC98A),
    RED_STEEL     (0xFFD9503F),
    WHITE_SILVER  (0xFFCCCCCC),
    YELLOW_GOLDEN (0xFFE8BE43),
    PURPLE_CRYSTAL(0xFFA855D4),

    //  六转 ~ 九转 (仙人境): 固定色
    GREEN_GRAPE   (0xFF9ACD32, false),
    RED_DATE      (0xFF8B2500, false),
    WHITE_LITCHI  (0xFFF5F0E1, false),
    YELLOW_APRICOT(0xFFFBCEB1, false);

    public static final Codec<GuEssenceColor> CODEC = StringRepresentable.fromEnum(GuEssenceColor::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.essence_color.";

    //  亮度系数, 下标与 GuStage.ordinal() 对齐: 无 初 中 高 巅. 想整体调深浅改这一行
    private static final float[] STAGE_BRIGHTNESS = {1.00F, 1.20F, 1.00F, 0.72F, 0.45F};

    private final int baseColor;
    private final boolean shadeByStage;

    GuEssenceColor(int baseColor) {
        this(baseColor, true);
    }

    GuEssenceColor(int baseColor, boolean shadeByStage) {
        this.baseColor = baseColor;
        this.shadeByStage = shadeByStage;
    }

    //  基色 (ARGB); 会变深浅的转这是中阶色, 不变的转这就是最终色
    public int getBaseColor() {return baseColor;}

    //  该小境界下的实际颜色 (ARGB); alpha 不动, 只缩放 RGB
    public int shade(@NotNull GuStage stage) {
        if (!shadeByStage) return baseColor;

        float factor = STAGE_BRIGHTNESS[stage.ordinal()];
        int alpha = baseColor >>> 24;
        int red = scale((baseColor >> 16) & 0xFF, factor);
        int green = scale((baseColor >> 8) & 0xFF, factor);
        int blue = scale(baseColor & 0xFF, factor);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int scale(int channel, float factor) {
        return Math.clamp(Math.round(channel * factor), 0, 255);
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
