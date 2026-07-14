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
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.custom.enums.GuTranslatable;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

//  每张表都按列对齐 —— 这个文件的全部价值就是「一眼看出哪个键配哪句话」, 硬折行会毁掉它.
public class EnUsLanguageProvider extends LanguageProvider {
    public EnUsLanguageProvider(PackOutput output) {
        super(output, Guzhenren.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addEnumKeys();
        addDisplayKeys();
        addCommandKeys();
        addDeathMessages();
    }

    //  每个 Gu* 枚举都是 GuTranslatable, 所以常量本身就是键 —— 短到能对齐, 这就是那个接口的用处.
    private void add(GuTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    //  值的部分, HUD 与 /guzhenren info 共用 (见 ModDisplayText); 标签由各自的键补
    //  realm 在中文里是直接拼接的 ("一转巅峰"), 英文需要一个空格 —— 分隔符只存在于键里
    private void addDisplayKeys() {
        add("guzhenren.display.realm",                     "%s %s");
        add("guzhenren.display.physique",                  "[ %s ]");
        add("guzhenren.display.lifespan",                  "%s [ age %s ]");

        add("guzhenren.hud.lifespan",                      "Lifespan %s");
    }
    //endregion

    //region COMMAND
    //  三类反馈: info 默认色 / 成功绿 / 失败红, 全部带 [GZR] 前缀 (见 ModCommandFeedback)
    private void addCommandKeys() {
        add("guzhenren.command.header",                    "[GZR]");
        add("guzhenren.command.tagged",                    "[GZR] %s");
        add("guzhenren.command.updated",                   "Updated %s player(s)");
        add("guzhenren.command.unknown_value",             "Unknown value: %s");

        add("guzhenren.command.failed.awakened",           "%s has already awakened -- run /guzhenren reset first to re-roll");
        add("guzhenren.command.failed.unawakened",         "%s has not awakened -- cultivation values are established by /guzhenren awaken");

        add("guzhenren.command.info.realm",                "Cultivation: %s");
        add("guzhenren.command.info.talent",               "Aptitude:    %s");
        add("guzhenren.command.info.essence",              "Essence:     %s / %s");
        add("guzhenren.command.info.soul",                 "Soul:        %s / %s");
        add("guzhenren.command.info.lifespan",             "Lifespan:    %s");
        add("guzhenren.command.info.life_state",           "State:       %s");
        add("guzhenren.command.info.paths",                "Paths:");
        add("guzhenren.command.info.path_entry",           "  %s  %s  Marks %s");
        add("guzhenren.command.info.path_empty",           "  None");
        add("guzhenren.command.info.mind",                 "Mind Ocean:");
        add("guzhenren.command.info.mind_entry",           "  %s  %s / %s");

        //  Derived detail for the operator, dimmed at the end of a line: aptitude base / soul title.
        add("guzhenren.command.info.detail",               " (%s)");
    }
    //endregion

    //region DEATH
    //  键名由 DamageType 的 msgId 决定: msgId "guzhenren.xxx" -> "death.attack.guzhenren.xxx"
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted",   "%1$s ran out of lifespan");
        add("death.attack.guzhenren.soul_collapse",        "%1$s suffered soul collapse");
        add("death.attack.guzhenren.mind_ocean_shattered", "%1$s shattered their Mind Ocean");
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
        addWisdomType();
    }

    private void addRank() {
        add(GuRank.NONE,  "Mortal");
        add(GuRank.ONE,   "Rank I");
        add(GuRank.TWO,   "Rank II");
        add(GuRank.THREE, "Rank III");
        add(GuRank.FOUR,  "Rank IV");
        add(GuRank.FIVE,  "Rank V");
        add(GuRank.SIX,   "Rank VI");
        add(GuRank.SEVEN, "Rank VII");
        add(GuRank.EIGHT, "Rank VIII");
        add(GuRank.NINE,  "Rank IX");
    }

    private void addStage() {
        //  故意留空: 未开窍时不显示
        add(GuStage.NONE,   "");
        add(GuStage.INIT,   "Initial");
        add(GuStage.MIDDLE, "Middle");
        add(GuStage.UPPER,  "Upper");
        add(GuStage.PEAK,   "Peak");
    }

    //  甲乙丙丁 are the classic Chinese ordinals, so they land on A/B/C/D rather than on I..IV.
    private void addTalent() {
        add(GuTalent.EXTREME, "Ten-Extremes Aptitude");
        add(GuTalent.FIRST,   "Grade-A Aptitude");
        add(GuTalent.SECOND,  "Grade-B Aptitude");
        add(GuTalent.THIRD,   "Grade-C Aptitude");
        add(GuTalent.FOURTH,  "Grade-D Aptitude");
        add(GuTalent.NONE,    "Unawakened");
    }

    private void addLifeForm() {
        add(GuLifeForm.MORTAL,   "Mortal");
        add(GuLifeForm.IMMORTAL, "Immortal");
    }

    private void addLifeState() {
        add(GuLifeState.ALIVE,  "Alive");
        add(GuLifeState.ZOMBIE, "Zombified");
        add(GuLifeState.DEAD,   "Dead");
    }

    //  真元色, 每转一色; 同转内的深浅由 GuEssenceColor.shade 按小境界算, 不另出文案
    private void addEssenceColor() {
        add(GuEssenceColor.NONE,           "None");
        add(GuEssenceColor.GREEN_COPPER,   "Green Copper");
        add(GuEssenceColor.RED_STEEL,      "Red Steel");
        add(GuEssenceColor.WHITE_SILVER,   "White Silver");
        add(GuEssenceColor.YELLOW_GOLDEN,  "Yellow Golden");
        add(GuEssenceColor.PURPLE_CRYSTAL, "Purple Crystal");
        add(GuEssenceColor.GREEN_GRAPE,    "Green Grape");
        add(GuEssenceColor.RED_DATE,       "Red Date");
        add(GuEssenceColor.WHITE_LITCHI,   "White Litchi");
        add(GuEssenceColor.YELLOW_APRICOT, "Yellow Apricot");
    }

    private void addTenExtreme() {
        //  故意留空: 非十绝体玩家不显示
        add(GuExtremePhysique.NONE,                               "");
        add(GuExtremePhysique.VERDANT_GREAT_SUN,                  "Verdant Great Sun");
        add(GuExtremePhysique.DESOLATE_ANCIENT_MOON,              "Desolate Ancient Moon");
        add(GuExtremePhysique.NORTHERN_DARK_ICE_SOUL,             "Northern Dark Ice Soul");
        add(GuExtremePhysique.BOUNDLESS_FOREST_SAMSARA,           "Boundless Forest Samsara");
        add(GuExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE, "Blazing Glory Lightning Brilliance");
        add(GuExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE,       "Myriad Gold Wondrous Essence");
        add(GuExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL,        "Great Strength True Martial");
        add(GuExtremePhysique.CAREFREE_WISDOM_HEART,              "Carefree Wisdom Heart");
        add(GuExtremePhysique.PROFOUND_EARTH_ORIGIN,              "Profound Earth Origin");
        add(GuExtremePhysique.UNIVERSE_GREAT_DERIVATION,          "Universe Great Derivation");
        add(GuExtremePhysique.PURE_DREAM_REALITY_SEEKER,          "Pure Dream Reality Seeker");
    }

    private void addPath() {
        add(GuPath.HEAVEN,         "Heaven Path");
        add(GuPath.RULE,           "Rule Path");
        add(GuPath.SPACE,          "Space Path");
        add(GuPath.TIME,           "Time Path");
        add(GuPath.HUMAN,          "Human Path");
        add(GuPath.METAL,          "Metal Path");
        add(GuPath.WOOD,           "Wood Path");
        add(GuPath.WATER,          "Water Path");
        add(GuPath.FIRE,           "Fire Path");
        add(GuPath.EARTH,          "Earth Path");
        add(GuPath.ICE_SNOW,       "Ice-Snow Path");
        add(GuPath.LIGHTNING,      "Lightning Path");
        add(GuPath.QI,             "Qi Path");
        add(GuPath.SOUND,          "Sound Path");
        add(GuPath.LIGHT,          "Light Path");
        add(GuPath.DARK,           "Dark Path");
        add(GuPath.STRENGTH,       "Strength Path");
        add(GuPath.DREAM,          "Dream Path");
        add(GuPath.REFINEMENT,     "Refinement Path");
        add(GuPath.WISDOM,         "Wisdom Path");
        add(GuPath.THEFT,          "Theft Path");
        add(GuPath.LUCK,           "Luck Path");
        add(GuPath.KILLING,        "Killing Path");
        add(GuPath.BLOOD,          "Blood Path");
        add(GuPath.SOUL,           "Soul Path");
        add(GuPath.ENSLAVEMENT,    "Enslavement Path");
        add(GuPath.FOOD,           "Food Path");
        add(GuPath.FORMATION,      "Formation Path");
        add(GuPath.PAINTING,       "Painting Path");
        add(GuPath.TRANSFORMATION, "Transformation Path");
    }

    private void addAttainment() {
        add(GuPathAttainment.NONE,                      "None");
        add(GuPathAttainment.ORDINARY,                  "Ordinary");
        add(GuPathAttainment.QUASI_MASTER,              "Quasi-Master");
        add(GuPathAttainment.MASTER,                    "Master");
        add(GuPathAttainment.QUASI_GRANDMASTER,         "Quasi-Grandmaster");
        add(GuPathAttainment.GRANDMASTER,               "Grandmaster");
        add(GuPathAttainment.QUASI_GREAT_GRANDMASTER,   "Quasi-Great Grandmaster");
        add(GuPathAttainment.GREAT_GRANDMASTER,         "Great Grandmaster");
        add(GuPathAttainment.QUASI_SUPREME_GRANDMASTER, "Quasi-Supreme Grandmaster");
        add(GuPathAttainment.SUPREME_GRANDMASTER,       "Supreme Grandmaster");
    }

    //  魂魄境界: 由魂魄值反查 (魂魄值 = 人数 × 100), 不单独存
    private void addSoulTier() {
        add(GuSoulTier.ONE,              "One-Person Soul");
        add(GuSoulTier.TEN,              "Ten-Person Soul");
        add(GuSoulTier.HUNDRED,          "Hundred-Person Soul");
        add(GuSoulTier.THOUSAND,         "Thousand-Person Soul");
        add(GuSoulTier.TEN_THOUSAND,     "Ten-Thousand-Person Soul");
        add(GuSoulTier.HUNDRED_THOUSAND, "Hundred-Thousand-Person Soul");
        add(GuSoulTier.MILLION,          "Million-Person Soul");
        add(GuSoulTier.TEN_MILLION,      "Ten-Million-Person Soul");
        add(GuSoulTier.HUNDRED_MILLION,  "Hundred-Million-Person Soul");
    }

    //  智道三态; 脑海初始容量 念30000 / 意5 / 情2, 装不下即脑海炸裂
    private void addWisdomType() {
        add(GuWisdomType.THOUGHTS, "Thoughts");
        add(GuWisdomType.WILLS,    "Wills");
        add(GuWisdomType.EMOTIONS, "Emotions");
    }
    //endregion
}
