# Guzhenren (蛊真人) — NeoForge 1.21.1

RPG / combat / exploration / xianxia mod. Intended as the *core* mod of a larger modpack,
so every system here must be extensible by later sibling mods.

- MC `1.21.1`, NeoForge `21.1.235`, Parchment `2024.11.17`, Java 21.
- Mod id `guzhenren`, base package `com.unknown.guzhenren`.
- **Project root is fixed**: `C:\alex\code\GZR Mod Dev\guzhenren-template-1.21.1`.
  Never read or write outside it unless the user explicitly asks.

## Language rules

- All chat replies to the user: **Simplified Chinese**.
- All code, code comments, commit messages, and this file: **English**
  (exception: existing `//` comments in `custom/enums/**` are Chinese — leave them).
- Every user-visible string goes through a translation key + both lang providers
  (`datagen/lang/EnUsLanguageProvider`, `ZhCnLanguageProvider`). Never hardcode display text.

## Naming convention

| Kind | Prefix | Examples |
|------|--------|----------|
| Registry / infrastructure holders | `Mod*` | `ModAttachments`, `ModDamageTypes`, `ModStreamCodecs`, `ModCommand`, `ModEnumArgument`, `ModDatapackProvider`, `ModDamageTypeTagsProvider` |
| Domain enums (the 蛊真人 world model) | `Gu*` | `GuRank`, `GuStage`, `GuTalent`, `GuSoulTier`, `GuLifeState`, `GuLifeForm`, `GuEssenceColor`, `GuExtremePhysique`, `GuPath`, `GuPathAttainment` |
| Attachment records | none | `CoreData`, `EssenceData`, `LifespanData`, `SoulData`, `PathData`, `PathEntry` |
| Services | none | `CoreService`, `EssenceService`, … |
| Event handler classes | none | `PlayerDataEvents`, `PlayerTickEvents` |

`GuPath` is prefixed rather than plain `Path` on purpose — a bare `Path` collides with
`java.nio.file.Path` in every file that touches both.

## Architecture: player data

Five "systems", each one NeoForge `AttachmentType` holding an **immutable record**.
Mutation only ever happens through the matching service in `attachment/service`.

| System   | Attachment      | Stored fields                                          |
|----------|-----------------|--------------------------------------------------------|
| core     | `CoreData`      | rank, stage, baseEssence, extremePhysique, lifeState    |
| essence  | `EssenceData`   | current                                                 |
| lifespan | `LifespanData`  | age, lifespan, lastDayIndex                             |
| soul     | `SoulData`      | maxSoul, currentSoul                                    |
| path     | `PathData`      | sparse `Map<GuPath, PathEntry(attainment, mark)>`       |

Plus `ModAttachments.ESSENCE_CARRY` (`Float`) — neither serialized nor synced, see Networking.

### Derived, never stored (single source of truth)

Storing these would create a second source of truth that silently rots whenever `core` changes.

- `talent` = `GuTalent.fromPercent(core.baseEssence())` — 甲/乙/丙/丁/十绝 tier.
- `maxEssence` = `baseEssence * stage.multiplier * rank.rankBase` (`EssenceService.maxEssence`).
- `soulTier` = `GuSoulTier.fromSoul(soul.maxSoul())` — 一人魂 / 十人魂 / ...
- `lifeForm` = `rank.getLifeForm()` — 凡 / 仙.

The client derives all of these itself, from the same synced records the server holds.

### Invariants enforced by `CoreService`

- `extremePhysique != NONE` **iff** `talent == EXTREME` (i.e. `baseEssence == 100`).
- `baseEssence` is `0` (未开窍) or `20..100` (开窍后). Clamped in `CoreData`'s compact ctor.
- `currentEssence <= maxEssence`, `currentSoul <= maxSoul` — clamped on every write, and
  re-clamped whenever core changes (raising rank raises the cap; lowering it must not leave
  a player above their new cap).

## Networking — there is none, and that is deliberate

NeoForge 21.1 syncs attachments itself. `ModAttachments` declares:

```java
.sync(OWNER_ONLY, CoreData.STREAM_CODEC)      // OWNER_ONLY = (holder, viewer) -> holder == viewer
```

which gives us, for free:

- `setData()` pushes the new value to the client (`Entity#setData` → `AttachmentSync.syncEntityUpdate`);
- login / respawn / dimension change each re-send the full set — vanilla is patched to call
  `AttachmentSync.syncInitialPlayerAttachments` at `PlayerList#placeNewPlayer`,
  `PlayerList#respawn` and `ServerPlayer#changeDimension`.

So **do not** write payloads, packet handlers, or a client-side mirror cache. The client reads
`mc.player.getData(ModAttachments.CORE)` — identical to the server. An earlier iteration of this
mod had all of that by hand; it was deleted.

`ESSENCE_CARRY` holds the sub-integer remainder of essence regen and is deliberately **unsynced
and unserialized**: if it lived in `EssenceData`, every regen step would `setData` and therefore
push a packet even on the steps where the pool did not move. Unsynced attachments no-op inside
`setData` (`syncEntityUpdate` returns early when `syncHandler == null`), so the carry is free.

