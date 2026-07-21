package com.unknown.guzhenren.datagen.lang;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.aperture.EssenceColor;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.custom.enums.soul.SoulTier;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

//  Every table is column-aligned -- seeing at a glance which key carries which phrase IS this file's
//  whole value, and a hard wrap destroys it.
public class EnUsLanguageProvider extends LanguageProvider {
    public EnUsLanguageProvider(PackOutput output) {
        super(output, Guzhenren.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addEnumKeys();
        addDisplayKeys();
        addCommandKeys();
        addScreenKeys();
        addItemKeys();
        addDeathMessages();
    }

    //  Every enum here is EnumTranslatable, so the constant IS the key -- short enough to align,
    //  which is exactly what that interface is for.
    private void add(EnumTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    //  The values, shared by the HUD and /guzhenren info (see ModDisplayText); each caller adds its label.
    //  realm joins directly in Chinese (一转巅峰) but needs a space here -- the separator lives in the key
    private void addDisplayKeys() {
        add("guzhenren.display.realm", "%s %s");
        add("guzhenren.display.gu_line", "%s %s %s");
        add("guzhenren.display.gu", "Gu");
        add("guzhenren.display.gu_material", "Gu Material");
        add("guzhenren.display.physique", "[%s]");
        add("guzhenren.display.lifespan", "%s [age %s]");
        add("guzhenren.display.base_fraction", "%s %s");
        add("guzhenren.display.base_round", "%s");
        add("guzhenren.display.base_full", "One Hundred");
        add("guzhenren.display.base_tens.2", "Twenty");
        add("guzhenren.display.base_tens.3", "Thirty");
        add("guzhenren.display.base_tens.4", "Forty");
        add("guzhenren.display.base_tens.5", "Fifty");
        add("guzhenren.display.base_tens.6", "Sixty");
        add("guzhenren.display.base_tens.7", "Seventy");
        add("guzhenren.display.base_tens.8", "Eighty");
        add("guzhenren.display.base_tens.9", "Ninety");
        add("guzhenren.display.base_units.1", "One");
        add("guzhenren.display.base_units.2", "Two");
        add("guzhenren.display.base_units.3", "Three");
        add("guzhenren.display.base_units.4", "Four");
        add("guzhenren.display.base_units.5", "Five");
        add("guzhenren.display.base_units.6", "Six");
        add("guzhenren.display.base_units.7", "Seven");
        add("guzhenren.display.base_units.8", "Eight");
        add("guzhenren.display.base_units.9", "Nine");
        add("guzhenren.display.none", "[NONE]");
        add("guzhenren.display.wild", "Wild %s");
        add("guzhenren.display.vital", "Vital %s");
        add("guzhenren.display.boar_strength.1", "[Strength of One Boar]");
        add("guzhenren.display.boar_strength.2", "[Strength of Two Boars]");
        add("guzhenren.display.jun_strength.jin.1", "[Strength of One Jin]");
        add("guzhenren.display.jun_strength.jin.2", "[Strength of Two Jin]");
        add("guzhenren.display.jun_strength.jin.3", "[Strength of Three Jin]");
        add("guzhenren.display.jun_strength.jin.4", "[Strength of Four Jin]");
        add("guzhenren.display.jun_strength.jin.5", "[Strength of Five Jin]");
        add("guzhenren.display.jun_strength.jin.6", "[Strength of Six Jin]");
        add("guzhenren.display.jun_strength.jin.7", "[Strength of Seven Jin]");
        add("guzhenren.display.jun_strength.jin.8", "[Strength of Eight Jin]");
        add("guzhenren.display.jun_strength.jin.9", "[Strength of Nine Jin]");

        add("guzhenren.hud.lifespan", "Lifespan %s");
        add("guzhenren.hud.refining", "Refining  %s / %s");
        add("guzhenren.hud.using", "Using  %s / %s");
    }
    //endregion

    //region COMMAND
    //  Three feedback classes: info default / success green / failure red, all tagged [GZR]
    //  (see ModCommandFeedback)
    private void addCommandKeys() {
        add("guzhenren.command.header", "[GZR]");
        add("guzhenren.command.tagged", "[GZR] %s");
        add("guzhenren.command.updated", "Updated %s player(s)");
        add("guzhenren.command.unknown_value", "Unknown value: %s");

        add("guzhenren.command.failed.awakened", "%s has already awakened -- run /guzhenren reset first to re-roll");
        add("guzhenren.command.failed.unawakened", "%s has not awakened -- cultivation values are established by /guzhenren awaken");
        add("guzhenren.command.failed.qi_mark", "The Qi Path's marks are the sum of every qi -- set them with /guzhenren body qi <type>");

        add("guzhenren.command.info.aperture_index", "Aperture %s");
        add("guzhenren.command.info.aperture_state", "Aperture:    %s");
        add("guzhenren.command.info.realm", "Cultivation: %s");
        add("guzhenren.command.info.talent", "Aptitude:    %s");
        add("guzhenren.command.info.essence", "Essence:     %s / %s");
        add("guzhenren.command.info.primary_path", "Primary:     %s");
        add("guzhenren.command.info.secondary_path", "Secondary:   %s");
        add("guzhenren.command.info.soul", "Soul:        %s / %s");
        add("guzhenren.command.info.lifespan", "Lifespan:    %s");
        add("guzhenren.command.info.life_state", "Body:        %s");
        add("guzhenren.command.info.life_form", "Life form:   %s");
        add("guzhenren.command.info.qi", "Qi Path: %s");
        add("guzhenren.command.info.qi_total", " Marks %s");
        add("guzhenren.command.info.qi_entry", "  %s  %s");
        add("guzhenren.command.info.paths", "Paths:");
        add("guzhenren.command.info.path_entry", "  %s  %s  Marks %s");
        add("guzhenren.command.info.path_speck", " Specks %s");
        add("guzhenren.command.info.strength", "Strength Path:");
        add("guzhenren.command.info.strength_entry", "  %s  %s");
        add("guzhenren.command.info.brilliance", "Brilliance:  %s");
        add("guzhenren.command.info.brilliance_rate", "%s thoughts/s");
        add("guzhenren.command.info.mind", "Mind Ocean:");
        add("guzhenren.command.info.mind_entry", "  %s  %s / %s");

        //  Derived detail for the operator, dimmed at the end of a line: aptitude base / soul title.
        add("guzhenren.command.info.detail", " [%s]");
    }
    //endregion

    //region SCREEN
    //  The G-key info panel (see client/screen/PlayerInfoScreen) plus its keybind
    private void addScreenKeys() {
        add("key.categories.guzhenren", "Guzhenren");
        add("key.guzhenren.open_info", "Open Info Panel");

        add("guzhenren.screen.info.title", "Info");
        add("guzhenren.screen.tab.aperture", "Aperture");
        add("guzhenren.screen.tab.body", "Body");
        add("guzhenren.screen.tab.mind", "Mind");
        add("guzhenren.screen.tab.storage", "Storage");
        add("guzhenren.menu.aperture_storage", "Aperture Storage");
        add("guzhenren.menu.vital", "Vital");
        add("guzhenren.screen.label.primary_path", "Primary Path");
        add("guzhenren.screen.label.secondary_path", "Secondary Path");
        add("guzhenren.screen.pick.title", "Choose a Secondary Path");
        add("guzhenren.screen.pick.hint", "click to set");
        add("guzhenren.screen.label.realm", "Realm");
        add("guzhenren.screen.label.talent", "Aptitude");
        add("guzhenren.screen.label.essence", "Essence");
        add("guzhenren.screen.label.state", "State");
        add("guzhenren.screen.label.life_form", "Form");
        add("guzhenren.screen.label.soul", "Soul");
        add("guzhenren.screen.label.lifespan", "Lifespan");
        add("guzhenren.screen.label.qi", "Qi Path");
        add("guzhenren.screen.label.paths", "Paths");
        add("guzhenren.screen.label.strength", "Strength Path");
        add("guzhenren.screen.label.brilliance", "Brilliance");
        add("guzhenren.screen.path_value", "%s Marks %s");
    }
    //endregion

    //region ITEM
    //  A refusal here addresses "you", so the command's keys don't fit -- those name a target.
    //  Nothing is said on success: the essence bar is the one that speaks.
    private void addItemKeys() {
        add("item.guzhenren.hope_gu", "Hope Gu");
        add("item.guzhenren.copper_relics_gu", "Green Copper Relics Gu");
        add("item.guzhenren.steel_relics_gu", "Red Steel Relics Gu");
        add("item.guzhenren.silver_relics_gu", "White Silver Relics Gu");
        add("item.guzhenren.gold_relics_gu", "Yellow Gold Relics Gu");
        add("item.guzhenren.crystal_relics_gu", "Purple Crystal Relics Gu");
        add("item.guzhenren.white_boar_gu", "White Boar Gu");
        add("item.guzhenren.black_boar_gu", "Black Boar Gu");
        add("item.guzhenren.jin_strength_gu", "Jin Strength Gu");
        add("item.guzhenren.vitality_leaf_gu", "Vitality Leaf Gu");
        add("item.guzhenren.primeval_stone", "Primeval Stone");

        add("itemGroup.guzhenren.mortal_gu", "Mortal Gu");
        add("itemGroup.guzhenren.gu_material", "Gu Material");

        add("guzhenren.item.failed.awakened", "You have already awakened");
        add("guzhenren.item.failed.unawakened", "You have NOT awakened");
        add("guzhenren.item.failed.essence_full", "Essence is already FULL");
        add("guzhenren.item.failed.rank_mismatch", "Wrong realm - this Gu needs %s");
        add("guzhenren.item.failed.stage_peak", "You are at the Stage Peak");
        add("guzhenren.item.failed.beast_strength_held", "Already hold the %s's strength");
        add("guzhenren.item.failed.jun_strength_full", "Already hold Nine Jin of strength");
        add("guzhenren.item.failed.vitality_active", "Vitality Leaf is still working");
        add("guzhenren.item.failed.refine_essence", "NOT enough essence to refine");

        add("guzhenren.item.gu.uses", "Used %s/%s");
        add("guzhenren.item.gu.refine_progress", "Refined %s/%s");
        add("guzhenren.item.gu.hungry", "Your %s is hungry");
        add("guzhenren.item.gu.starved", "Your %s starved to death");
        add("guzhenren.item.gu.exhausted", "Your %s was forced past its limit and died");
        add("guzhenren.item.gu.vital_lost", "Your %s is gone -- health, soul and mind all suffer!");

        add("effect.guzhenren.vitality_leaf", "Vitality Leaf");
    }
    //endregion

    //region DEATH
    //  The key is decided by the DamageType's msgId: msgId "guzhenren.xxx" -> "death.attack.guzhenren.xxx"
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted", "%1$s ran out of lifespan");
        add("death.attack.guzhenren.soul_collapse", "%1$s suffered soul collapse");
        add("death.attack.guzhenren.mind_ocean_shattered", "%1$s shattered their Mind Ocean");
        add("death.attack.guzhenren.vital_gu_lost", "%1$s lost their Vital Gu");
    }
    //endregion

    //region ENUM
    private void addEnumKeys() {
        addRank();
        addStage();
        addTalent();
        addApertureState();
        addEssenceColor();
        addTenExtreme();
        addLifeForm();
        addLifeState();
        addSoulTier();
        addQiType();
        addPath();
        addAttainment();
        addWisdomType();
        addBrilliance();
        addBeastStrength();
        addStrengthBranch();
        addJunStrength();
    }

    //  Beast strengths: taken by consuming a beast Gu, one kind once ever. Two boars today
    private void addBeastStrength() {
        add(BeastStrength.WHITE_BOAR, "White Boar");
        add(BeastStrength.BLACK_BOAR, "Black Boar");
    }

    //  The Strength Path's three branches. Titles today; effects come later, and ENVIRONMENT has no data
    private void addStrengthBranch() {
        add(StrengthBranch.HUMAN, "Human Jun Strength Branch");
        add(StrengthBranch.BEASTS, "Beast Strength Phantom Branch");
        add(StrengthBranch.ENVIRONMENT, "Atmospheric Heaven and Earth Branch");
    }

    //  The Human Jun branch's grades. Ten Jin and Hundred Jin become their own later
    private void addJunStrength() {
        add(JunStrength.JIN, "Jin");
    }

    //  An aperture is only alive or dead; the body's LifeState is the one with a third state, zombie
    private void addApertureState() {
        add(ApertureState.ALIVE, "Alive");
        add(ApertureState.DEAD, "Dead");
    }

    private void addRank() {
        add(Rank.NONE, "Mortal");
        add(Rank.ONE, "Rank I");
        add(Rank.TWO, "Rank II");
        add(Rank.THREE, "Rank III");
        add(Rank.FOUR, "Rank IV");
        add(Rank.FIVE, "Rank V");
        add(Rank.SIX, "Rank VI");
        add(Rank.SEVEN, "Rank VII");
        add(Rank.EIGHT, "Rank VIII");
        add(Rank.NINE, "Rank IX");
    }

    private void addStage() {
        //  Deliberately blank: nothing is shown while unawakened
        add(Stage.NONE, "");
        add(Stage.INIT, "Initial");
        add(Stage.MIDDLE, "Middle");
        add(Stage.UPPER, "Upper");
        add(Stage.PEAK, "Peak");
    }

    //  The grades 甲乙丙丁 are the classic Chinese ordinals, so they land on A/B/C/D, not I..IV.
    private void addTalent() {
        add(Talent.EXTREME, "Ten-Extremes Aptitude");
        add(Talent.FIRST, "Grade-A Aptitude");
        add(Talent.SECOND, "Grade-B Aptitude");
        add(Talent.THIRD, "Grade-C Aptitude");
        add(Talent.FOURTH, "Grade-D Aptitude");
        add(Talent.NONE, "Unawakened");
    }

    private void addLifeForm() {
        add(LifeForm.MORTAL, "Mortal");
        add(LifeForm.IMMORTAL, "Immortal");
    }

    private void addLifeState() {
        add(LifeState.ALIVE, "Alive");
        add(LifeState.ZOMBIE, "Zombified");
        add(LifeState.DEAD, "Dead");
    }

    //  Essence colors, one per rank; the shade within a rank is computed by EssenceColor.shade from the
    //  stage, so it needs no text of its own
    private void addEssenceColor() {
        add(EssenceColor.NONE, "None");
        add(EssenceColor.GREEN_COPPER, "Green Copper");
        add(EssenceColor.RED_STEEL, "Red Steel");
        add(EssenceColor.WHITE_SILVER, "White Silver");
        add(EssenceColor.YELLOW_GOLDEN, "Yellow Golden");
        add(EssenceColor.PURPLE_CRYSTAL, "Purple Crystal");
        add(EssenceColor.GREEN_GRAPE, "Green Grape");
        add(EssenceColor.RED_DATE, "Red Date");
        add(EssenceColor.WHITE_LITCHI, "White Litchi");
        add(EssenceColor.YELLOW_APRICOT, "Yellow Apricot");
    }

    private void addTenExtreme() {
        //  Deliberately blank: nothing is shown for a player without an extreme physique
        add(ExtremePhysique.NONE, "");
        add(ExtremePhysique.VERDANT_GREAT_SUN, "Verdant Great Sun");
        add(ExtremePhysique.DESOLATE_ANCIENT_MOON, "Desolate Ancient Moon");
        add(ExtremePhysique.NORTHERN_DARK_ICE_SOUL, "Northern Dark Ice Soul");
        add(ExtremePhysique.BOUNDLESS_FOREST_SAMSARA, "Boundless Forest Samsara");
        add(ExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE, "Blazing Glory Lightning Brilliance");
        add(ExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE, "Myriad Gold Wondrous Essence");
        add(ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL, "Great Strength True Martial");
        add(ExtremePhysique.CAREFREE_WISDOM_HEART, "Carefree Wisdom Heart");
        add(ExtremePhysique.PROFOUND_EARTH_ORIGIN, "Profound Earth Origin");
        add(ExtremePhysique.UNIVERSE_GREAT_DERIVATION, "Universe Great Derivation");
        add(ExtremePhysique.PURE_DREAM_REALITY_SEEKER, "Pure Dream Reality Seeker");
    }

    //  Heaven, earth and human qi are the threshold for ascension; natural qi has no effect.
    //  Their sum IS the Qi Path's marks (never stored twice, see QiData)
    private void addQiType() {
        add(QiType.HEAVEN, "Heaven Qi");
        add(QiType.EARTH, "Earth Qi");
        add(QiType.HUMAN, "Human Qi");
        add(QiType.NATURAL, "Natural Qi");
    }

    private void addPath() {
        add(GuPath.HEAVEN, "Heaven Path");
        add(GuPath.RULE, "Rule Path");
        add(GuPath.SPACE, "Space Path");
        add(GuPath.TIME, "Time Path");
        add(GuPath.HUMAN, "Human Path");
        add(GuPath.METAL, "Metal Path");
        add(GuPath.WOOD, "Wood Path");
        add(GuPath.WATER, "Water Path");
        add(GuPath.FIRE, "Fire Path");
        add(GuPath.EARTH, "Earth Path");
        add(GuPath.ICE_SNOW, "Ice-Snow Path");
        add(GuPath.LIGHTNING, "Lightning Path");
        add(GuPath.QI, "Qi Path");
        add(GuPath.SOUND, "Sound Path");
        add(GuPath.LIGHT, "Light Path");
        add(GuPath.DARK, "Dark Path");
        add(GuPath.STRENGTH, "Strength Path");
        add(GuPath.DREAM, "Dream Path");
        add(GuPath.REFINEMENT, "Refinement Path");
        add(GuPath.WISDOM, "Wisdom Path");
        add(GuPath.THEFT, "Theft Path");
        add(GuPath.LUCK, "Luck Path");
        add(GuPath.KILLING, "Killing Path");
        add(GuPath.BLOOD, "Blood Path");
        add(GuPath.SOUL, "Soul Path");
        add(GuPath.ENSLAVEMENT, "Enslavement Path");
        add(GuPath.FOOD, "Food Path");
        add(GuPath.FORMATION, "Formation Path");
        add(GuPath.PAINTING, "Painting Path");
        add(GuPath.TRANSFORMATION, "Transformation Path");
    }

    private void addAttainment() {
        add(GuAttainment.NONE, "None");
        add(GuAttainment.ORDINARY, "Ordinary");
        add(GuAttainment.QUASI_MASTER, "Quasi-Master");
        add(GuAttainment.MASTER, "Master");
        add(GuAttainment.QUASI_GRANDMASTER, "Quasi-Grandmaster");
        add(GuAttainment.GRANDMASTER, "Grandmaster");
        add(GuAttainment.QUASI_GREAT_GRANDMASTER, "Quasi-Great Grandmaster");
        add(GuAttainment.GREAT_GRANDMASTER, "Great Grandmaster");
        add(GuAttainment.QUASI_SUPREME_GRANDMASTER, "Quasi-Supreme Grandmaster");
        add(GuAttainment.SUPREME_GRANDMASTER, "Supreme Grandmaster");
    }

    //  Soul tiers: looked up from maxSoul (soul = men × 100), never stored on their own
    private void addSoulTier() {
        add(SoulTier.ONE, "One-Person Soul");
        add(SoulTier.TEN, "Ten-Person Soul");
        add(SoulTier.HUNDRED, "Hundred-Person Soul");
        add(SoulTier.THOUSAND, "Thousand-Person Soul");
        add(SoulTier.TEN_THOUSAND, "Ten-Thousand-Person Soul");
        add(SoulTier.HUNDRED_THOUSAND, "Hundred-Thousand-Person Soul");
        add(SoulTier.MILLION, "Million-Person Soul");
        add(SoulTier.TEN_MILLION, "Ten-Million-Person Soul");
        add(SoulTier.HUNDRED_MILLION, "Hundred-Million-Person Soul");
    }

    //  The three cells; initial capacities 30000 / 5 / 2, and overflowing them shatters the Mind Ocean
    private void addWisdomType() {
        add(WisdomType.THOUGHTS, "Thoughts");
        add(WisdomType.WILLS, "Wills");
        add(WisdomType.EMOTIONS, "Emotions");
    }

    //  Brilliance: the natural thought regen rate (1 / 4 / 16 / 64 / 256 a second), rolled at birth
    private void addBrilliance() {
        add(Brilliance.ORDINARY, "Ordinary Brilliance");
        add(Brilliance.DECENT, "Decent Brilliance");
        add(Brilliance.DISTINCTIVE, "Distinctive Brilliance");
        add(Brilliance.OUTSTANDING, "Outstanding Brilliance");
        add(Brilliance.UNRIVALED, "Unrivaled Brilliance");
    }
    //endregion
}
