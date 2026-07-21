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
        addItemKeys();
        addDeathMessages();
    }

    //  Every enum here is EnumTranslatable, so the constant IS the key -- short enough to align,
    //  which is exactly what that interface is for.
    private void add(EnumTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    //  The values, shared by the HUD and /guzhenren info (see ModDisplayText); each caller adds its label
    private void addDisplayKeys() {
        add("guzhenren.display.realm", "%s%s");
        add("guzhenren.display.gu_line", "%s%s%s");
        add("guzhenren.display.gu", "蛊虫");
        add("guzhenren.display.gu_material", "蛊材");
        add("guzhenren.display.physique", "[%s]");
        add("guzhenren.display.lifespan", "%s [%s岁]");
        add("guzhenren.display.base_fraction", "%s成%s");
        add("guzhenren.display.base_round", "%s成");
        add("guzhenren.display.base_full", "十成");
        add("guzhenren.display.base_tens.2", "二");
        add("guzhenren.display.base_tens.3", "三");
        add("guzhenren.display.base_tens.4", "四");
        add("guzhenren.display.base_tens.5", "五");
        add("guzhenren.display.base_tens.6", "六");
        add("guzhenren.display.base_tens.7", "七");
        add("guzhenren.display.base_tens.8", "八");
        add("guzhenren.display.base_tens.9", "九");
        add("guzhenren.display.base_units.1", "一");
        add("guzhenren.display.base_units.2", "二");
        add("guzhenren.display.base_units.3", "三");
        add("guzhenren.display.base_units.4", "四");
        add("guzhenren.display.base_units.5", "五");
        add("guzhenren.display.base_units.6", "六");
        add("guzhenren.display.base_units.7", "七");
        add("guzhenren.display.base_units.8", "八");
        add("guzhenren.display.base_units.9", "九");
        add("guzhenren.display.none", "[无]");
        add("guzhenren.display.wild", "野生·%s");
        add("guzhenren.display.vital", "本命·%s");
        add("guzhenren.display.boar_strength.1", "[一猪之力]");
        add("guzhenren.display.boar_strength.2", "[两猪之力]");
        add("guzhenren.display.jun_strength.jin.1", "[一斤之力]");
        add("guzhenren.display.jun_strength.jin.2", "[二斤之力]");
        add("guzhenren.display.jun_strength.jin.3", "[三斤之力]");
        add("guzhenren.display.jun_strength.jin.4", "[四斤之力]");
        add("guzhenren.display.jun_strength.jin.5", "[五斤之力]");
        add("guzhenren.display.jun_strength.jin.6", "[六斤之力]");
        add("guzhenren.display.jun_strength.jin.7", "[七斤之力]");
        add("guzhenren.display.jun_strength.jin.8", "[八斤之力]");
        add("guzhenren.display.jun_strength.jin.9", "[九斤之力]");

        add("guzhenren.hud.lifespan", "寿元 %s");
        add("guzhenren.hud.refining", "炼化中  %s / %s");
        add("guzhenren.hud.using", "使用中  %s / %s");
    }
    //endregion

    //region COMMAND
    //  Three feedback classes: info default / success green / failure red, all tagged [GZR]
    //  (see ModCommandFeedback)
    private void addCommandKeys() {
        add("guzhenren.command.header", "[GZR]");
        add("guzhenren.command.tagged", "[GZR] %s");
        add("guzhenren.command.updated", "已更新 %s 名玩家");
        add("guzhenren.command.unknown_value", "未知的取值: %s");

        add("guzhenren.command.failed.awakened", "%s 已开窍 —— 要重掷请先 /guzhenren reset");
        add("guzhenren.command.failed.unawakened", "%s 尚未开窍 —— 修为相关的值只能由 /guzhenren awaken 建立");
        add("guzhenren.command.failed.qi_mark", "气道的道痕是诸气之和，不能直接改 —— 请用 /guzhenren body qi <种类>");

        add("guzhenren.command.info.aperture_index", "第 %s 窍");
        add("guzhenren.command.info.aperture_state", "空窍状态  %s");
        add("guzhenren.command.info.realm", "玩家修为  %s");
        add("guzhenren.command.info.talent", "玩家天赋  %s");
        add("guzhenren.command.info.essence", "玩家真元  %s / %s");
        add("guzhenren.command.info.primary_path", "主修流派  %s");
        add("guzhenren.command.info.secondary_path", "辅修流派  %s");
        add("guzhenren.command.info.soul", "玩家魂魄  %s / %s");
        add("guzhenren.command.info.lifespan", "玩家寿元  %s");
        add("guzhenren.command.info.life_state", "肉身状态  %s");
        add("guzhenren.command.info.life_form", "玩家形态  %s");
        add("guzhenren.command.info.qi", "玩家气道  %s");
        add("guzhenren.command.info.qi_total", " 道痕 %s");
        add("guzhenren.command.info.qi_entry", "  %s  %s");
        add("guzhenren.command.info.paths", "流派造诣");
        add("guzhenren.command.info.path_entry", "  %s  %s  道痕 %s");
        add("guzhenren.command.info.path_speck", " 碎屑 %s");
        add("guzhenren.command.info.strength", "力道造诣");
        add("guzhenren.command.info.strength_entry", "  %s  %s");
        add("guzhenren.command.info.brilliance", "才情  %s");
        add("guzhenren.command.info.brilliance_rate", "%s个念头每秒");
        add("guzhenren.command.info.mind", "脑海");
        add("guzhenren.command.info.mind_entry", "  %s  %s / %s");

        //  Derived detail for operators, gray at the end of a line: aptitude base / soul tier
        add("guzhenren.command.info.detail", " [%s]");
    }
    //endregion

    //region SCREEN
    //  The G-key info panel (see client/screen/PlayerInfoScreen) and its keybind. Labels stay terse;
    //  the values come from the enums' own keys
    private void addScreenKeys() {
        add("key.categories.guzhenren", "蛊真人");
        add("key.guzhenren.open_info", "打开信息面板");

        add("guzhenren.screen.info.title", "信息");
        add("guzhenren.screen.tab.aperture", "空窍");
        add("guzhenren.screen.tab.body", "肉身");
        add("guzhenren.screen.tab.mind", "脑海");
        add("guzhenren.screen.tab.storage", "空窍存储");
        add("guzhenren.menu.aperture_storage", "空窍存储");
        add("guzhenren.menu.vital", "本命");
        add("guzhenren.screen.label.primary_path", "主修流派");
        add("guzhenren.screen.label.secondary_path", "辅修流派");
        add("guzhenren.screen.pick.title", "选择辅修流派");
        add("guzhenren.screen.pick.hint", "点击选择");
        add("guzhenren.screen.label.realm", "修为");
        add("guzhenren.screen.label.talent", "天赋");
        add("guzhenren.screen.label.essence", "真元");
        add("guzhenren.screen.label.state", "状态");
        add("guzhenren.screen.label.life_form", "形态");
        add("guzhenren.screen.label.soul", "魂魄");
        add("guzhenren.screen.label.lifespan", "寿元");
        add("guzhenren.screen.label.qi", "气道造诣");
        add("guzhenren.screen.label.paths", "流派造诣");
        add("guzhenren.screen.label.strength", "力道造诣");
        add("guzhenren.screen.label.brilliance", "才情");
        add("guzhenren.screen.path_value", "%s 道痕 %s");
    }
    //endregion

    //region ITEM
    //  A failure here addresses "you", so the command's keys don't fit -- those name a target.
    //  Nothing is said on success: the essence bar is the one that speaks
    private void addItemKeys() {
        add("item.guzhenren.hope_gu", "希望蛊");
        add("item.guzhenren.copper_relics_gu", "青铜舍利蛊");
        add("item.guzhenren.steel_relics_gu", "赤铁舍利蛊");
        add("item.guzhenren.silver_relics_gu", "白银舍利蛊");
        add("item.guzhenren.gold_relics_gu", "黄金舍利蛊");
        add("item.guzhenren.crystal_relics_gu", "紫晶舍利蛊");
        add("item.guzhenren.white_boar_gu", "白豕蛊");
        add("item.guzhenren.black_boar_gu", "黑豕蛊");
        add("item.guzhenren.jin_strength_gu", "斤力蛊");
        add("item.guzhenren.vitality_leaf_gu", "生机叶蛊");
        add("item.guzhenren.primeval_stone", "元石");

        add("itemGroup.guzhenren.mortal_gu", "凡蛊");
        add("itemGroup.guzhenren.gu_material", "蛊材");

        add("guzhenren.item.failed.awakened", "你已开窍");
        add("guzhenren.item.failed.unawakened", "你未开窍");
        add("guzhenren.item.failed.essence_full", "真元已满");
        add("guzhenren.item.failed.rank_mismatch", "境界不符 — 此蛊需%s");
        add("guzhenren.item.failed.stage_peak", "已至小境界巅峰");
        add("guzhenren.item.failed.beast_strength_held", "已有%s之力");
        add("guzhenren.item.failed.jun_strength_full", "已有九斤之力");
        add("guzhenren.item.failed.vitality_active", "生机叶效果未散");
        add("guzhenren.item.failed.refine_essence", "真元不足无法炼化");

        add("guzhenren.item.gu.uses", "已用 %s/%s");
        add("guzhenren.item.gu.refine_progress", "炼化 %s/%s");
        add("guzhenren.item.gu.hungry", "%s饿了");
        add("guzhenren.item.gu.starved", "%s饿死了");
        add("guzhenren.item.gu.exhausted", "%s被强行催动，力竭而亡！");
        add("guzhenren.item.gu.vital_lost", "%s断绝，气血、魂魄与念头俱损！");

        add("effect.guzhenren.vitality_leaf", "生机叶");
    }
    //endregion

    //region DEATH
    //  The key is decided by the DamageType's msgId: msgId "guzhenren.xxx" -> "death.attack.guzhenren.xxx"
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted", "%1$s 寿元耗尽而亡");
        add("death.attack.guzhenren.soul_collapse", "%1$s 魂魄衰竭而亡");
        add("death.attack.guzhenren.mind_ocean_shattered", "%1$s 脑海炸裂而亡");
        add("death.attack.guzhenren.vital_gu_lost", "%1$s 本命蛊断绝而亡");
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
        add(BeastStrength.WHITE_BOAR, "白豕");
        add(BeastStrength.BLACK_BOAR, "黑豕");
    }

    //  The Strength Path's three branches. Titles today; effects come later, and ENVIRONMENT has no data
    private void addStrengthBranch() {
        add(StrengthBranch.HUMAN, "人力钧力流");
        add(StrengthBranch.BEASTS, "兽力虚影流");
        add(StrengthBranch.ENVIRONMENT, "气象天地流");
    }

    //  The Human Jun branch's kinds. Ten Jin and Hundred Jin each become their own later.
    private void addJunStrength() {
        add(JunStrength.JIN, "斤");
    }

    //  An aperture is only alive or dead; the body's LifeState is the one with a third state, zombie
    private void addApertureState() {
        add(ApertureState.ALIVE, "生");
        add(ApertureState.DEAD, "死");
    }

    private void addRank() {
        add(Rank.NONE, "凡人");
        add(Rank.ONE, "一转");
        add(Rank.TWO, "二转");
        add(Rank.THREE, "三转");
        add(Rank.FOUR, "四转");
        add(Rank.FIVE, "五转");
        add(Rank.SIX, "六转");
        add(Rank.SEVEN, "七转");
        add(Rank.EIGHT, "八转");
        add(Rank.NINE, "九转");
    }

    private void addStage() {
        //  Deliberately blank: nothing is shown while unawakened
        add(Stage.NONE, "");
        add(Stage.INIT, "初阶");
        add(Stage.MIDDLE, "中阶");
        add(Stage.UPPER, "高阶");
        add(Stage.PEAK, "巅峰");
    }

    private void addTalent() {
        add(Talent.EXTREME, "十绝天资");
        add(Talent.FIRST, "甲等资质");
        add(Talent.SECOND, "乙等资质");
        add(Talent.THIRD, "丙等资质");
        add(Talent.FOURTH, "丁等资质");
        add(Talent.NONE, "未觉醒");
    }

    private void addLifeForm() {
        add(LifeForm.MORTAL, "凡");
        add(LifeForm.IMMORTAL, "仙");
    }

    private void addLifeState() {
        add(LifeState.ALIVE, "生");
        add(LifeState.ZOMBIE, "僵");
        add(LifeState.DEAD, "死");
    }

    //  Essence colors, one per rank; the shade within a rank is computed by EssenceColor.shade from the
    //  stage, so it needs no text of its own
    private void addEssenceColor() {
        add(EssenceColor.NONE, "无");
        add(EssenceColor.GREEN_COPPER, "青铜色");
        add(EssenceColor.RED_STEEL, "赤铁色");
        add(EssenceColor.WHITE_SILVER, "白银色");
        add(EssenceColor.YELLOW_GOLDEN, "黄金色");
        add(EssenceColor.PURPLE_CRYSTAL, "紫晶色");
        add(EssenceColor.GREEN_GRAPE, "青提");
        add(EssenceColor.RED_DATE, "红枣");
        add(EssenceColor.WHITE_LITCHI, "白荔");
        add(EssenceColor.YELLOW_APRICOT, "黄杏");
    }

    private void addTenExtreme() {
        //  Deliberately blank: nothing is shown for a player without an extreme physique
        add(ExtremePhysique.NONE, "");
        add(ExtremePhysique.VERDANT_GREAT_SUN, "太日阳莽体");
        add(ExtremePhysique.DESOLATE_ANCIENT_MOON, "古月阴荒体");
        add(ExtremePhysique.NORTHERN_DARK_ICE_SOUL, "北冥冰魄体");
        add(ExtremePhysique.BOUNDLESS_FOREST_SAMSARA, "森海轮回体");
        add(ExtremePhysique.BLAZING_GLORY_LIGHTNING_BRILLIANCE, "炎煌雷泽体");
        add(ExtremePhysique.MYRIAD_GOLD_WONDROUS_ESSENCE, "万金妙华体");
        add(ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL, "大力真武体");
        add(ExtremePhysique.CAREFREE_WISDOM_HEART, "逍遥智心体");
        add(ExtremePhysique.PROFOUND_EARTH_ORIGIN, "厚土元央体");
        add(ExtremePhysique.UNIVERSE_GREAT_DERIVATION, "宇宙大衍体");
        add(ExtremePhysique.PURE_DREAM_REALITY_SEEKER, "纯梦求真体");
    }

    //  Heaven, earth and human qi are the threshold for ascension; natural qi has no effect.
    //  Their sum IS the Qi Path's marks (never stored twice, see QiData)
    private void addQiType() {
        add(QiType.HEAVEN, "天气");
        add(QiType.EARTH, "地气");
        add(QiType.HUMAN, "人气");
        add(QiType.NATURAL, "自然气");
    }

    private void addPath() {
        add(GuPath.HEAVEN, "天道");
        add(GuPath.RULE, "律道");
        add(GuPath.SPACE, "宇道");
        add(GuPath.TIME, "宙道");
        add(GuPath.HUMAN, "人道");
        add(GuPath.METAL, "金道");
        add(GuPath.WOOD, "木道");
        add(GuPath.WATER, "水道");
        add(GuPath.FIRE, "火道");
        add(GuPath.EARTH, "土道");
        add(GuPath.ICE_SNOW, "冰雪道");
        add(GuPath.LIGHTNING, "雷道");
        add(GuPath.QI, "气道");
        add(GuPath.SOUND, "音道");
        add(GuPath.LIGHT, "光道");
        add(GuPath.DARK, "暗道");
        add(GuPath.STRENGTH, "力道");
        add(GuPath.DREAM, "梦道");
        add(GuPath.REFINEMENT, "炼道");
        add(GuPath.WISDOM, "智道");
        add(GuPath.THEFT, "偷道");
        add(GuPath.LUCK, "运道");
        add(GuPath.KILLING, "杀道");
        add(GuPath.BLOOD, "血道");
        add(GuPath.SOUL, "魂道");
        add(GuPath.ENSLAVEMENT, "奴道");
        add(GuPath.FOOD, "食道");
        add(GuPath.FORMATION, "阵道");
        add(GuPath.PAINTING, "画道");
        add(GuPath.TRANSFORMATION, "变化道");
    }

    private void addAttainment() {
        add(GuAttainment.NONE, "无");
        add(GuAttainment.ORDINARY, "普通");
        add(GuAttainment.QUASI_MASTER, "准大师");
        add(GuAttainment.MASTER, "大师");
        add(GuAttainment.QUASI_GRANDMASTER, "准宗师");
        add(GuAttainment.GRANDMASTER, "宗师");
        add(GuAttainment.QUASI_GREAT_GRANDMASTER, "准大宗师");
        add(GuAttainment.GREAT_GRANDMASTER, "大宗师");
        add(GuAttainment.QUASI_SUPREME_GRANDMASTER, "准无上大宗师");
        add(GuAttainment.SUPREME_GRANDMASTER, "无上大宗师");
    }

    //  Soul tiers: looked up from maxSoul (soul = men × 100), never stored on their own
    private void addSoulTier() {
        add(SoulTier.ONE, "一人魂");
        add(SoulTier.TEN, "十人魂");
        add(SoulTier.HUNDRED, "百人魂");
        add(SoulTier.THOUSAND, "千人魂");
        add(SoulTier.TEN_THOUSAND, "万人魂");
        add(SoulTier.HUNDRED_THOUSAND, "十万人魂");
        add(SoulTier.MILLION, "百万人魂");
        add(SoulTier.TEN_MILLION, "千万人魂");
        add(SoulTier.HUNDRED_MILLION, "亿人魂");
    }

    //  The three cells; initial capacities 30000 / 5 / 2, and overflowing them shatters the Mind Ocean
    private void addWisdomType() {
        add(WisdomType.THOUGHTS, "念");
        add(WisdomType.WILLS, "意");
        add(WisdomType.EMOTIONS, "情");
    }

    //  Brilliance: the natural thought regen rate (1 / 4 / 16 / 64 / 256 a second), rolled at birth
    private void addBrilliance() {
        add(Brilliance.ORDINARY, "才情普通");
        add(Brilliance.DECENT, "才情尚可");
        add(Brilliance.DISTINCTIVE, "才情不俗");
        add(Brilliance.OUTSTANDING, "才情卓越");
        add(Brilliance.UNRIVALED, "才情旷世");
    }
    //endregion
}