To reveal a player's rank to *others* later (观气术, name-tag display), loosen `CORE`'s predicate.
That is the entire change.

## Formulas

```
maxEssence   = baseEssence * stage.essenceMultiplier * rank.rankBase
regenPerDay  = 100 * talent.regenRate * rank.rankBase * stage.essenceMultiplier
regenPerTick = regenPerDay / 24000
```

⚠ **The regen formula is wrong and the user will restate it** — see Pending below. Do not build
on it. Both formulas are funnelled through `EssenceService` so there is exactly one place to fix.

Essence does **not** regen while `lifeState != ALIVE` — a zombified cultivator's aperture is
dead and cannot draw in ambient qi (see the `GuLifeState` comment).

## Time, sleep, death

- One in-game day (`overworld().getDayTime() / 24000`) → `age + 1`, `lifespan - 1`.
  Day index is read from the **overworld** so that dimension-local time cannot skew it.
  `LifespanData.lastDayIndex` makes this idempotent and relog-safe.
- Sleeping through a full night → `currentSoul = maxSoul` and `currentEssence = maxEssence`.
  Fires exactly once per completed sleep (see `PlayerTickEvents.SLEEP_GRANTED`).
- `lifespan <= 0` → death by `guzhenren:lifespan_exhausted`.
- `currentSoul <= 0` → death by `guzhenren:soul_collapse`.
- All five attachments are `copyOnDeath()` **and** copied explicitly in `PlayerEvent.Clone`.
- On respawn: `lifespan` is floored to `GRACE_LIFESPAN` (1) and `currentSoul` to `maxSoul`,
  otherwise a player who died of old age would re-die on every respawn forever.

## Commands

`/gzr` (alias `/guzhenren`), permission level 2. Every leaf works on self, or on
`[targets]` when an `EntityArgument.players()` is supplied.

```
/gzr info|reset                       [targets]
/gzr core rank|stage|physique|lifestate set <v>   [targets]
/gzr core base set|add <int>          [targets]
/gzr core awaken                      [targets]   # rolls talent + baseEssence + physique
/gzr essence set|add <long> | refill  [targets]
/gzr lifespan age|lifespan set|add <long>         [targets]
/gzr soul max|current set|add <long> | refill     [targets]
/gzr path <path> attainment set <v>   [targets]
/gzr path <path> mark set|add <long>  [targets]
```

Enum arguments are `StringArgumentType.word()` + suggestions (`command/ModEnumArgument`),
not NeoForge's `EnumArgument` — no argument-type registration, no client/server sync concerns.

## Pending (user will drive these; do not pre-empt)

1. **HUD + command** — display style and behavior. The next major piece of work.
2. **Essence natural regen** — the current formula is a misreading; the user will restate it.
3. **Immortal ranks (`GuRank.SIX..NINE`)** — `rankBase == 0` is a *deliberate placeholder*.
   Immortals will not use the essence system at all; they get their own system much later.
   Do not "fix" the zero cap.

## Code style — follow `.editorconfig`, it is not decorative

```
max_line_length = 120
ij_java_keep_simple_methods_in_one_line = true
ij_java_blank_lines_around_method = 0
```

So: **a method whose body is one expression goes on one line**, in the enum files' shape —
`{return x;}`, no space after the brace. Group such methods tightly, no blank line between them.
Use short parameter names (`p`, `v`) in these to stay inside 120 columns; spell names out in
full in real multi-line methods.

```java
public static CoreData get(Player p) {return p.getData(ModAttachments.CORE);}
public static void setRank(ServerPlayer p, GuRank rank) {set(p, get(p).withRank(rank));}
public static void addBaseEssence(ServerPlayer p, int delta) {setBaseEssence(p, get(p).baseEssence() + delta);}
```

If a one-liner would exceed 120 columns, expand it into a normal block and move it below the
tight group rather than shrinking names further.
Check with: `awk 'length > 120' $(find src/main/java -name '*.java')` — but note `awk` counts
**bytes**, so CJK comments produce false hits.

## Gotcha: `Level` is `AutoCloseable`

`net.minecraft.world.level.Level implements LevelAccessor, AutoCloseable`, so any IDE flags
`entity.level()` with *"'Level' used without 'try'-with-resources"*. It is a false positive — you
never close a `Level` you got from an entity — but do not suppress it, route around it:

- need the registries? `entity.registryAccess()` (`Entity` has it; it delegates internally).
- need the server? `entity.getServer()`.

`ModDamageTypes.source` used to hit this and now uses `entity.registryAccess()`.

## Conventions

- Registries live in `registry/`, one `Mod*` holder class each. Register `DeferredRegister`s
  from the `Guzhenren` constructor.
- Enums: `implements StringRepresentable` + `CODEC` + `getTranslationKey()`. Follow the
  existing files in `custom/enums/**` exactly.
- Prefer deriving over storing. Prefer immutable records over mutable state.
- Damage types are datagen'd (`datagen/ModDatapackProvider` + `ModDamageTypeTagsProvider`).

## Build

```
./gradlew build          # compile + jar
./gradlew runData        # regenerate lang + damage type json into src/generated
./gradlew runClient
```
`src/generated/resources` is a source set — generated files must be committed.
