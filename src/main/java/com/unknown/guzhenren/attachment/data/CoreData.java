package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.core.GuExtremePhysique;
import com.unknown.guzhenren.custom.enums.core.GuLifeForm;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.custom.enums.core.GuRank;
import com.unknown.guzhenren.custom.enums.core.GuStage;
import com.unknown.guzhenren.custom.enums.core.GuTalent;
import com.unknown.guzhenren.util.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The core system: what a cultivator *is*. Talent and life form derived, never stored.
public record CoreData(
        GuRank rank,
        GuStage stage,
        int baseEssence,
        GuExtremePhysique extremePhysique,
        GuLifeState lifeState
) {

    //  资质基数: 0 unawakened, 20..100 after. GuTalent's bands cover exactly 20..100.
    public static final int UNAWAKENED_BASE = 0;
    public static final int MIN_BASE = 20;
    public static final int MAX_BASE = 100;

    public static final CoreData DEFAULT =
            new CoreData(GuRank.NONE, GuStage.NONE, UNAWAKENED_BASE, GuExtremePhysique.NONE, GuLifeState.ALIVE);

    //  All optional: a field added later must not invalidate old saves.
    public static final Codec<CoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GuRank.CODEC.optionalFieldOf("rank", GuRank.NONE).forGetter(CoreData::rank),
            GuStage.CODEC.optionalFieldOf("stage", GuStage.NONE).forGetter(CoreData::stage),
            Codec.INT.optionalFieldOf("base_essence", UNAWAKENED_BASE).forGetter(CoreData::baseEssence),
            GuExtremePhysique.CODEC.optionalFieldOf("extreme_physique", GuExtremePhysique.NONE)
                    .forGetter(CoreData::extremePhysique),
            GuLifeState.CODEC.optionalFieldOf("life_state", GuLifeState.ALIVE).forGetter(CoreData::lifeState)
    ).apply(instance, CoreData::new));

    public static final StreamCodec<ByteBuf, CoreData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(GuRank.class), CoreData::rank,
            ModStreamCodecs.ofEnum(GuStage.class), CoreData::stage,
            ByteBufCodecs.VAR_INT, CoreData::baseEssence,
            ModStreamCodecs.ofEnum(GuExtremePhysique.class), CoreData::extremePhysique,
            ModStreamCodecs.ofEnum(GuLifeState.class), CoreData::lifeState,
            CoreData::new);

    //  ⚠ 1..19 is a hole, not a value: no talent tier there, but a live essence cap. Snap out of it.
    public CoreData {
        baseEssence = baseEssence <= UNAWAKENED_BASE
                ? UNAWAKENED_BASE
                : Math.clamp(baseEssence, MIN_BASE, MAX_BASE);
    }

    //  ---- derived, never stored; see CLAUDE.md ----
    public GuTalent talent() {return GuTalent.fromPercent(baseEssence);}
    public GuLifeForm lifeForm() {return rank.getLifeForm();}
    public boolean isAwakened() {return baseEssence >= MIN_BASE;}
    public boolean isExtreme() {return talent() == GuTalent.EXTREME;}

    //  ---- withers ----
    public CoreData withRank(GuRank v) {return new CoreData(v, stage, baseEssence, extremePhysique, lifeState);}
    public CoreData withStage(GuStage v) {return new CoreData(rank, v, baseEssence, extremePhysique, lifeState);}
    public CoreData withBaseEssence(int v) {return new CoreData(rank, stage, v, extremePhysique, lifeState);}
    public CoreData withLifeState(GuLifeState v) {return new CoreData(rank, stage, baseEssence, extremePhysique, v);}
    public CoreData withExtremePhysique(GuExtremePhysique v) {
        return new CoreData(rank, stage, baseEssence, v, lifeState);
    }
}
