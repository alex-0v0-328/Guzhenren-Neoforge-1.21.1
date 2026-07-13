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

public class ZhCnLanguageProvider extends LanguageProvider {
    public ZhCnLanguageProvider(PackOutput output) {
        super(output, Guzhenren.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        addEnumKeys();
        addCommandKeys();
        addDeathMessages();
    }

    //region COMMAND
    private void addCommandKeys() {
        add("guzhenren.command.updated", "已更新 %s 名玩家");
        add("guzhenren.command.unknown_value", "未知的取值: %s");

        add("guzhenren.command.info.header", "==== %s ====");
        add("guzhenren.command.info.realm", "境界: %s%s (%s)");
        add("guzhenren.command.info.talent", "资质: %s (基数 %s)");
        add("guzhenren.command.info.physique", "体质: %s");
        add("guzhenren.command.info.essence", "真元: %s / %s");
        add("guzhenren.command.info.lifespan", "寿元: 年龄 %s, 剩余 %s");
        add("guzhenren.command.info.soul", "魂魄: %s  %s / %s");
        add("guzhenren.command.info.paths", "流派:");
        add("guzhenren.command.info.no_path", "流派: 无");
        add("guzhenren.command.info.path_entry", "  %s  %s  道痕 %s");
    }
    //endregion

    //region DEATH
    //  键名由 DamageType 的 msgId 决定: msgId "guzhenren.xxx" -> "death.attack.guzhenren.xxx"
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted", "%1$s 寿元耗尽而亡");
        add("death.attack.guzhenren.soul_collapse", "%1$s 魂魄衰竭而亡");
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
        add(GuSoulTier.ONE.getTranslationKey(), "一人魂");
        add(GuSoulTier.TEN.getTranslationKey(), "十人魂");
        add(GuSoulTier.HUNDRED.getTranslationKey(), "百人魂");
        add(GuSoulTier.THOUSAND.getTranslationKey(), "千人魂");
        add(GuSoulTier.TEN_THOUSAND.getTranslationKey(), "万人魂");
        add(GuSoulTier.HUNDRED_THOUSAND.getTranslationKey(), "十万人魂");
        add(GuSoulTier.MILLION.getTranslationKey(), "百万人魂");
        add(GuSoulTier.TEN_MILLION.getTranslationKey(), "千万人魂");
        add(GuSoulTier.HUNDRED_MILLION.getTranslationKey(), "亿人魂");
    }

    //  真元色, 每转一色; 同转内的深浅由 GuEssenceColor.shade 按小境界算, 不另出文案
    private void addEssenceColor() {
        add(GuEssenceColor.NONE.getTranslationKey(), "无");
        add(GuEssenceColor.GREEN_COPPER.getTranslationKey(), "青铜色");
        add(GuEssenceColor.RED_STEEL.getTranslationKey(), "赤铁色");
        add(GuEssenceColor.WHITE_SILVER.getTranslationKey(), "白银色");
        add(GuEssenceColor.YELLOW_GOLDEN.getTranslationKey(), "黄金色");
        add(GuEssenceColor.PURPLE_CRYSTAL.getTranslationKey(), "紫晶色");
        add(GuEssenceColor.GREEN_GRAPE.getTranslationKey(), "青提");
        add(GuEssenceColor.RED_DATE.getTranslationKey(), "红枣");
        add(GuEssenceColor.WHITE_LITCHI.getTranslationKey(), "白荔");
        add(GuEssenceColor.YELLOW_APRICOT.getTranslationKey(), "黄杏");
    }

    private void addRank() {
        add(GuRank.NONE.getTranslationKey(), "凡人");
        add(GuRank.ONE.getTranslationKey(), "一转");
        add(GuRank.TWO.getTranslationKey(), "二转");
        add(GuRank.THREE.getTranslationKey(), "三转");
        add(GuRank.FOUR.getTranslationKey(), "四转");
        add(GuRank.FIVE.getTranslationKey(), "五转");
        add(GuRank.SIX.getTranslationKey(), "六转");
        add(GuRank.SEVEN.getTranslationKey(), "七转");
        add(GuRank.EIGHT.getTranslationKey(), "八转");
        add(GuRank.NINE.getTranslationKey(), "九转");
    }

    private void addStage() {
        //  故意留空: 未开窍时不显示
        add(GuStage.NONE.getTranslationKey(), "");
        add(GuStage.INIT.getTranslationKey(), "初阶");
        add(GuStage.MIDDLE.getTranslationKey(), "中阶");
        add(GuStage.UPPER.getTranslationKey(), "高阶");
        add(GuStage.PEAK.getTranslationKey(), "巅峰");
    }

    private void addTalent() {
        add(GuTalent.EXTREME.getTranslationKey(), "十绝体");
        add(GuTalent.FIRST.getTranslationKey(), "甲等");
        add(GuTalent.SECOND.getTranslationKey(), "乙等");
        add(GuTalent.THIRD.getTranslationKey(), "丙等");
        add(GuTalent.FOURTH.getTranslationKey(), "丁等");
        add(GuTalent.NONE.getTranslationKey(), "未觉醒");
    }

