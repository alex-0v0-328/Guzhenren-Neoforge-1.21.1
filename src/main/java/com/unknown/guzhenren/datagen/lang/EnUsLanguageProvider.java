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
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class EnUsLanguageProvider extends LanguageProvider {
    public EnUsLanguageProvider(PackOutput output) {
        super(output, Guzhenren.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addEnumKeys();
        addCommandKeys();
        addDeathMessages();
    }

    //region COMMAND
    private void addCommandKeys() {
        add("guzhenren.command.updated", "Updated %s player(s)");
        add("guzhenren.command.unknown_value", "Unknown value: %s");

        add("guzhenren.command.info.header", "==== %s ====");
        add("guzhenren.command.info.realm", "Realm: %s %s (%s)");
        add("guzhenren.command.info.talent", "Aptitude: %s (base %s)");
        add("guzhenren.command.info.physique", "Physique: %s");
        add("guzhenren.command.info.essence", "Essence: %s / %s");
        add("guzhenren.command.info.lifespan", "Lifespan: age %s, %s remaining");
        add("guzhenren.command.info.soul", "Soul: %s  %s / %s");
        add("guzhenren.command.info.paths", "Paths:");
        add("guzhenren.command.info.no_path", "Paths: none");
        add("guzhenren.command.info.path_entry", "  %s  %s  Marks %s");
    }
    //endregion

    //region DEATH
    //  键名由 DamageType 的 msgId 决定: msgId "guzhenren.xxx" -> "death.attack.guzhenren.xxx"
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted", "%1$s ran out of lifespan");
        add("death.attack.guzhenren.soul_collapse", "%1$s suffered soul collapse");
    }
    //endregion

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
        add(GuPath.HEAVEN.getTranslationKey(), "Heaven Path");
        add(GuPath.RULE.getTranslationKey(), "Rule Path");
        add(GuPath.SPACE.getTranslationKey(), "Space Path");
        add(GuPath.TIME.getTranslationKey(), "Time Path");
        add(GuPath.HUMAN.getTranslationKey(), "Human Path");
        add(GuPath.METAL.getTranslationKey(), "Metal Path");
        add(GuPath.WOOD.getTranslationKey(), "Wood Path");
        add(GuPath.WATER.getTranslationKey(), "Water Path");
        add(GuPath.FIRE.getTranslationKey(), "Fire Path");
        add(GuPath.EARTH.getTranslationKey(), "Earth Path");
        add(GuPath.ICE_SNOW.getTranslationKey(), "Ice-Snow Path");
        add(GuPath.LIGHTNING.getTranslationKey(), "Lightning Path");
        add(GuPath.QI.getTranslationKey(), "Qi Path");
        add(GuPath.SOUND.getTranslationKey(), "Sound Path");
        add(GuPath.LIGHT.getTranslationKey(), "Light Path");
        add(GuPath.DARK.getTranslationKey(), "Dark Path");
        add(GuPath.STRENGTH.getTranslationKey(), "Strength Path");
        add(GuPath.DREAM.getTranslationKey(), "Dream Path");
        add(GuPath.REFINEMENT.getTranslationKey(), "Refinement Path");
        add(GuPath.WISDOM.getTranslationKey(), "Wisdom Path");
        add(GuPath.THEFT.getTranslationKey(), "Theft Path");
        add(GuPath.LUCK.getTranslationKey(), "Luck Path");
        add(GuPath.KILLING.getTranslationKey(), "Killing Path");
        add(GuPath.BLOOD.getTranslationKey(), "Blood Path");
        add(GuPath.SOUL.getTranslationKey(), "Soul Path");
        add(GuPath.ENSLAVEMENT.getTranslationKey(), "Enslavement Path");
        add(GuPath.FOOD.getTranslationKey(), "Food Path");
        add(GuPath.FORMATION.getTranslationKey(), "Formation Path");
        add(GuPath.PAINTING.getTranslationKey(), "Painting Path");
        add(GuPath.TRANSFORMATION.getTranslationKey(), "Transformation Path");
    }

    private void addAttainment() {
        add(GuPathAttainment.NONE.getTranslationKey(), "None");
        add(GuPathAttainment.ORDINARY.getTranslationKey(), "Ordinary");
        add(GuPathAttainment.QUASI_MASTER.getTranslationKey(), "Quasi-Master");
        add(GuPathAttainment.MASTER.getTranslationKey(), "Master");
        add(GuPathAttainment.QUASI_GRANDMASTER.getTranslationKey(), "Quasi-Grandmaster");
        add(GuPathAttainment.GRANDMASTER.getTranslationKey(), "Grandmaster");
        add(GuPathAttainment.QUASI_GREAT_GRANDMASTER.getTranslationKey(), "Quasi-Great Grandmaster");
        add(GuPathAttainment.GREAT_GRANDMASTER.getTranslationKey(), "Great Grandmaster");
        add(GuPathAttainment.QUASI_SUPREME_GRANDMASTER.getTranslationKey(), "Quasi-Supreme Grandmaster");
        add(GuPathAttainment.SUPREME_GRANDMASTER.getTranslationKey(), "Supreme Grandmaster");
    }
    //endregion
}
