# 蛊真人
<sub>Guzhenren — current technical reference for a NeoForge 1.21.1 xianxia RPG mod. Java 21; Epic Fight required; L1/L2/L3 tests; mod code all rights reserved.</sub>

围绕**空窍、肉身、脑海**三个域展开的仙侠 RPG 模组。本文只写技术规格。

## 工具链

|                              |                                       |
|------------------------------|---------------------------------------|
| Minecraft                    | `1.21.1`                              |
| NeoForge                     | `21.1.238`                            |
| Parchment                    | `2024.11.17`                          |
| Java                         | `21`                                  |
| mod id / package             | `guzhenren` · `com.unknown.guzhenren` |
| 必需依赖                     | `Epic Fight`                          |
| 可选依赖                     | `JEI` · `Curios`                      |

```
./gradlew build
./gradlew runData
./gradlew runClient
```

入口 `Guzhenren.java`（通用）/ `GuzhenrenClient.java`（仅客户端）。
⚠ `src/generated/resources` 是 source set，`runData` 的产物**必须提交**。

## 数据层

玩家的持久状态主要是 **NeoForge data attachment**：九个核心 attachment 是**不可变 record**，写入只经过拥有它的 service；另有出生闩与瞬时真元 carry。

| Attachment                                                 | 装什么                               |
|------------------------------------------------------------|--------------------------------------|
| `ApertureData` · `ApertureNourishData` · `ApertureStorage` | 空窍、修炼进度、蛊虫仓               |
| `BodyData`                                                 | 生命形态、种族、年龄与寿元、死气欠账 |
| `SoulData`                                                 | 魂魄                                 |
| `PathData` · `PathQiData` · `PathStrengthData`             | 33 条流派道痕、八种气、力道数据      |
| `MindData`                                                 | 才情与念/意/情三个池                 |

⚠ 同步靠 attachment 自带的 `sync(OWNER_ONLY, …)`，**不写玩家数据的 payload**；当前有六个自定义 payload，全部是 B 面板或移动操作的客户端意图。

耐力由 **Epic Fight** 独占保存、回复、HUD 与普通消耗；GZR 只通过兼容桥接调整最大耐力，并让僵尸与半僵的技能耐力消耗为零。

## 承重约定

- **一件事一扇门。** 攻击力只经 `BodyAttackService`，时间流速只经 `PathTimeFlowService`，道痕只经 `PathService`；调用点不自行计算。
- **每秒一次心跳**（`PlayerTickEvents`，`tickCount % 20`），一串步骤，**先后顺序承重**。
  ⚠ 任何挂在心跳上的间隔必须整除 20，否则静默不执行。
- **时间锚**（存 game time、事后反推数量）是主要状态形状之一：气的衰减、冷却、半僵窗口。
  ⚠ 哨兵永远不能是 `0` —— 游戏时钟的 0 是真值。
- **枚举是封闭词汇**，附属模组扩展的是数据层而不是词汇：不新增 `GuPath` / `Rank` / `MarkTag` 常量。
- **派生优先于存储**：上限、称号、承受、攻击力都是算出来的，不落盘。

## 内容系统

物品与蛊虫（两条分支：一次性 / 需照顾；后者包含用完消失的蛊）· 炼蛊（26 秒仪式 + 蛊方）· 修炼（温养与冲击窍壁）·
气（八种，独立资源）· 生命形态四态 · 野生蛊虫实体 · 六页 B 面板与 HUD · `/guzhenren`（别名 `/gzr`，权限 2）。

⚠ 仍在开发阶段：蛊方只有两张，其余物品走创造模式栏或 `/give`。

## 测试

`src/pureTest/java` 是纯 JVM 的 L1，`src/test/java` 是可用注册表但没有 world 的 L2；两者都是 JUnit 5，`./gradlew build` 会跑。
真实世界与 tick 行为放在 `src/main/java/.../gametest` 的 L3，使用 `./gradlew runGameTestServer`。
⚠ **不 mock Minecraft。**

## 许可

模组本体版权所有，保留所有权利。`LICENSE.txt` 是继承自 NeoForge MDK 模板的 MIT 协议，**不覆盖模组代码**。