    private void addLifeForm() {
        add(GuLifeForm.MORTAL.getTranslationKey(), "凡");
        add(GuLifeForm.IMMORTAL.getTranslationKey(), "仙");
    }

    private void addLifeState() {
        add(GuLifeState.ALIVE.getTranslationKey(), "生");
        add(GuLifeState.ZOMBIE.getTranslationKey(), "僵");
        add(GuLifeState.DEAD.getTranslationKey(), "死");
    }

    private void addTenExtreme() {
        //  故意留空: 非十绝体玩家不显示
        add(GuExtremePhysique.NONE.getTranslationKey(), "");
        add(GuExtremePhysique.VERDANT_GREAT_SUN.getTranslationKey(), "太日阳莽体");
        add(GuExtremePhysique.DESOLATE_ANCIENT_MOON.getTranslationKey(), "古月阴荒体");
        add(GuExtremePhysique.NORTHERN_DARK_ICE_SOUL.getTranslationKey(), "北冥冰魄体");
        add(GuExtremePhysique.BOUNDLESS_FOREST_SAMSARA.getTranslationKey(), "森海轮回体");
        add(GuExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE.getTranslationKey(), "炎煌雷泽体");
        add(GuExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE.getTranslationKey(), "万金妙华体");
        add(GuExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL.getTranslationKey(), "大力真武体");
        add(GuExtremePhysique.CAREFREE_WISDOM_HEART.getTranslationKey(), "逍遥智心体");
        add(GuExtremePhysique.PROFOUND_EARTH_ORIGIN.getTranslationKey(), "厚土元央体");
        add(GuExtremePhysique.UNIVERSE_GREAT_DERIVATION.getTranslationKey(), "宇宙大衍体");
        add(GuExtremePhysique.PURE_DREAM_REALITY_SEEKER.getTranslationKey(), "纯梦求真体");
    }

    private void addPath() {
        add(GuPath.HEAVEN.getTranslationKey(), "天道");
        add(GuPath.RULE.getTranslationKey(), "律道");
        add(GuPath.SPACE.getTranslationKey(), "宇道");
        add(GuPath.TIME.getTranslationKey(), "宙道");
        add(GuPath.HUMAN.getTranslationKey(), "人道");
        add(GuPath.METAL.getTranslationKey(), "金道");
        add(GuPath.WOOD.getTranslationKey(), "木道");
        add(GuPath.WATER.getTranslationKey(), "水道");
        add(GuPath.FIRE.getTranslationKey(), "火道");
        add(GuPath.EARTH.getTranslationKey(), "土道");
        add(GuPath.ICE_SNOW.getTranslationKey(), "冰雪道");
        add(GuPath.LIGHTNING.getTranslationKey(), "雷道");
        add(GuPath.QI.getTranslationKey(), "气道");
        add(GuPath.SOUND.getTranslationKey(), "音道");
        add(GuPath.LIGHT.getTranslationKey(), "光道");
        add(GuPath.DARK.getTranslationKey(), "暗道");
        add(GuPath.STRENGTH.getTranslationKey(), "力道");
        add(GuPath.DREAM.getTranslationKey(), "梦道");
        add(GuPath.REFINEMENT.getTranslationKey(), "炼道");
        add(GuPath.WISDOM.getTranslationKey(), "智道");
        add(GuPath.THEFT.getTranslationKey(), "偷道");
        add(GuPath.LUCK.getTranslationKey(), "运道");
        add(GuPath.KILLING.getTranslationKey(), "杀道");
        add(GuPath.BLOOD.getTranslationKey(), "血道");
        add(GuPath.SOUL.getTranslationKey(), "魂道");
        add(GuPath.ENSLAVEMENT.getTranslationKey(), "奴道");
        add(GuPath.FOOD.getTranslationKey(), "食道");
        add(GuPath.FORMATION.getTranslationKey(), "阵道");
        add(GuPath.PAINTING.getTranslationKey(), "画道");
        add(GuPath.TRANSFORMATION.getTranslationKey(), "变化道");
    }

    private void addAttainment() {
        add(GuPathAttainment.NONE.getTranslationKey(), "无");
        add(GuPathAttainment.ORDINARY.getTranslationKey(), "普通");
        add(GuPathAttainment.QUASI_MASTER.getTranslationKey(), "准大师");
        add(GuPathAttainment.MASTER.getTranslationKey(), "大师");
        add(GuPathAttainment.QUASI_GRANDMASTER.getTranslationKey(), "准宗师");
        add(GuPathAttainment.GRANDMASTER.getTranslationKey(), "宗师");
        add(GuPathAttainment.QUASI_GREAT_GRANDMASTER.getTranslationKey(), "准大宗师");
        add(GuPathAttainment.GREAT_GRANDMASTER.getTranslationKey(), "大宗师");
        add(GuPathAttainment.QUASI_SUPREME_GRANDMASTER.getTranslationKey(), "准无上大宗师");
        add(GuPathAttainment.SUPREME_GRANDMASTER.getTranslationKey(), "无上大宗师");
    }
    //endregion
}
