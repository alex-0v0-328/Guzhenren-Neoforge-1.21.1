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
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
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
        addScreenKeys();
        addItemKeys();
        addDeathMessages();
    }

    //  每个 Gu* 枚举都是 GuTranslatable, 所以常量本身就是键 —— 短到能对齐, 这就是那个接口的用处.
    private void add(EnumTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    //  值的部分, HUD 与 /guzhenren info 共用 (见 ModDisplayText); 标签由各自的键补
    //  realm 在中文里是直接拼接的 ("一转巅峰"), 英文需要一个空格 —— 分隔符只存在于键里
    private void addDisplayKeys() {
        add("guzhenren.display.realm",                     "%s %s");
        add("guzhenren.display.gu_line",                   "%s %s %s");
        add("guzhenren.display.gu",                        "Gu");
        add("guzhenren.display.gu_material",               "Gu Material");
        add("guzhenren.display.physique",                  "[%s]");
        add("guzhenren.display.lifespan",                  "%s [age %s]");
        add("guzhenren.display.base_fraction",             "%s %s");
        add("guzhenren.display.base_round",                "%s");
        add("guzhenren.display.base_full",                 "One Hundred");
        add("guzhenren.display.base_tens.2",               "Twenty");
        add("guzhenren.display.base_tens.3",               "Thirty");
        add("guzhenren.display.base_tens.4",               "Forty");
        add("guzhenren.display.base_tens.5",               "Fifty");
        add("guzhenren.display.base_tens.6",               "Sixty");
        add("guzhenren.display.base_tens.7",               "Seventy");
        add("guzhenren.display.base_tens.8",               "Eighty");
        add("guzhenren.display.base_tens.9",               "Ninety");
        add("guzhenren.display.base_units.1",              "One");
        add("guzhenren.display.base_units.2",              "Two");
        add("guzhenren.display.base_units.3",              "Three");
        add("guzhenren.display.base_units.4",              "Four");
        add("guzhenren.display.base_units.5",              "Five");
        add("guzhenren.display.base_units.6",              "Six");
        add("guzhenren.display.base_units.7",              "Seven");
        add("guzhenren.display.base_units.8",              "Eight");
        add("guzhenren.display.base_units.9",              "Nine");
        add("guzhenren.display.none",                      "[NONE]");

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
        add("guzhenren.command.failed.qi_mark",            "The Qi Path's marks are the sum of every qi -- set them with /guzhenren body qi <type>");
        add("guzhenren.command.failed.qi_speck",           "The Qi Path has no specks -- marks are the sum of qi");

        add("guzhenren.command.info.aperture_index",       "Aperture %s");
        add("guzhenren.command.info.aperture_state",       "Aperture:    %s");
        add("guzhenren.command.info.realm",                "Cultivation: %s");
        add("guzhenren.command.info.talent",               "Aptitude:    %s");
        add("guzhenren.command.info.essence",              "Essence:     %s / %s");
        add("guzhenren.command.info.soul",                 "Soul:        %s / %s");
        add("guzhenren.command.info.lifespan",             "Lifespan:    %s");
        add("guzhenren.command.info.life_state",           "Body:        %s");
        add("guzhenren.command.info.life_form",            "Life form:   %s");
        add("guzhenren.command.info.qi",                   "Qi Path: %s");
        add("guzhenren.command.info.qi_total",             " Marks %s");
        add("guzhenren.command.info.qi_entry",             "  %s  %s");
        add("guzhenren.command.info.paths",                "Paths:");
        add("guzhenren.command.info.path_entry",           "  %s  %s  Marks %s");
        add("guzhenren.command.info.path_speck",           " Specks %s");
        add("guzhenren.command.info.brilliance",           "Brilliance:  %s");
        add("guzhenren.command.info.brilliance_rate",      "%s thoughts/s");
        add("guzhenren.command.info.mind",                 "Mind Ocean:");
        add("guzhenren.command.info.mind_entry",           "  %s  %s / %s");

        //  Derived detail for the operator, dimmed at the end of a line: aptitude base / soul title.
        add("guzhenren.command.info.detail",               " [%s]");
    }
    //endregion

    //region SCREEN
    //  The G-key info panel (see client/screen/PlayerInfoScreen) plus its keybind
    private void addScreenKeys() {
        add("key.categories.guzhenren",                    "Guzhenren");
        add("key.guzhenren.open_info",                     "Open Info Panel");

        add("guzhenren.screen.info.title",                 "Info");
        add("guzhenren.screen.tab.aperture",               "Aperture");
        add("guzhenren.screen.tab.body",                   "Body");
        add("guzhenren.screen.tab.mind",                   "Mind");
        add("guzhenren.screen.label.realm",                "Realm");
        add("guzhenren.screen.label.talent",               "Aptitude");
        add("guzhenren.screen.label.essence",              "Essence");
        add("guzhenren.screen.label.state",                "State");
        add("guzhenren.screen.label.life_form",            "Form");
        add("guzhenren.screen.label.soul",                 "Soul");
        add("guzhenren.screen.label.lifespan",             "Lifespan");
        add("guzhenren.screen.label.qi",                   "Qi Path");
        add("guzhenren.screen.label.paths",                "Paths");
        add("guzhenren.screen.label.brilliance",           "Brilliance");
        add("guzhenren.screen.path_value",                 "%s Marks %s");
    }
    //endregion

    //region ITEM
    //  A refusal here addresses "you", so the command's keys don't fit -- those name a target.
    //  Nothing is said on success: the essence bar is the one that speaks.
    private void addItemKeys() {
        add("item.guzhenren.hope_gu",                      "Hope Gu");
        add("item.guzhenren.primeval_stone",               "Primeval Stone");
        add("itemGroup.guzhenren.main",                    "Guzhenren");

        add("guzhenren.item.failed.awakened",              "You have already awakened -- the Hope Gu is wasted on you");
        add("guzhenren.item.failed.unawakened",            "You have not awakened -- no aperture to hold essence");
        add("guzhenren.item.failed.essence_full",          "Your essence is already full");
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
    }

    //  空窍只有生死两态; 肉身的 LifeState 才有第三态「僵」
    private void addApertureState() {
        add(ApertureState.ALIVE, "Alive");
        add(ApertureState.DEAD,  "Dead");
    }

    private void addRank() {
        add(Rank.NONE,  "Mortal");
        add(Rank.ONE,   "Rank I");
        add(Rank.TWO,   "Rank II");
        add(Rank.THREE, "Rank III");
        add(Rank.FOUR,  "Rank IV");
        add(Rank.FIVE,  "Rank V");
        add(Rank.SIX,   "Rank VI");
        add(Rank.SEVEN, "Rank VII");
        add(Rank.EIGHT, "Rank VIII");
        add(Rank.NINE,  "Rank IX");
    }

    private void addStage() {
        //  故意留空: 未开窍时不显示
        add(Stage.NONE,   "");
        add(Stage.INIT,   "Initial");
        add(Stage.MIDDLE, "Middle");
        add(Stage.UPPER,  "Upper");
        add(Stage.PEAK,   "Peak");
    }

    //  甲乙丙丁 are the classic Chinese ordinals, so they land on A/B/C/D rather than on I..IV.
    private void addTalent() {
        add(Talent.EXTREME, "Ten-Extremes Aptitude");
        add(Talent.FIRST,   "Grade-A Aptitude");
        add(Talent.SECOND,  "Grade-B Aptitude");
        add(Talent.THIRD,   "Grade-C Aptitude");
        add(Talent.FOURTH,  "Grade-D Aptitude");
        add(Talent.NONE,    "Unawakened");
    }

    private void addLifeForm() {
        add(LifeForm.MORTAL,   "Mortal");
        add(LifeForm.IMMORTAL, "Immortal");
    }

    private void addLifeState() {
        add(LifeState.ALIVE,  "Alive");
        add(LifeState.ZOMBIE, "Zombified");
        add(LifeState.DEAD,   "Dead");
    }

    //  真元色, 每转一色; 同转内的深浅由 EssenceColor.shade 按小境界算, 不另出文案
    private void addEssenceColor() {
        add(EssenceColor.NONE,           "None");
        add(EssenceColor.GREEN_COPPER,   "Green Copper");
        add(EssenceColor.RED_STEEL,      "Red Steel");
        add(EssenceColor.WHITE_SILVER,   "White Silver");
        add(EssenceColor.YELLOW_GOLDEN,  "Yellow Golden");
        add(EssenceColor.PURPLE_CRYSTAL, "Purple Crystal");
        add(EssenceColor.GREEN_GRAPE,    "Green Grape");
        add(EssenceColor.RED_DATE,       "Red Date");
        add(EssenceColor.WHITE_LITCHI,   "White Litchi");
        add(EssenceColor.YELLOW_APRICOT, "Yellow Apricot");
    }

    private void addTenExtreme() {
        //  故意留空: 非十绝体玩家不显示
        add(ExtremePhysique.NONE,                               "");
        add(ExtremePhysique.VERDANT_GREAT_SUN,                  "Verdant Great Sun");
        add(ExtremePhysique.DESOLATE_ANCIENT_MOON,              "Desolate Ancient Moon");
        add(ExtremePhysique.NORTHERN_DARK_ICE_SOUL,             "Northern Dark Ice Soul");
        add(ExtremePhysique.BOUNDLESS_FOREST_SAMSARA,           "Boundless Forest Samsara");
        add(ExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE, "Blazing Glory Lightning Brilliance");
        add(ExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE,       "Myriad Gold Wondrous Essence");
        add(ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL,        "Great Strength True Martial");
        add(ExtremePhysique.CAREFREE_WISDOM_HEART,              "Carefree Wisdom Heart");
        add(ExtremePhysique.PROFOUND_EARTH_ORIGIN,              "Profound Earth Origin");
        add(ExtremePhysique.UNIVERSE_GREAT_DERIVATION,          "Universe Great Derivation");
        add(ExtremePhysique.PURE_DREAM_REALITY_SEEKER,          "Pure Dream Reality Seeker");
    }

    //  天地人三气是升仙的门槛; 自然气无效用. 诸气之和即气道的道痕 (不另存, 见 QiData)
    private void addQiType() {
        add(QiType.HEAVEN,  "Heaven Qi");
        add(QiType.EARTH,   "Earth Qi");
        add(QiType.HUMAN,   "Human Qi");
        add(QiType.NATURAL, "Natural Qi");
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
        add(GuAttainment.NONE,                      "None");
        add(GuAttainment.ORDINARY,                  "Ordinary");
        add(GuAttainment.QUASI_MASTER,              "Quasi-Master");
        add(GuAttainment.MASTER,                    "Master");
        add(GuAttainment.QUASI_GRANDMASTER,         "Quasi-Grandmaster");
        add(GuAttainment.GRANDMASTER,               "Grandmaster");
        add(GuAttainment.QUASI_GREAT_GRANDMASTER,   "Quasi-Great Grandmaster");
        add(GuAttainment.GREAT_GRANDMASTER,         "Great Grandmaster");
        add(GuAttainment.QUASI_SUPREME_GRANDMASTER, "Quasi-Supreme Grandmaster");
        add(GuAttainment.SUPREME_GRANDMASTER,       "Supreme Grandmaster");
    }

    //  魂魄境界: 由魂魄值反查 (魂魄值 = 人数 × 100), 不单独存
    private void addSoulTier() {
        add(SoulTier.ONE,              "One-Person Soul");
        add(SoulTier.TEN,              "Ten-Person Soul");
        add(SoulTier.HUNDRED,          "Hundred-Person Soul");
        add(SoulTier.THOUSAND,         "Thousand-Person Soul");
        add(SoulTier.TEN_THOUSAND,     "Ten-Thousand-Person Soul");
        add(SoulTier.HUNDRED_THOUSAND, "Hundred-Thousand-Person Soul");
        add(SoulTier.MILLION,          "Million-Person Soul");
        add(SoulTier.TEN_MILLION,      "Ten-Million-Person Soul");
        add(SoulTier.HUNDRED_MILLION,  "Hundred-Million-Person Soul");
    }

    //  智道三态; 脑海初始容量 念30000 / 意5 / 情2, 装不下即脑海炸裂
    private void addWisdomType() {
        add(WisdomType.THOUGHTS, "Thoughts");
        add(WisdomType.WILLS,    "Wills");
        add(WisdomType.EMOTIONS, "Emotions");
    }

    //  才情: 念的自然恢复速度 (每秒 1 / 4 / 16 / 64 / 256), 出生时抽取
    private void addBrilliance() {
        add(Brilliance.ORDINARY,    "Ordinary Brilliance");
        add(Brilliance.DECENT,      "Decent Brilliance");
        add(Brilliance.DISTINCTIVE, "Distinctive Brilliance");
        add(Brilliance.OUTSTANDING, "Outstanding Brilliance");
        add(Brilliance.UNRIVALED,   "Unrivaled Brilliance");
    }
    //endregion
}
