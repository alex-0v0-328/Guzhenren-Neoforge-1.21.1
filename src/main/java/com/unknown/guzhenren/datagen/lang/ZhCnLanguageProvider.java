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
public class ZhCnLanguageProvider extends LanguageProvider {
    public ZhCnLanguageProvider(PackOutput output) {
        super(output, Guzhenren.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        addEnumKeys();
        addDisplayKeys();
        addCommandKeys();
        addScreenKeys();
        addDeathMessages();
    }

    //  每个 Gu* 枚举都是 GuTranslatable, 所以常量本身就是键 —— 短到能对齐, 这就是那个接口的用处.
    private void add(EnumTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    //  值的部分, HUD 与 /guzhenren info 共用 (见 ModDisplayText); 标签由各自的键补
    private void addDisplayKeys() {
        add("guzhenren.display.realm",                     "%s%s");
        add("guzhenren.display.physique",                  "[%s]");
        add("guzhenren.display.lifespan",                  "%s [%s岁]");
        add("guzhenren.display.base_fraction",             "%s成%s");
        add("guzhenren.display.base_round",                "%s成");
        add("guzhenren.display.base_full",                 "十成");
        add("guzhenren.display.base_tens.2",               "二");
        add("guzhenren.display.base_tens.3",               "三");
        add("guzhenren.display.base_tens.4",               "四");
        add("guzhenren.display.base_tens.5",               "五");
        add("guzhenren.display.base_tens.6",               "六");
        add("guzhenren.display.base_tens.7",               "七");
        add("guzhenren.display.base_tens.8",               "八");
        add("guzhenren.display.base_tens.9",               "九");
        add("guzhenren.display.base_units.1",              "一");
        add("guzhenren.display.base_units.2",              "二");
        add("guzhenren.display.base_units.3",              "三");
        add("guzhenren.display.base_units.4",              "四");
        add("guzhenren.display.base_units.5",              "五");
        add("guzhenren.display.base_units.6",              "六");
        add("guzhenren.display.base_units.7",              "七");
        add("guzhenren.display.base_units.8",              "八");
        add("guzhenren.display.base_units.9",              "九");
        add("guzhenren.display.none",                      "[无]");

        add("guzhenren.hud.lifespan",                      "寿元 %s");
    }
    //endregion

    //region COMMAND
    //  三类反馈: info 默认色 / 成功绿 / 失败红, 全部带 [GZR] 前缀 (见 ModCommandFeedback)
    private void addCommandKeys() {
        add("guzhenren.command.header",                    "[GZR]");
        add("guzhenren.command.tagged",                    "[GZR] %s");
        add("guzhenren.command.updated",                   "已更新 %s 名玩家");
        add("guzhenren.command.unknown_value",             "未知的取值: %s");

        add("guzhenren.command.failed.awakened",           "%s 已开窍 —— 要重掷请先 /guzhenren reset");
        add("guzhenren.command.failed.unawakened",         "%s 尚未开窍 —— 修为相关的值只能由 /guzhenren awaken 建立");
        add("guzhenren.command.failed.qi_mark",            "气道的道痕是诸气之和，不能直接改 —— 请用 /guzhenren body qi <种类>");

        add("guzhenren.command.info.aperture_index",       "第 %s 窍");
        add("guzhenren.command.info.aperture_state",       "空窍状态  %s");
        add("guzhenren.command.info.realm",                "玩家修为  %s");
        add("guzhenren.command.info.talent",               "玩家天赋  %s");
        add("guzhenren.command.info.essence",              "玩家真元  %s / %s");
        add("guzhenren.command.info.soul",                 "玩家魂魄  %s / %s");
        add("guzhenren.command.info.lifespan",             "玩家寿元  %s");
        add("guzhenren.command.info.life_state",           "肉身状态  %s");
        add("guzhenren.command.info.life_form",            "玩家形态  %s");
        add("guzhenren.command.info.qi",                   "玩家气道  %s");
        add("guzhenren.command.info.qi_entry",             "  %s  %s");
        add("guzhenren.command.info.paths",                "流派造诣");
        add("guzhenren.command.info.path_entry",           "  %s  %s  道痕 %s");
        add("guzhenren.command.info.brilliance",           "才情  %s");
        add("guzhenren.command.info.brilliance_rate",      "%s个念头每秒");
        add("guzhenren.command.info.mind",                 "脑海");
        add("guzhenren.command.info.mind_entry",           "  %s  %s / %s");

        //  给 operator 看的派生细节, 灰字缀在行尾: 资质基数 / 魂魄境界
        add("guzhenren.command.info.detail",               " [%s]");
    }
    //endregion

    //region SCREEN
    //  G 键信息面板 (见 client/screen/PlayerInfoScreen) 与它的键位. 标签紧凑, 值取枚举自己的键
    private void addScreenKeys() {
        add("key.categories.guzhenren",                    "蛊真人");
        add("key.guzhenren.open_info",                     "打开信息面板");

        add("guzhenren.screen.info.title",                 "信息");
        add("guzhenren.screen.tab.aperture",               "空窍");
        add("guzhenren.screen.tab.body",                   "肉身");
        add("guzhenren.screen.tab.mind",                   "脑海");
        add("guzhenren.screen.label.realm",                "修为");
        add("guzhenren.screen.label.talent",               "天赋");
        add("guzhenren.screen.label.essence",              "真元");
        add("guzhenren.screen.label.state",                "状态");
        add("guzhenren.screen.label.life_form",            "形态");
        add("guzhenren.screen.label.soul",                 "魂魄");
        add("guzhenren.screen.label.lifespan",             "寿元");
        add("guzhenren.screen.label.qi",                   "气道造诣");
        add("guzhenren.screen.label.paths",                "流派造诣");
        add("guzhenren.screen.label.brilliance",           "才情");
        add("guzhenren.screen.path_value",                 "%s 道痕 %s");
    }
    //endregion

    //region DEATH
    //  键名由 DamageType 的 msgId 决定: msgId "guzhenren.xxx" -> "death.attack.guzhenren.xxx"
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted",   "%1$s 寿元耗尽而亡");
        add("death.attack.guzhenren.soul_collapse",        "%1$s 魂魄衰竭而亡");
        add("death.attack.guzhenren.mind_ocean_shattered", "%1$s 脑海炸裂而亡");
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
        add(ApertureState.ALIVE, "生");
        add(ApertureState.DEAD,  "死");
    }

    private void addRank() {
        add(Rank.NONE,  "凡人");
        add(Rank.ONE,   "一转");
        add(Rank.TWO,   "二转");
        add(Rank.THREE, "三转");
        add(Rank.FOUR,  "四转");
        add(Rank.FIVE,  "五转");
        add(Rank.SIX,   "六转");
        add(Rank.SEVEN, "七转");
        add(Rank.EIGHT, "八转");
        add(Rank.NINE,  "九转");
    }

    private void addStage() {
        //  故意留空: 未开窍时不显示
        add(Stage.NONE,   "");
        add(Stage.INIT,   "初阶");
        add(Stage.MIDDLE, "中阶");
        add(Stage.UPPER,  "高阶");
        add(Stage.PEAK,   "巅峰");
    }

    private void addTalent() {
        add(Talent.EXTREME, "十绝天资");
        add(Talent.FIRST,   "甲等资质");
        add(Talent.SECOND,  "乙等资质");
        add(Talent.THIRD,   "丙等资质");
        add(Talent.FOURTH,  "丁等资质");
        add(Talent.NONE,    "未觉醒");
    }

    private void addLifeForm() {
        add(LifeForm.MORTAL,   "凡");
        add(LifeForm.IMMORTAL, "仙");
    }

    private void addLifeState() {
        add(LifeState.ALIVE,  "生");
        add(LifeState.ZOMBIE, "僵");
        add(LifeState.DEAD,   "死");
    }

    //  真元色, 每转一色; 同转内的深浅由 EssenceColor.shade 按小境界算, 不另出文案
    private void addEssenceColor() {
        add(EssenceColor.NONE,           "无");
        add(EssenceColor.GREEN_COPPER,   "青铜色");
        add(EssenceColor.RED_STEEL,      "赤铁色");
        add(EssenceColor.WHITE_SILVER,   "白银色");
        add(EssenceColor.YELLOW_GOLDEN,  "黄金色");
        add(EssenceColor.PURPLE_CRYSTAL, "紫晶色");
        add(EssenceColor.GREEN_GRAPE,    "青提");
        add(EssenceColor.RED_DATE,       "红枣");
        add(EssenceColor.WHITE_LITCHI,   "白荔");
        add(EssenceColor.YELLOW_APRICOT, "黄杏");
    }

    private void addTenExtreme() {
        //  故意留空: 非十绝体玩家不显示
        add(ExtremePhysique.NONE,                               "");
        add(ExtremePhysique.VERDANT_GREAT_SUN,                  "太日阳莽体");
        add(ExtremePhysique.DESOLATE_ANCIENT_MOON,              "古月阴荒体");
        add(ExtremePhysique.NORTHERN_DARK_ICE_SOUL,             "北冥冰魄体");
        add(ExtremePhysique.BOUNDLESS_FOREST_SAMSARA,           "森海轮回体");
        add(ExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE, "炎煌雷泽体");
        add(ExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE,       "万金妙华体");
        add(ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL,        "大力真武体");
        add(ExtremePhysique.CAREFREE_WISDOM_HEART,              "逍遥智心体");
        add(ExtremePhysique.PROFOUND_EARTH_ORIGIN,              "厚土元央体");
        add(ExtremePhysique.UNIVERSE_GREAT_DERIVATION,          "宇宙大衍体");
        add(ExtremePhysique.PURE_DREAM_REALITY_SEEKER,          "纯梦求真体");
    }

    //  天地人三气是升仙的门槛; 自然气无效用. 诸气之和即气道的道痕 (不另存, 见 QiData)
    private void addQiType() {
        add(QiType.HEAVEN,  "天气");
        add(QiType.EARTH,   "地气");
        add(QiType.HUMAN,   "人气");
        add(QiType.NATURAL, "自然气");
    }

    private void addPath() {
        add(GuPath.HEAVEN,         "天道");
        add(GuPath.RULE,           "律道");
        add(GuPath.SPACE,          "宇道");
        add(GuPath.TIME,           "宙道");
        add(GuPath.HUMAN,          "人道");
        add(GuPath.METAL,          "金道");
        add(GuPath.WOOD,           "木道");
        add(GuPath.WATER,          "水道");
        add(GuPath.FIRE,           "火道");
        add(GuPath.EARTH,          "土道");
        add(GuPath.ICE_SNOW,       "冰雪道");
        add(GuPath.LIGHTNING,      "雷道");
        add(GuPath.QI,             "气道");
        add(GuPath.SOUND,          "音道");
        add(GuPath.LIGHT,          "光道");
        add(GuPath.DARK,           "暗道");
        add(GuPath.STRENGTH,       "力道");
        add(GuPath.DREAM,          "梦道");
        add(GuPath.REFINEMENT,     "炼道");
        add(GuPath.WISDOM,         "智道");
        add(GuPath.THEFT,          "偷道");
        add(GuPath.LUCK,           "运道");
        add(GuPath.KILLING,        "杀道");
        add(GuPath.BLOOD,          "血道");
        add(GuPath.SOUL,           "魂道");
        add(GuPath.ENSLAVEMENT,    "奴道");
        add(GuPath.FOOD,           "食道");
        add(GuPath.FORMATION,      "阵道");
        add(GuPath.PAINTING,       "画道");
        add(GuPath.TRANSFORMATION, "变化道");
    }

    private void addAttainment() {
        add(GuAttainment.NONE,                      "无");
        add(GuAttainment.ORDINARY,                  "普通");
        add(GuAttainment.QUASI_MASTER,              "准大师");
        add(GuAttainment.MASTER,                    "大师");
        add(GuAttainment.QUASI_GRANDMASTER,         "准宗师");
        add(GuAttainment.GRANDMASTER,               "宗师");
        add(GuAttainment.QUASI_GREAT_GRANDMASTER,   "准大宗师");
        add(GuAttainment.GREAT_GRANDMASTER,         "大宗师");
        add(GuAttainment.QUASI_SUPREME_GRANDMASTER, "准无上大宗师");
        add(GuAttainment.SUPREME_GRANDMASTER,       "无上大宗师");
    }

    //  魂魄境界: 由魂魄值反查 (魂魄值 = 人数 × 100), 不单独存
    private void addSoulTier() {
        add(SoulTier.ONE,              "一人魂");
        add(SoulTier.TEN,              "十人魂");
        add(SoulTier.HUNDRED,          "百人魂");
        add(SoulTier.THOUSAND,         "千人魂");
        add(SoulTier.TEN_THOUSAND,     "万人魂");
        add(SoulTier.HUNDRED_THOUSAND, "十万人魂");
        add(SoulTier.MILLION,          "百万人魂");
        add(SoulTier.TEN_MILLION,      "千万人魂");
        add(SoulTier.HUNDRED_MILLION,  "亿人魂");
    }

    //  智道三态; 脑海初始容量 念30000 / 意5 / 情2, 装不下即脑海炸裂
    private void addWisdomType() {
        add(WisdomType.THOUGHTS, "念");
        add(WisdomType.WILLS,    "意");
        add(WisdomType.EMOTIONS, "情");
    }

    //  才情: 念的自然恢复速度 (每秒 1 / 4 / 16 / 64 / 256), 开窍时抽取
    private void addBrilliance() {
        add(Brilliance.ORDINARY,    "才情普通");
        add(Brilliance.DECENT,      "才情尚可");
        add(Brilliance.DISTINCTIVE, "才情不俗");
        add(Brilliance.OUTSTANDING, "才情卓越");
        add(Brilliance.UNRIVALED,   "才情旷世");
    }
    //endregion
}
