package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  智道三态: 念 -> 意 -> 情, 逐级凝练 (转化尚未实现, 见 CLAUDE.md "Pending")
//  括号里是脑海的初始容量; 装不下就是脑海炸裂而亡, 见 MindPool
public enum GuWisdomType implements StringRepresentable, EnumTranslatable {

    THOUGHTS(30_000L),
    WILLS   (     5L),
    EMOTIONS(     2L);

    public static final Codec<GuWisdomType> CODEC = StringRepresentable.fromEnum(GuWisdomType::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.type.";

    //  脑海初始容量. 上限日后可被蛊 / 丹药抬高, 所以要存, 不能派生
    private final long defaultCapacity;

    GuWisdomType(long defaultCapacity) {
        this.defaultCapacity = defaultCapacity;
    }

    public long getDefaultCapacity() {return defaultCapacity;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
