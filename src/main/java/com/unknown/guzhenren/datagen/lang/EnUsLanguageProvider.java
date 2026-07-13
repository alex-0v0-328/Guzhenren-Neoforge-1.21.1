package com.unknown.guzhenren.datagen.lang;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.core.GuEssenceColor;
import com.unknown.guzhenren.custom.enums.core.GuExtremePhysique;
import com.unknown.guzhenren.custom.enums.core.GuLifeForm;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.custom.enums.core.GuRank;
import com.unknown.guzhenren.custom.enums.core.GuSoulTier;
import com.unknown.guzhenren.custom.enums.core.GuStage;
import com.unknown.guzhenren.custom.enums.core.GuTalent;
import com.unknown.guzhenren.custom.enums.path.Path;
import com.unknown.guzhenren.custom.enums.path.PathAttainment;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class EnUsLanguageProvider extends LanguageProvider {
    public EnUsLanguageProvider(PackOutput output) {
        super(output, Guzhenren.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addEnumKeys();
    }

    //region ENUM
    private void addEnumKeys() {
        addRank();
        addStage();
        addTalent();
        addLifeForm();
        addLifeState();
        addEssenceColor();
        addTenExtreme();
        addPath();
        addAttainment();
        addSoulTier();
    }

    //  魂魄境界: 由魂魄值反查 (魂魄值 = 人数 × 100), 不单独存
    private void addSoulTier() {
        add(GuSoulTier.ONE.getTranslationKey(), "One-Person Soul");
        add(GuSoulTier.TEN.getTranslationKey(), "Ten-Person Soul");
        add(GuSoulTier.HUNDRED.getTranslationKey(), "Hundred-Person Soul");
        add(GuSoulTier.THOUSAND.getTranslationKey(), "Thousand-Person Soul");
        add(GuSoulTier.TEN_THOUSAND.getTranslationKey(), "Ten-Thousand-Person Soul");
        add(GuSoulTier.HUNDRED_THOUSAND.getTranslationKey(), "Hundred-Thousand-Person Soul");
        add(GuSoulTier.MILLION.getTranslationKey(), "Million-Person Soul");
        add(GuSoulTier.TEN_MILLION.getTranslationKey(), "Ten-Million-Person Soul");
        add(GuSoulTier.HUNDRED_MILLION.getTranslationKey(), "Hundred-Million-Person Soul");
    }

    //  真元色, 每转一色; 同转内的深浅由 GuEssenceColor.shade 按小境界算, 不另出文案
    private void addEssenceColor() {
        add(GuEssenceColor.NONE.getTranslationKey(), "None");
        add(GuEssenceColor.GREEN_COPPER.getTranslationKey(), "Green Copper");
        add(GuEssenceColor.RED_STEEL.getTranslationKey(), "Red Steel");
        add(GuEssenceColor.WHITE_SILVER.getTranslationKey(), "White Silver");
        add(GuEssenceColor.YELLOW_GOLDEN.getTranslationKey(), "Yellow Golden");
        add(GuEssenceColor.PURPLE_CRYSTAL.getTranslationKey(), "Purple Crystal");
        add(GuEssenceColor.GREEN_GRAPE.getTranslationKey(), "Green Grape");
        add(GuEssenceColor.RED_DATE.getTranslationKey(), "Red Date");
        add(GuEssenceColor.WHITE_LITCHI.getTranslationKey(), "White Litchi");
        add(GuEssenceColor.YELLOW_APRICOT.getTranslationKey(), "Yellow Apricot");
    }

    private void addRank() {
        add(GuRank.NONE.getTranslationKey(), "Mortal");
        add(GuRank.ONE.getTranslationKey(), "Rank I");
        add(GuRank.TWO.getTranslationKey(), "Rank II");
        add(GuRank.THREE.getTranslationKey(), "Rank III");
        add(GuRank.FOUR.getTranslationKey(), "Rank IV");
        add(GuRank.FIVE.getTranslationKey(), "Rank V");
        add(GuRank.SIX.getTranslationKey(), "Rank VI");
        add(GuRank.SEVEN.getTranslationKey(), "Rank VII");
        add(GuRank.EIGHT.getTranslationKey(), "Rank VIII");
        add(GuRank.NINE.getTranslationKey(), "Rank IX");
    }

    private void addStage() {
        //  故意留空: 未开窍时不显示
        add(GuStage.NONE.getTranslationKey(), "");
        add(GuStage.INIT.getTranslationKey(), "Initial");
        add(GuStage.MIDDLE.getTranslationKey(), "Middle");
        add(GuStage.UPPER.getTranslationKey(), "Upper");
        add(GuStage.PEAK.getTranslationKey(), "Peak");
    }

    private void addTalent() {
        add(GuTalent.EXTREME.getTranslationKey(), "Ten Extremes");
        add(GuTalent.FIRST.getTranslationKey(), "Tier I");
        add(GuTalent.SECOND.getTranslationKey(), "Tier II");
        add(GuTalent.THIRD.getTranslationKey(), "Tier III");
        add(GuTalent.FOURTH.getTranslationKey(), "Tier IV");
        add(GuTalent.NONE.getTranslationKey(), "Unawakened");
    }

    private void addLifeForm() {
        add(GuLifeForm.MORTAL.getTranslationKey(), "Mortal");
        add(GuLifeForm.IMMORTAL.getTranslationKey(), "Immortal");
    }

    private void addLifeState() {
        add(GuLifeState.ALIVE.getTranslationKey(), "Alive");
        add(GuLifeState.ZOMBIE.getTranslationKey(), "Zombified");
        add(GuLifeState.DEAD.getTranslationKey(), "Dead");
    }

    private void addTenExtreme() {
        //  故意留空: 非十绝体玩家不显示
        add(GuExtremePhysique.NONE.getTranslationKey(), "");
        add(GuExtremePhysique.VERDANT_GREAT_SUN.getTranslationKey(), "Verdant Great Sun");
        add(GuExtremePhysique.DESOLATE_ANCIENT_MOON.getTranslationKey(), "Desolate Ancient Moon");
        add(GuExtremePhysique.NORTHERN_DARK_ICE_SOUL.getTranslationKey(), "Northern Dark Ice Soul");
        add(GuExtremePhysique.BOUNDLESS_FOREST_SAMSARA.getTranslationKey(), "Boundless Forest Samsara");
        add(GuExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE.getTranslationKey(),
                "Blazing Glory Lightning Brilliance");
        add(GuExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE.getTranslationKey(), "Myriad Gold Wondrous Essence");
        add(GuExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL.getTranslationKey(), "Great Strength True Martial");
        add(GuExtremePhysique.CAREFREE_WISDOM_HEART.getTranslationKey(), "Carefree Wisdom Heart");
        add(GuExtremePhysique.PROFOUND_EARTH_ORIGIN.getTranslationKey(), "Profound Earth Origin");
        add(GuExtremePhysique.UNIVERSE_GREAT_DERIVATION.getTranslationKey(), "Universe Great Derivation");
        add(GuExtremePhysique.PURE_DREAM_REALITY_SEEKER.getTranslationKey(), "Pure Dream Reality Seeker");
    }

    private void addPath() {
        add(Path.HEAVEN.getTranslationKey(), "Heaven Path");
        add(Path.RULE.getTranslationKey(), "Rule Path");
        add(Path.SPACE.getTranslationKey(), "Space Path");
        add(Path.TIME.getTranslationKey(), "Time Path");
        add(Path.HUMAN.getTranslationKey(), "Human Path");
        add(Path.METAL.getTranslationKey(), "Metal Path");
        add(Path.WOOD.getTranslationKey(), "Wood Path");
        add(Path.WATER.getTranslationKey(), "Water Path");
        add(Path.FIRE.getTranslationKey(), "Fire Path");
        add(Path.EARTH.getTranslationKey(), "Earth Path");
        add(Path.ICE_SNOW.getTranslationKey(), "Ice-Snow Path");
        add(Path.LIGHTNING.getTranslationKey(), "Lightning Path");
        add(Path.QI.getTranslationKey(), "Qi Path");
        add(Path.SOUND.getTranslationKey(), "Sound Path");
        add(Path.LIGHT.getTranslationKey(), "Light Path");
        add(Path.DARK.getTranslationKey(), "Dark Path");
        add(Path.STRENGTH.getTranslationKey(), "Strength Path");
        add(Path.DREAM.getTranslationKey(), "Dream Path");
        add(Path.REFINEMENT.getTranslationKey(), "Refinement Path");
        add(Path.WISDOM.getTranslationKey(), "Wisdom Path");
        add(Path.THEFT.getTranslationKey(), "Theft Path");
        add(Path.LUCK.getTranslationKey(), "Luck Path");
        add(Path.KILLING.getTranslationKey(), "Killing Path");
        add(Path.BLOOD.getTranslationKey(), "Blood Path");
        add(Path.SOUL.getTranslationKey(), "Soul Path");
        add(Path.ENSLAVEMENT.getTranslationKey(), "Enslavement Path");
        add(Path.FOOD.getTranslationKey(), "Food Path");
        add(Path.FORMATION.getTranslationKey(), "Formation Path");
        add(Path.PAINTING.getTranslationKey(), "Painting Path");
        add(Path.TRANSFORMATION.getTranslationKey(), "Transformation Path");
    }

    private void addAttainment() {
        add(PathAttainment.NONE.getTranslationKey(), "None");
        add(PathAttainment.ORDINARY.getTranslationKey(), "Ordinary");
        add(PathAttainment.QUASI_MASTER.getTranslationKey(), "Quasi-Master");
        add(PathAttainment.MASTER.getTranslationKey(), "Master");
        add(PathAttainment.QUASI_GRANDMASTER.getTranslationKey(), "Quasi-Grandmaster");
        add(PathAttainment.GRANDMASTER.getTranslationKey(), "Grandmaster");
        add(PathAttainment.QUASI_GREAT_GRANDMASTER.getTranslationKey(), "Quasi-Great Grandmaster");
        add(PathAttainment.GREAT_GRANDMASTER.getTranslationKey(), "Great Grandmaster");
        add(PathAttainment.QUASI_SUPREME_GRANDMASTER.getTranslationKey(), "Quasi-Supreme Grandmaster");
        add(PathAttainment.SUPREME_GRANDMASTER.getTranslationKey(), "Supreme Grandmaster");
    }
    //endregion
}
