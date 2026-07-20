# 蛊真人
<sub>Guzhenren — a xianxia RPG mod for NeoForge, `1.21.1`</sub>

一个仙侠风格的角色扮演 / 战斗 / 探索类模组，围绕**空窍、肉身、脑海**三件事展开。
<sub>An RPG / combat / exploration mod built around three things a person is: aperture, body, and mind.</sub>

---

## 玩家 <sub>Players</sub>

- 按 **G** 打开信息面板，查看空窍、肉身、脑海三个页签。
  <sub>Press **G** to open the info panel — three tabs, one per domain.</sub>
- 左上角 HUD 常驻显示：真元、灵魂、寿元。
  <sub>A HUD in the top-left always shows essence, soul, and lifespan.</sub>
- `/gzr`（管理员指令，权限等级 2）可以直接读写以上所有数值，方便测试。
  <sub>`/gzr` is an operator command (permission level 2) for reading and writing all of the above directly, mainly for testing.</sub>
- 仍在早期开发阶段：没有合成配方，物品目前只能从创造模式栏或 `/give` 获取。
  <sub>Still early — no crafting recipes yet; every item is creative-tab / `/give` only for now.</sub>

## 开发者 <sub>Developers</sub>

- `Minecraft 1.21.1` · `NeoForge 21.1.235` · `Parchment 2024.11.17` · `Java 21`
- 可选依赖（未安装也能运行）：`JEI`、`Curios`。
  <sub>Optional at runtime, mod still loads without them: `JEI`, `Curios`.</sub>

```
./gradlew build       # 编译打包 / compile + jar
./gradlew runData      # 重新生成语言与数据 json / regenerate lang + data json
./gradlew runClient    # 启动客户端调试 / launch a dev client
```

入口：`Guzhenren.java`（通用）/ `GuzhenrenClient.java`（仅客户端）。
<sub>Entry points: `Guzhenren.java` (common) / `GuzhenrenClient.java` (client-only).</sub>

## 许可 <sub>License</sub>

模组本体：版权所有，保留所有权利。仓库内 `LICENSE.txt` 是继承自 NeoForge MDK 模板文件本身的 MIT 协议，不适用于模组代码。
<sub>The mod itself: all rights reserved. `LICENSE.txt` in this repo is the MIT license inherited from the NeoForge MDK template scaffolding — it does not cover the mod's own code.</sub>
