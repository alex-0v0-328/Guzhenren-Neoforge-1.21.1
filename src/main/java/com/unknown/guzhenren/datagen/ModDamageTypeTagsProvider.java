package com.unknown.guzhenren.datagen;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  Running out of lifespan or soul is not something armour, a potion, or a shield can argue with,
//  so both damage types opt out of every mitigation vanilla offers.
//
//  Deliberately NOT in BYPASSES_INVULNERABILITY: creative players should stay unkillable, and
//  PlayerTickHandler already skips them anyway.
public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {

    public ModDamageTypeTagsProvider(PackOutput output,
                                    CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(ModDamageTypes.LIFESPAN_EXHAUSTED, ModDamageTypes.SOUL_COLLAPSE);
        tag(DamageTypeTags.BYPASSES_EFFECTS)
                .add(ModDamageTypes.LIFESPAN_EXHAUSTED, ModDamageTypes.SOUL_COLLAPSE);
        tag(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                .add(ModDamageTypes.LIFESPAN_EXHAUSTED, ModDamageTypes.SOUL_COLLAPSE);
        tag(DamageTypeTags.BYPASSES_RESISTANCE)
                .add(ModDamageTypes.LIFESPAN_EXHAUSTED, ModDamageTypes.SOUL_COLLAPSE);
        tag(DamageTypeTags.BYPASSES_SHIELD)
                .add(ModDamageTypes.LIFESPAN_EXHAUSTED, ModDamageTypes.SOUL_COLLAPSE);
        tag(DamageTypeTags.NO_KNOCKBACK)
                .add(ModDamageTypes.LIFESPAN_EXHAUSTED, ModDamageTypes.SOUL_COLLAPSE);
    }
}
