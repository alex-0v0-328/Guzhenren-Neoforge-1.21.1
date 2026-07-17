package com.unknown.guzhenren.datagen.damage;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  Lifespan, soul and Mind Ocean bypass armor, potions, shields -- nothing can argue with them.
//  NOT BYPASSES_INVULNERABILITY: creative stays unkillable, PlayerTickEvents skips it.
public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {

    public ModDamageTypeTagsProvider(PackOutput output,
                                     CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        unstoppable(DamageTypeTags.BYPASSES_ARMOR);
        unstoppable(DamageTypeTags.BYPASSES_EFFECTS);
        unstoppable(DamageTypeTags.BYPASSES_ENCHANTMENTS);
        unstoppable(DamageTypeTags.BYPASSES_RESISTANCE);
        unstoppable(DamageTypeTags.BYPASSES_SHIELD);
        unstoppable(DamageTypeTags.NO_KNOCKBACK);
    }

    private void unstoppable(TagKey<DamageType> tag) {
        tag(tag).add(ModDamageTypes.LIFESPAN_EXHAUSTED,
                ModDamageTypes.SOUL_COLLAPSE,
                ModDamageTypes.MIND_OCEAN_SHATTERED);
    }
}
