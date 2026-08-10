package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum LifeForm implements StringRepresentable, EnumTranslatable {

    ALIVE,
    DEAD,
    ZOMBIE,
    HALF_ZOMBIE;

    public static final Codec<LifeForm> CODEC = StringRepresentable.fromEnum(LifeForm::values);
    private static final String KEY_PREFIX = "guzhenren.enum.body.life_form.";

    public boolean isZombie() {return this == ZOMBIE;}
    public boolean isHalfZombie() {return this == HALF_ZOMBIE;}
    public boolean isAnyZombie() {return this == ZOMBIE || this == HALF_ZOMBIE;}

    public boolean spendsStamina() {return !isAnyZombie();}
    public boolean ages() {return !isAnyZombie();}
    public boolean breathes() {return !isAnyZombie();}
    public boolean getsHungry() {return !isAnyZombie();}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}
