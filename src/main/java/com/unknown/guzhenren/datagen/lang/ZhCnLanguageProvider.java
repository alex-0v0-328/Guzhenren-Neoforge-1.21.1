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
import com.unknown.guzhenren.custom.enums.body.Race;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.soul.SoulTier;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

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

    private void add(EnumTranslatable key, String value) {add(key.getTranslationKey(), value);}

    //region DISPLAY
    private void addDisplayKeys() {
        add("guzhenren.display.realm",                        "%s%s");
        add("guzhenren.display.gu_line",                      "%s%s%s");
        add("guzhenren.display.gu",                           "蛊虫");
        add("guzhenren.display.gu_material",                  "蛊材");
        add("guzhenren.display.physique",                     "[%s]");
        add("guzhenren.display.lifespan",                     "%s [%s岁]");
        add("guzhenren.display.base_fraction",                "%s成%s");
        add("guzhenren.display.base_round",                   "%s成");
        add("guzhenren.display.base_full",                    "十成");
        add("guzhenren.display.base_tens.2",                  "二");
        add("guzhenren.display.base_tens.3",                  "三");
        add("guzhenren.display.base_tens.4",                  "四");
        add("guzhenren.display.base_tens.5",                  "五");
        add("guzhenren.display.base_tens.6",                  "六");
        add("guzhenren.display.base_tens.7",                  "七");
        add("guzhenren.display.base_tens.8",                  "八");
        add("guzhenren.display.base_tens.9",                  "九");
        add("guzhenren.display.base_units.1",                 "一");
        add("guzhenren.display.base_units.2",                 "二");
        add("guzhenren.display.base_units.3",                 "三");
        add("guzhenren.display.base_units.4",                 "四");
        add("guzhenren.display.base_units.5",                 "五");
        add("guzhenren.display.base_units.6",                 "六");
        add("guzhenren.display.base_units.7",                 "七");
        add("guzhenren.display.base_units.8",                 "八");
        add("guzhenren.display.base_units.9",                 "九");
        add("guzhenren.display.none",                         "[无]");
        add("guzhenren.display.wild",                         "野生·%s");
        add("guzhenren.display.vital",                        "本命·%s");
        add("guzhenren.display.strength.beast_reading",       "[%s%s之力]");
        add("guzhenren.display.strength.beast.strength_boar", "猪");
        add("guzhenren.display.strength.beast.strength_bear", "熊");
        add("guzhenren.display.strength.beast_number.1",      "一");
        add("guzhenren.display.strength.beast_number.2",      "两");
        add("guzhenren.display.strength.beast_number.10",     "十");
        add("guzhenren.display.strength.beast_number.100",    "百");
        add("guzhenren.display.strength.beast_number.1000",   "千");
        add("guzhenren.display.strength.beast_number.10000",  "万");
        add("guzhenren.display.strength.num_join",            "%s%s");
        add("guzhenren.display.strength.num_join_zero",       "%s零%s");
        add("guzhenren.display.strength.num_hundreds.1",      "一百");
        add("guzhenren.display.strength.num_hundreds.2",      "二百");
        add("guzhenren.display.strength.num_hundreds.3",      "三百");
        add("guzhenren.display.strength.num_tens_led.1",      "一十");
        add("guzhenren.display.strength.num_tens.1",          "十");
        add("guzhenren.display.strength.num_tens.2",          "二十");
        add("guzhenren.display.strength.num_tens.3",          "三十");
        add("guzhenren.display.strength.num_tens.4",          "四十");
        add("guzhenren.display.strength.num_tens.5",          "五十");
        add("guzhenren.display.strength.num_tens.6",          "六十");
        add("guzhenren.display.strength.num_tens.7",          "七十");
        add("guzhenren.display.strength.num_tens.8",          "八十");
        add("guzhenren.display.strength.num_tens.9",          "九十");
        add("guzhenren.display.strength.num_units.1",         "一");
        add("guzhenren.display.strength.num_units.2",         "二");
        add("guzhenren.display.strength.num_units.3",         "三");
        add("guzhenren.display.strength.num_units.4",         "四");
        add("guzhenren.display.strength.num_units.5",         "五");
        add("guzhenren.display.strength.num_units.6",         "六");
        add("guzhenren.display.strength.num_units.7",         "七");
        add("guzhenren.display.strength.num_units.8",         "八");
        add("guzhenren.display.strength.num_units.9",         "九");
        add("guzhenren.display.strength.jin_total",           "[%s斤]");
        add("guzhenren.display.strength.jin_reading",         "[%s斤之力]");
        add("guzhenren.display.strength.jun_reading",         "[%s钧之力]");

        add("guzhenren.hud.lifespan",                         "寿元 %s");
        add("guzhenren.display.tenth.2",                      "二成");
        add("guzhenren.display.tenth.3",                      "三成");
        add("guzhenren.display.tenth.4",                      "四成");
        add("guzhenren.display.tenth.5",                      "五成");
        add("guzhenren.display.tenth.6",                      "六成");
        add("guzhenren.display.tenth.7",                      "七成");
        add("guzhenren.display.tenth.8",                      "八成");
        add("guzhenren.display.tenth.9",                      "九成");
        add("guzhenren.display.tenth.10",                     "十成十");

        add("guzhenren.hud.refining",                         "炼化 %s/%s");
        add("guzhenren.hud.using",                            "使用 %s/%s");
        add("guzhenren.hud.refining_plain",                   "炼化中");
        add("guzhenren.hud.using_plain",                      "使用中");
    }
    //endregion

    //region COMMAND
    private void addCommandKeys() {
        add("guzhenren.command.header",               "[GZR]");
        add("guzhenren.command.tagged",               "[GZR] %s");
        add("guzhenren.command.updated",              "已更新 %s 名玩家");
        add("guzhenren.command.unknown_value",        "未知的取值: %s");

        add("guzhenren.command.failed.awakened",      "%s 已开窍 —— 要重掷请先 /guzhenren reset");
        add("guzhenren.command.failed.unawakened",    "%s 尚未开窍 —— 修为相关的值只能由 /guzhenren awaken 建立");
        add("guzhenren.command.failed.tag_path",      "%s 不是 %s 的来源 —— 通用来源是 natural");

        add("guzhenren.command.info.aperture_index",  "第 %s 窍");
        add("guzhenren.command.info.aperture_state",  "空窍状态  %s");
        add("guzhenren.command.info.realm",           "玩家修为  %s");
        add("guzhenren.command.info.talent",          "玩家天赋  %s");
        add("guzhenren.command.info.essence",         "玩家真元  %s / %s");
        add("guzhenren.command.info.distilled",       "精炼真元  %s / %s");
        add("guzhenren.command.info.primary_path",    "主修流派  %s");
        add("guzhenren.command.info.secondary_path",  "辅修流派  %s");
        add("guzhenren.command.info.soul",            "玩家魂魄  %s / %s");
        add("guzhenren.command.info.lifespan",        "玩家寿元  %s");
        add("guzhenren.command.info.life_state",      "肉身状态  %s");
        add("guzhenren.command.info.life_form",       "玩家形态  %s");
        add("guzhenren.command.info.race",            "种族      %s");
        add("guzhenren.command.info.wisdom",          "智道造诣  %s");
        add("guzhenren.command.info.qi",              "玩家气道  %s");
        add("guzhenren.command.info.qi_total",        " 道痕 %s");
        add("guzhenren.command.info.qi_speck_total",  " 碎屑 %s");
        add("guzhenren.command.info.qi_mark_entry",   "  %s  道痕 %s");
        add("guzhenren.command.info.qi_speck_entry",  "  %s  碎屑 %s");
        add("guzhenren.command.info.paths",           "流派造诣");
        add("guzhenren.command.info.path_entry",      "  %s  %s  道痕 %s");
        add("guzhenren.command.info.path_speck",      " 碎屑 %s");
        add("guzhenren.command.info.strength",        "力道造诣");
        add("guzhenren.command.info.strength_entry",  "  %s  %s");
        add("guzhenren.command.info.capacity",        "肉身承受  %s / %s斤");
        add("guzhenren.command.info.attack",          "肉身攻击  %s");
        add("guzhenren.command.info.brilliance",      "才情  %s");
        add("guzhenren.command.info.brilliance_rate", "%s个念头每秒");
        add("guzhenren.command.info.mind",            "脑海");
        add("guzhenren.command.info.mind_entry",      "  %s  %s / %s");

        add("guzhenren.command.info.detail",          " [%s]");
    }
    //endregion

    //region SCREEN
    private void addScreenKeys() {
        add("key.categories.guzhenren",              "蛊真人");
        add("key.guzhenren.open_info",               "打开信息面板");

        add("guzhenren.screen.info.title",           "信息");
        add("guzhenren.screen.tab.aperture",         "空窍");
        add("guzhenren.screen.tab.body",             "肉身");
        add("guzhenren.screen.tab.path",             "流派造诣");
        add("guzhenren.screen.tab.mind",             "脑海");
        add("guzhenren.screen.tab.storage",          "空窍存储");
        add("guzhenren.screen.tab.refinement",       "炼蛊");
        add("guzhenren.menu.aperture_storage",       "空窍存储");
        add("guzhenren.menu.vital",                  "本命");
        add("guzhenren.menu.refinement",             "炼蛊");
        add("guzhenren.menu.refinement.essence",     "炼制此蛊需真元%s");
        add("guzhenren.menu.refinement.craft",       "炼制");
        add("guzhenren.menu.refinement.room",        "输出格放不下了");
        add("guzhenren.menu.refinement.awakened",    "未开窍，无法炼蛊");
        add("guzhenren.menu.refinement.rings",       "外圈 蛊材  ·  内圈 蛊虫");
        add("guzhenren.menu.refinement.window",      "第 %s/%s 窗口  ·  余 %s 秒  ·  元石 %s/%s");
        add("guzhenren.menu.refinement.gap",         "第 %s/%s 窗口已过  ·  沉炼中");
        add("guzhenren.menu.refinement.lost_stones", "元石不足，炼制失败");
        add("guzhenren.menu.refinement.lost_essence","真元耗尽，炼制失败");
        add("guzhenren.menu.refinement.lost_roll",   "炼制失败");
        add("guzhenren.menu.refinement.elder_spent", "元老蛊已耗尽，已退回背包");
        add("guzhenren.screen.label.primary_path",   "主修流派");
        add("guzhenren.screen.label.secondary_path", "辅修流派");
        add("guzhenren.screen.pick.title",           "选择辅修流派");
        add("guzhenren.screen.pick.hint",            "点击选择");
        add("guzhenren.screen.label.realm",          "修为");
        add("guzhenren.screen.label.talent",         "天赋");
        add("guzhenren.screen.label.essence",        "真元");
        add("guzhenren.screen.label.distilled",      "精炼真元");
        add("guzhenren.screen.label.state",          "状态");
        add("guzhenren.screen.label.life_form",      "形态");
        add("guzhenren.screen.label.race",           "种族");
        add("guzhenren.screen.label.wisdom",         "智道造诣");
        add("guzhenren.screen.label.soul",           "魂魄");
        add("guzhenren.screen.label.lifespan",       "寿元");
        add("guzhenren.screen.label.qi",             "气道造诣");
        add("guzhenren.screen.label.paths",          "流派造诣");
        add("guzhenren.screen.label.strength",       "力道造诣");
        add("guzhenren.screen.label.capacity",       "承受");
        add("guzhenren.screen.label.attack",         "攻击力");
        add("guzhenren.screen.capacity",             "%s / %s斤");
        add("guzhenren.screen.label.brilliance",     "才情");
        add("guzhenren.screen.path_value",           "%s 道痕 %s");
        add("guzhenren.screen.qi_mark",              "道痕 %s");
        add("guzhenren.screen.qi_speck",             "碎屑 %s");
    }
    //endregion

    //region ITEM
    private void addItemKeys() {
        addItem(ModItems.HOPE_GU,                          "希望蛊");
        addItem(ModItems.COPPER_RELICS_GU,                 "青铜舍利蛊");
        addItem(ModItems.STEEL_RELICS_GU,                  "赤铁舍利蛊");
        addItem(ModItems.SILVER_RELICS_GU,                 "白银舍利蛊");
        addItem(ModItems.GOLD_RELICS_GU,                   "黄金舍利蛊");
        addItem(ModItems.CRYSTAL_RELICS_GU,                "紫晶舍利蛊");
        addItem(ModItems.WHITE_BOAR_GU,                    "白豕蛊");
        addItem(ModItems.BLACK_BOAR_GU,                    "黑豕蛊");
        addItem(ModItems.FLOWER_BOAR_GU,                   "花豕蛊");
        addItem(ModItems.BEAR_STRENGTH_GU,                 "熊力蛊");
        addItem(ModItems.DRAGONPILL_CRICKET_GU,            "龙丸蛐蛐蛊");
        addItem(ModItems.BRUTE_FORCE_LONGHORN_BEETLE_GU,   "蛮力天牛蛊");
        addItem(ModItems.ALL_OUT_EFFORT_GU_3,              "三转全力以赴蛊");
        addItem(ModItems.ALL_OUT_EFFORT_GU_4,              "四转全力以赴蛊");
        addItem(ModItems.ALL_OUT_EFFORT_GU_5,              "五转全力以赴蛊");
        addItem(ModItems.JIN_STRENGTH_GU,                  "斤力蛊");
        addItem(ModItems.TENS_JIN_STRENGTH_GU,             "十斤之力蛊");
        addItem(ModItems.JUN_STRENGTH_GU,                  "钧力蛊");
        addItem(ModItems.TENS_JUN_STRENGTH_GU,             "十钧之力蛊");
        addItem(ModItems.VITALITY_LEAF_GU,                 "生机叶蛊");
        addItem(ModItems.LIFESPAN_GU,                      "寿蛊");
        addItem(ModItems.TENS_LIFESPAN_GU,                 "十年寿蛊");
        addItem(ModItems.HUNDREDS_LIFESPAN_GU,             "百年寿蛊");
        addItem(ModItems.THOUSANDS_LIFESPAN_GU,            "千年寿蛊");
        addItem(ModItems.LIQUOR_WORM,                      "酒虫");
        addItem(ModItems.FOUR_FLAVORS_LIQUOR_WORM,         "四味酒虫");
        addItem(ModItems.SEVEN_FRAGRANCES_LIQUOR_WORM,     "七香酒虫");
        addItem(ModItems.NINE_EYES_LIQUOR_WORM,            "九眼酒虫");
        addItem(ModItems.PRIMEVAL_ELDER_GU_1,              "一转元老蛊");
        addItem(ModItems.PRIMEVAL_ELDER_GU_2,              "二转元老蛊");
        addItem(ModItems.PRIMEVAL_ELDER_GU_3,              "三转元老蛊");
        addItem(ModItems.PRIMEVAL_ELDER_GU_4,              "四转元老蛊");
        addItem(ModItems.PRIMEVAL_ELDER_GU_5,              "五转元老蛊");
        addItem(ModItems.PRIMEVAL_STONE,                   "元石");
        addItem(ModItems.LIQUOR,                           "酒");

        addItem(ModItems.SWORD_QI_1,                       "一转剑气");
        addItem(ModItems.SWORD_QI_2,                       "二转剑气");
        addItem(ModItems.SWORD_QI_3,                       "三转剑气");
        addItem(ModItems.SWORD_QI_4,                       "四转剑气");
        addItem(ModItems.SWORD_QI_5,                       "五转剑气");
        addItem(ModItems.STRENGTH_QI_1,                    "一转力气");
        addItem(ModItems.STRENGTH_QI_2,                    "二转力气");
        addItem(ModItems.STRENGTH_QI_3,                    "三转力气");
        addItem(ModItems.STRENGTH_QI_4,                    "四转力气");
        addItem(ModItems.STRENGTH_QI_5,                    "五转力气");
        addItem(ModItems.LIFE_QI_1,                        "一转生气");
        addItem(ModItems.LIFE_QI_2,                        "二转生气");
        addItem(ModItems.LIFE_QI_3,                        "三转生气");
        addItem(ModItems.LIFE_QI_4,                        "四转生气");
        addItem(ModItems.LIFE_QI_5,                        "五转生气");
        addItem(ModItems.ESSENCE_QI_1,                     "一转元气");
        addItem(ModItems.ESSENCE_QI_2,                     "二转元气");
        addItem(ModItems.ESSENCE_QI_3,                     "三转元气");
        addItem(ModItems.ESSENCE_QI_4,                     "四转元气");
        addItem(ModItems.ESSENCE_QI_5,                     "五转元气");
        addItem(ModItems.DEATH_QI_5,                       "五转死气");

        add("itemGroup.guzhenren.mortal_gu",               "凡蛊");
        add("itemGroup.guzhenren.gu_material",             "蛊材");

        add("guzhenren.item.failed.awakened",              "你已开窍");
        add("guzhenren.item.failed.unawakened",            "你未开窍");
        add("guzhenren.item.failed.essence_full",          "真元已满");
        add("guzhenren.item.failed.rank_mismatch",         "境界不符 — 此蛊需%s");
        add("guzhenren.item.failed.stage_peak",            "已至小境界巅峰");
        add("guzhenren.item.failed.beast_strength_held",   "已有%s之力");
        add("guzhenren.item.failed.human_strength_full",   "此力已满%s层");
        add("guzhenren.item.failed.vitality_active",       "生机叶效果未散");
        add("guzhenren.item.failed.refine_essence",        "真元不足无法炼化");
        add("guzhenren.item.failed.essence",               "真元不足");
        add("guzhenren.item.failed.liquor_rank",           "唯%s修士方可催动此蛊");
        add("guzhenren.item.failed.liquor_distilling",     "精炼未止");
        add("guzhenren.item.failed.elder_gu_empty",        "蛊中无元石");
        add("guzhenren.item.failed.elder_gu_full",         "蛊中元石已满");
        add("guzhenren.item.failed.elder_gu_no_stones",    "身上无元石可存");
        add("guzhenren.item.failed.gu_cooldown",           "此蛊尚需 %s 秒");
        add("guzhenren.item.failed.all_out_active",        "全力未歇");
        add("guzhenren.item.failed.gu_starving",           "蛊已太饿 需先喂食");

        add("guzhenren.item.gu.invested",                  "已投入 %s/%s");
        add("guzhenren.item.gu.refine_progress",           "炼化 %s/%s");
        add("guzhenren.item.gu.refine_cost",               "炼化 %s");
        add("guzhenren.item.gu.hunger_progress",           "饱食 %s/%s");
        add("guzhenren.item.gu.stored_stones",             "存石 %s/%s");
        add("guzhenren.item.gu.meal_liquor",               "一餐 %s 瓶酒");
        add("guzhenren.item.gu.lifespan_gained",           "寿元 +%s 年");
        add("guzhenren.item.gu.hungry",                    "%s饿了");
        add("guzhenren.item.gu.starved",                   "%s饿死了");
        add("guzhenren.item.gu.exhausted",                 "%s被强行催动，力竭而亡！");
        add("guzhenren.item.gu.ruined",                    "%s在炼制中被炼废了！");
        add("guzhenren.item.gu.vital_lost",                "%s断绝，气血、魂魄与念头俱损！");
        add("guzhenren.item.death_qi_cured",               "生气化去死气 —— 回复 %s 年寿元");

        add("effect.guzhenren.vitality_leaf",              "生机叶");
        add("effect.guzhenren.liquor_worm",                "酒虫");
        add("effect.guzhenren.life_qi",                    "生气");
        add("effect.guzhenren.essence_qi",                 "元气");
        add("effect.guzhenren.death_qi",                   "死气");
        add("effect.guzhenren.strength_qi",                "力气");
        add("effect.guzhenren.flower_boar_gu",             "花豕蛊");
        add("effect.guzhenren.dragonpill_cricket_gu",      "龙丸蛐蛐蛊");
        add("effect.guzhenren.brute_force_longhorn_beetle_gu", "蛮力天牛蛊");
        add("effect.guzhenren.all_out_effort",             "全力以赴");
    }
    //endregion

    //region DEATH
    private void addDeathMessages() {
        add("death.attack.guzhenren.lifespan_exhausted",   "%1$s 寿元耗尽而亡");
        add("death.attack.guzhenren.soul_collapse",        "%1$s 魂魄衰竭而亡");
        add("death.attack.guzhenren.mind_ocean_shattered", "%1$s 脑海炸裂而亡");
        add("death.attack.guzhenren.vital_gu_lost",        "%1$s 本命蛊断绝而亡");
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
        addRace();
        addLifeState();
        addSoulTier();
        addMarkTag();
        addPath();
        addAttainment();
        addWisdomType();
        addBrilliance();
        addBeastStrength();
        addStrengthBranch();
        addHumanStrength();
    }

    private void addBeastStrength() {
        add(BeastStrength.WHITE_BOAR, "白豕");
        add(BeastStrength.BLACK_BOAR, "黑豕");
        add(BeastStrength.BEAR,       "熊");
    }

    private void addStrengthBranch() {
        add(StrengthBranch.HUMAN,           "人力钧力流");
        add(StrengthBranch.BEASTS,          "兽力虚影流");
        add(StrengthBranch.ENVIRONMENT,     "气象天地流");
        add(StrengthBranch.OLDER_ANTIQUITY, "上古力道");
    }

    private void addHumanStrength() {
        add(HumanStrength.JIN,     "斤");
        add(HumanStrength.TEN_JIN, "十斤");
        add(HumanStrength.JUN,     "钧");
        add(HumanStrength.TEN_JUN, "十钧");
    }

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

    private void addRace() {
        add(Race.HUMAN,       "人族");
        add(Race.HAIRY_MEN,   "毛民");
        add(Race.EGGMEN,      "蛋人");
        add(Race.ROCKMEN,     "石人");
        add(Race.FEATHERMEN,  "羽民");
        add(Race.INKMEN,      "墨人");
        add(Race.MINIMEN,     "小人");
        add(Race.MERMEN,      "鲛人");
        add(Race.BEASTMEN,    "兽人");
        add(Race.DRAGONMEN,   "龙人");
        add(Race.MUSHROOMMEN, "菇人");
        add(Race.SNOWMEN,     "雪人");
    }

    private void addLifeState() {
        add(LifeState.ALIVE,  "生");
        add(LifeState.DEAD,   "死");
    }

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

    private void addMarkTag() {
        add(MarkTag.NATURAL,         "自然");
        add(MarkTag.RACE,            "种族");
        add(MarkTag.QI_HEAVEN,       "天气");
        add(MarkTag.QI_EARTH,        "地气");
        add(MarkTag.QI_HUMAN,        "人气");
        add(MarkTag.QI_NATURAL,      "自然气");
        add(MarkTag.QI_DEATH,        "死气");
        add(MarkTag.QI_SWORD,        "剑气");
        add(MarkTag.QI_LIFE,         "生气");
        add(MarkTag.QI_ESSENCE,      "元气");
        add(MarkTag.QI_STRENGTH,     "力气");
        add(MarkTag.STRENGTH_BEASTS, "兽力虚影流");
        add(MarkTag.STRENGTH_BOAR,   "豕力");
        add(MarkTag.STRENGTH_BEAR,   "熊力");
        add(MarkTag.STRENGTH_HUMAN,  "人力钧力流");
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
        add(GuPath.CLOUD,          "云道");
        add(GuPath.QI,             "气道");
        add(GuPath.SOUND,          "音道");
        add(GuPath.LIGHT,          "光道");
        add(GuPath.DARK,           "暗道");
        add(GuPath.POISON,         "毒道");
        add(GuPath.STRENGTH,       "力道");
        add(GuPath.DREAM,          "梦道");
        add(GuPath.REFINEMENT,     "炼道");
        add(GuPath.WISDOM,         "智道");
        add(GuPath.INFORMATION,    "信道");
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

    private void addWisdomType() {
        add(WisdomType.THOUGHTS, "念");
        add(WisdomType.WILLS,    "意");
        add(WisdomType.EMOTIONS, "情");
    }

    private void addBrilliance() {
        add(Brilliance.ORDINARY,    "才情普通");
        add(Brilliance.DECENT,      "才情尚可");
        add(Brilliance.DISTINCTIVE, "才情不俗");
        add(Brilliance.OUTSTANDING, "才情卓越");
        add(Brilliance.UNRIVALED,   "才情旷世");
    }
    //endregion
}
