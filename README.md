# 蛊真人
<sub>Guzhenren — a xianxia RPG mod for NeoForge, `1.21.1`</sub>

围绕**空窍、肉身、脑海**三个域展开的仙侠 RPG 模组。本文只写技术规格。
<sub>A xianxia RPG built on three domains — aperture, body, mind. This file is the technical spec only.</sub>

## 工具链 <sub>Toolchain</sub>

|                              |                                       |
|------------------------------|---------------------------------------|
| Minecraft                    | `1.21.1`                              |
| NeoForge                     | `21.1.238`                            |
| Parchment                    | `2024.11.17`                          |
| Java                         | `21`                                  |
| mod id / package             | `guzhenren` · `com.unknown.guzhenren` |
| 必需依赖 <sub>required</sub> | `Epic Fight`                          |
| 可选依赖 <sub>optional</sub> | `JEI` · `Curios`                      |

```
./gradlew build      # 编译 + 打包 + 跑单测 / compile, jar, unit tests
./gradlew runData    # 重新生成 lang 与 json / regenerate lang + json
./gradlew runClient  # 启动开发客户端 / launch a dev client
```

入口 `Guzhenren.java`（通用）/ `GuzhenrenClient.java`（仅客户端）。
<sub>Entry points: `Guzhenren.java` (common), `GuzhenrenClient.java` (client-only).</sub>
⚠ `src/generated/resources` 是 source set，`runData` 的产物**必须提交**。
<sub>`src/generated/resources` is a source set — its output is committed.</sub>

## 数据层 <sub>Data layer</sub>

玩家的持久状态主要是 **NeoForge data attachment**：九个核心 attachment 是**不可变 record**，写入只经过拥有它的 service；另有出生闩与瞬时真元 carry。
<sub>Persistent player state lives mainly in data attachments: nine core attachments are immutable records, written only through their owning services; two auxiliary attachments hold the birth latch and transient essence carry.</sub>

| Attachment                                                 | 装什么                                                                                         |
|------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| `ApertureData` · `ApertureNourishData` · `ApertureStorage` | 空窍、修炼进度、蛊虫仓 <sub>apertures, cultivation, Gu storage</sub>                           |
| `BodyData`                                                 | 生命形态、种族、年龄与寿元、死气欠账 <sub>life form, race, age & lifespan, death-qi debt</sub> |
| `SoulData`                                                 | 魂魄 <sub>soul</sub>                                                                           |
| `PathData` · `PathQiData` · `PathStrengthData`             | 33 条流派道痕、八种气、力道数据 <sub>Dao marks, qi, strength</sub>                             |
| `MindData`                                                 | 才情与念/意/情三个池 <sub>brilliance and the three mind pools</sub>                            |

⚠ 同步靠 attachment 自带的 `sync(OWNER_ONLY, …)`，**不写玩家数据的 payload**；当前有六个自定义 payload，全部是 B 面板或移动操作的客户端意图。
<sub>Sync rides the attachment's own `sync(...)`; six custom payloads currently exist, all of them client intents from the B-panel or movement input.</sub>

耐力由 **Epic Fight** 独占保存、回复、HUD 与普通消耗；GZR 只通过兼容桥接调整最大耐力，并让僵尸与半僵的技能耐力消耗为零。
<sub>Epic Fight exclusively owns stamina storage, regeneration, HUD, and ordinary consumption; GZR only adjusts maximum stamina through its compatibility bridge and makes zombie and half-zombie skill stamina costs zero.</sub>

## 承重约定 <sub>Load-bearing conventions</sub>

- **一件事一扇门。** 攻击力只经 `BodyAttackService`，时间流速只经 `PathTimeFlowService`，
  道痕只经 `PathService`。在调用点自己算，就是这个项目历史上大多数 bug 的来源。
  <sub>One door per fact. Doing the arithmetic at a call site is where the bugs came from.</sub>
- **每秒一次心跳**（`PlayerTickEvents`，`tickCount % 20`），一串步骤，**先后顺序承重**。
  ⚠ 任何挂在心跳上的间隔必须整除 20，否则静默不执行。
  <sub>A one-second heartbeat whose step order matters; any interval hung on it must divide 20.</sub>
- **时间锚**（存 game time、事后反推数量）是主要状态形状之一：气的衰减、冷却、半僵窗口。
  ⚠ 哨兵永远不能是 `0` —— 游戏时钟的 0 是真值。
  <sub>Timestamp anchors are a core shape; their sentinel is never `0`, which is a real game time.</sub>
- **枚举是封闭词汇**，附属模组扩展的是数据层而不是词汇：不新增 `GuPath` / `Rank` / `MarkTag` 常量。
  <sub>Enums are closed vocabulary — an addon extends the data layer, never the words.</sub>
- **派生优先于存储**：上限、称号、承受、攻击力都是算出来的，不落盘。
  <sub>Derive rather than store: caps, titles, capacity and attack are all computed.</sub>

## 内容系统 <sub>Systems</sub>

物品与蛊虫（两条分支：一次性 / 需照顾；后者包含用完消失的蛊）· 炼蛊（26 秒仪式 + 蛊方）· 修炼（温养与冲击窍壁）·
气（八种，独立资源）· 生命形态四态 · 野生蛊虫实体 · 六页 B 面板与 HUD · `/guzhenren`（别名 `/gzr`，权限 2）。
<sub>Items and Gu (two branches: one-shot and tended, with consumed Gu as a tended subtype), refinement, cultivation, qi, life forms, wild Gu entities,
a six-tab B-panel, and an operator command tree.</sub>

⚠ 仍在开发阶段：蛊方只有两张，其余物品走创造模式栏或 `/give`。
<sub>Still in development: only two Gu recipes exist; everything else is creative-tab / `/give`.</sub>

## 测试 <sub>Tests</sub>

`src/pureTest/java` 是纯 JVM 的 L1，`src/test/java` 是可用注册表但没有 world 的 L2；两者都是 JUnit 5，`./gradlew build` 会跑。
真实世界与 tick 行为放在 `src/main/java/.../gametest` 的 L3，使用 `./gradlew runGameTestServer`。
⚠ **不 mock Minecraft。**
<sub>L1 covers pure JVM logic, L2 runs with the modded registry but no world, and real world/tick behavior belongs
to L3 GameTest. Minecraft is never mocked.</sub>

## 许可 <sub>License</sub>

模组本体版权所有，保留所有权利。`LICENSE.txt` 是继承自 NeoForge MDK 模板的 MIT 协议，**不覆盖模组代码**。
<sub>The mod itself is all rights reserved. `LICENSE.txt` is the MDK template's MIT license and does not
cover the mod's own code.</sub>
