package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stamina [耐力], the everyday pool that sprinting and jumping spend and that refills on its own.
 *
 * <p>Immutable record attachment keyed {@code stamina_data}; {@link StaminaService} is the only writer.
 * Two components: the current value and a bonus that a physique Gu may add. The cap is derived
 * ({@code baseMax + bonus}), never stored, which is why this record cannot clamp itself.
 *
 * <p>⚠ There is deliberately no {@code max} field: the cap has two sources ({@link com.unknown.guzhenren.custom.enums.aperture.Talent}
 * and {@link com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique}), so storing a third copy
 * would let them drift apart. ⚠ The compact ctor only floors both fields at 0 -- the upper clamp lives
 * in {@link StaminaService#setCurrent}, because only the service knows the derived cap. ⚠ A future
 * "+5000 max stamina" Gu stores a BONUS, not the cap, exactly as this record already does.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.StaminaService
 */
public record StaminaData(long currentStamina, long bonusStamina) {

    public static final long MORTAL_STAMINA = Talent.NONE.getStaminaBase();

    public static final StaminaData DEFAULT = new StaminaData(MORTAL_STAMINA, 0L);

    public static final Codec<StaminaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("current_stamina", MORTAL_STAMINA).forGetter(StaminaData::currentStamina),
            Codec.LONG.optionalFieldOf("bonus_stamina", 0L).forGetter(StaminaData::bonusStamina)
    ).apply(instance, StaminaData::new));

    public static final StreamCodec<ByteBuf, StaminaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, StaminaData::currentStamina,
            ByteBufCodecs.VAR_LONG, StaminaData::bonusStamina,
            StaminaData::new);

    public StaminaData {
        currentStamina = Math.max(0L, currentStamina);
        bonusStamina = Math.max(0L, bonusStamina);
    }

    public boolean isEmpty() {return currentStamina <= 0L;}
    public StaminaData withCurrentStamina(long v) {return new StaminaData(v, bonusStamina);}
    public StaminaData withBonusStamina(long v) {return new StaminaData(currentStamina, v);}
}
