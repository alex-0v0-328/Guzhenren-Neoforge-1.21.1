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
import com.unknown.guzhenren.custom.enums.wisdom.GuBrilliance;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
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
        addDeathMessages();
    }

    //  每个 Gu* 枚举都是 GuTranslatable, 所以常量本身就是键 —— 短到能对齐, 这就是那个接口的用处.
    private void add(EnumTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    //  值的部分, HUD 与 /guzhenren info 共用 (见 ModDisplayText); 标签由各自的键补
    private void addDisplayKeys() {
        add("guzhenren.display.realm",                     "%s%s");
        add("guzhenren.display.physique",                  "[ %s ]");
        add("guzhenren.display.lifespan",                  "%s [ %s岁 ]");

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

        add("guzhenren.command.info.realm",                "玩家修为：%s");
        add("guzhenren.command.info.talent",               "玩家天赋：%s");
        add("guzhenren.command.info.essence",              "玩家真元：%s / %s");
        add("guzhenren.command.info.soul",                 "玩家魂魄：%s / %s");
        add("guzhenren.command.info.lifespan",             "玩家寿元：%s");
        add("guzhenren.command.info.life_state",           "玩家状态：%s");
        add("guzhenren.command.info.paths",                "玩家流派：");
        add("guzhenren.command.info.path_entry",           "  %s  %s  道痕 %s");
        add("guzhenren.command.info.path_empty",           "  无");
        add("guzhenren.command.info.brilliance",           "玩家才情：%s");
        add("guzhenren.command.info.brilliance_rate",      "%s 念/秒");
        add("guzhenren.command.info.mind",                 "玩家脑海：");
        add("guzhenren.command.info.mind_entry",           "  %s  %s / %s");

        //  给 operator 看的派生细节, 灰字缀在行尾: 资质基数 / 魂魄境界
        add("guzhenren.command.info.detail",               " (%s)");
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
        addLifeForm();
        addLifeState();
        addEssenceColor();
        addTenExtreme();
        addPath();
        addAttainment();
        addSoulTier();
        addWisdomType();
        addBrilliance();
    }

    private void addRank() {
        add(GuRank.NONE,  "凡人");
        add(GuRank.ONE,   "一转");
        add(GuRank.TWO,   "二转");
        add(GuRank.THREE, "三转");
        add(GuRank.FOUR,  "四转");
        add(GuRank.FIVE,  "五转");
        add(GuRank.SIX,   "六转");
        add(GuRank.SEVEN, "七转");
        add(GuRank.EIGHT, "八转");
        add(GuRank.NINE,  "九转");
    }

    private void addStage() {
        //  故意留空: 未开窍时不显示
        add(GuStage.NONE,   "");
        add(GuStage.INIT,   "初阶");
        add(GuStage.MIDDLE, "中阶");
        add(GuStage.UPPER,  "高阶");
        add(GuStage.PEAK,   "巅峰");
    }

    private void addTalent() {
        add(GuTalent.EXTREME, "十绝天资");
        add(GuTalent.FIRST,   "甲等资质");
        add(GuTalent.SECOND,  "乙等资质");
        add(GuTalent.THIRD,   "丙等资质");
        add(GuTalent.FOURTH,  "丁等资质");
        add(GuTalent.NONE,    "未觉醒");
    }

    private void addLifeForm() {
        add(GuLifeForm.MORTAL,   "凡");
        add(GuLifeForm.IMMORTAL, "仙");
    }

    private void addLifeState() {
        add(GuLifeState.ALIVE,  "生");
        add(GuLifeState.ZOMBIE, "僵");
        add(GuLifeState.DEAD,   "死");
    }

    //  真元色, 每转一色; 同转内的深浅由 GuEssenceColor.shade 按小境界算, 不另出文案
    private void addEssenceColor() {
        add(GuEssenceColor.NONE,           "无");
        add(GuEssenceColor.GREEN_COPPER,   "青铜色");
        add(GuEssenceColor.RED_STEEL,      "赤铁色");
        add(GuEssenceColor.WHITE_SILVER,   "白银色");
        add(GuEssenceColor.YELLOW_GOLDEN,  "黄金色");
        add(GuEssenceColor.PURPLE_CRYSTAL, "紫晶色");
        add(GuEssenceColor.GREEN_GRAPE,    "青提");
        add(GuEssenceColor.RED_DATE,       "红枣");
        add(GuEssenceColor.WHITE_LITCHI,   "白荔");
        add(GuEssenceColor.YELLOW_APRICOT, "黄杏");
    }

    private void addTenExtreme() {
        //  故意留空: 非十绝体玩家不显示
        add(GuExtremePhysique.NONE,                               "");
        add(GuExtremePhysique.VERDANT_GREAT_SUN,                  "太日阳莽体");
        add(GuExtremePhysique.DESOLATE_ANCIENT_MOON,              "古月阴荒体");
        add(GuExtremePhysique.NORTHERN_DARK_ICE_SOUL,             "北冥冰魄体");
        add(GuExtremePhysique.BOUNDLESS_FOREST_SAMSARA,           "森海轮回体");
        add(GuExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE, "炎煌雷泽体");
        add(GuExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE,       "万金妙华体");
        add(GuExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL,        "大力真武体");
        add(GuExtremePhysique.CAREFREE_WISDOM_HEART,              "逍遥智心体");
        add(GuExtremePhysique.PROFOUND_EARTH_ORIGIN,              "厚土元央体");
        add(GuExtremePhysique.UNIVERSE_GREAT_DERIVATION,          "宇宙大衍体");
        add(GuExtremePhysique.PURE_DREAM_REALITY_SEEKER,          "纯梦求真体");
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
        add(GuPathAttainment.NONE,                      "无");
        add(GuPathAttainment.ORDINARY,                  "普通");
        add(GuPathAttainment.QUASI_MASTER,              "准大师");
        add(GuPathAttainment.MASTER,                    "大师");
        add(GuPathAttainment.QUASI_GRANDMASTER,         "准宗师");
        add(GuPathAttainment.GRANDMASTER,               "宗师");
        add(GuPathAttainment.QUASI_GREAT_GRANDMASTER,   "准大宗师");
        add(GuPathAttainment.GREAT_GRANDMASTER,         "大宗师");
        add(GuPathAttainment.QUASI_SUPREME_GRANDMASTER, "准无上大宗师");
        add(GuPathAttainment.SUPREME_GRANDMASTER,       "无上大宗师");
    }

    //  魂魄境界: 由魂魄值反查 (魂魄值 = 人数 × 100), 不单独存
    private void addSoulTier() {
        add(GuSoulTier.ONE,              "一人魂");
        add(GuSoulTier.TEN,              "十人魂");
        add(GuSoulTier.HUNDRED,          "百人魂");
        add(GuSoulTier.THOUSAND,         "千人魂");
        add(GuSoulTier.TEN_THOUSAND,     "万人魂");
        add(GuSoulTier.HUNDRED_THOUSAND, "十万人魂");
        add(GuSoulTier.MILLION,          "百万人魂");
        add(GuSoulTier.TEN_MILLION,      "千万人魂");
        add(GuSoulTier.HUNDRED_MILLION,  "亿人魂");
    }

    //  智道三态; 脑海初始容量 念30000 / 意5 / 情2, 装不下即脑海炸裂
    private void addWisdomType() {
        add(GuWisdomType.THOUGHTS, "念");
        add(GuWisdomType.WILLS,    "意");
        add(GuWisdomType.EMOTIONS, "情");
    }

    //  才情: 念的自然恢复速度 (每秒 1 / 4 / 16 / 64 / 256), 开窍时抽取
    private void addBrilliance() {
        add(GuBrilliance.ORDINARY,    "才情普通");
        add(GuBrilliance.DECENT,      "才情尚可");
        add(GuBrilliance.DISTINCTIVE, "才情不俗");
        add(GuBrilliance.OUTSTANDING, "才情卓越");
        add(GuBrilliance.UNRIVALED,   "才情旷世");
    }
    //endregion
}
