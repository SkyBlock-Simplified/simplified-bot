# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

See the root [`CLAUDE.md`](../CLAUDE.md) for multi-module build commands, environment variables, cross-cutting patterns, and dependency details.

## Build & Test

```bash
# From repo root
./gradlew :simplified-bot:build          # Build (includes shadowJar)
./gradlew :simplified-bot:test           # Run all tests
./gradlew :simplified-bot:test --tests "dev.sbs.simplifiedbot.optimizer.OptimizerTest"  # Single test class

# Fat JAR (shadowJar merged into build task)
./gradlew :simplified-bot:shadowJar      # Output: build/libs/simplified-bot-0.1.0.jar
```

Tests require a live MariaDB database and valid `HYPIXEL_API_KEY` - they are integration tests that hit real APIs. A `TestLifecycleListener` (JUnit platform listener) manages `SimplifiedApi` startup/shutdown so the test JVM exits cleanly.

## Module Overview

`simplified-bot` is the Discord bot application. It depends on all three library modules (`api`, `discord-api`, `minecraft-api`) via Maven coordinates (not project references).

### Entry Points

- **`SimplifiedBot`** - Main bot entry point. Extends `DiscordBot`, configures Discord gateway, connects to MariaDB via `JpaConfig.commonSql()`, schedules Hypixel cache updates.
- **`SimplifiedConsole`** - Secondary entry point for a resource-processing console bot. Runs `ResourceProcessor` tasks on a schedule to sync Hypixel resource API data into the database.

### Package Structure

**`command/`** - Discord slash commands. Two categories:
- Commands extending **`SkyBlockUserCommand`** (most commands) - automatically resolve a Minecraft player via the `name` and `profile` parameters, construct a `SkyBlockUser`, then delegate to `subprocess()`.
- Commands extending **`DiscordCommand`** directly (e.g. `AboutCommand`, `HelpCommand`) - general commands not tied to a SkyBlock player.
- `command/developer/` - Developer-only commands (`@Structure(developerOnly = true)`)
- `command/embed/` - Embed management commands
- `command/reputation/` - Reputation system commands

**`optimizer/`** - OptaPlanner-based reforge optimizer. Solves for best reforge allocation across armor, weapon, and accessories to maximize either damage-per-hit or damage-per-second.
- `Optimizer` - Static facade; owns two `SolverManager` instances (DPH and DPS)
- `modules/common/` - Shared abstractions: `Solution` (planning solution), `ItemEntity` (planning entity), `ReforgeFact` (planning fact), `Calculator` (score calculator)
- `modules/damage_per_hit/` and `modules/damage_per_second/` - Concrete implementations for each optimization type
- `util/OptimizerRequest` - Builder-pattern request object; resolves player profile, loads weapon from inventory/ender chest/backpacks by item ID
- `util/OptimizerSolver` - Programmatic `SolverConfig` builder (alternative to XML config)
- `util/OptimizerHelper` - Computes damage multipliers, enchant bonuses, armor bonuses, pet ability bonuses
- XML solver configs in `src/main/resources/optaplanner/`

**`profile_stats/`** - Player stat calculation engine.
- `ProfileStats` - Aggregates all stat sources (skills, slayers, dungeons, armor, accessories, pets, potions, century cakes, essence perks, mining core, etc.) into a typed `StatData` map keyed by `ProfileStats.Type` enum. Supports bonus stat calculation (enchantment multipliers, pet ability percentages).
- `data/` - Data abstractions: `Data` (base/bonus stat holder), `StatData`, `ItemData`, `AccessoryData`, `ObjectData`, `PlayerDataHelper`

**`model/`** - Bot-specific JPA entities stored in MariaDB (not embedded H2). These implement `JpaModel` directly with manual `equals()`/`hashCode()`. Examples: `AppUser`, `AppGuild`, `AppEmoji`, `OptimizerMobType`, `SkyBlockEvent`.

**`data/skyblock/`** - Supplementary SkyBlock data models (also JPA entities). These extend the `minecraft-api` model layer with additional bonus/shop data: `BonusArmorSetModel`, `BonusItemStatModel`, `BonusReforgeStatModel`, `HotPotatoStatModel`, `ShopBitTypeModel`, etc.

**`processor/`** - `Processor<R>` base class for syncing Hypixel resource API responses into the database. Subclasses: `ResourceItemsProcessor`, `ResourceSkillsProcessor`, `ResourceCollectionsProcessor`.

**`util/`** - Shared utilities:
- `SkyBlockUserCommand` - Abstract base for player-lookup commands; provides common parameters (`name`, `profile`), embed builders, and skill embed formatting
- `SkyBlockUser` - Resolves a Minecraft player from command arguments or linked Discord account; fetches profiles, guild, session, and auctions in the constructor
- `ItemCache` - Caches auction house, bazaar, and ended auctions from Hypixel API with expiry-based refresh

### Key Architectural Patterns

- **`SkyBlockUserCommand` template method**: `process()` is final and creates a `SkyBlockUser`, then calls abstract `subprocess()`. All player-facing commands follow this.
- **`ProfileStats` as computation hub**: Constructed from a `SkyBlockIsland` + `SkyBlockMember`, it loads every stat source, applies bonus calculations, and provides `getCombinedStats()` for total stat aggregation. The optimizer consumes this directly.
- **Optimizer flow**: `OptimizerRequest.of(username)` (builder) -> `Optimizer.solve(request)` -> `OptimizerResponse` containing solution, reforge counts, and final damage. The `Solution` base class handles reforge pruning (dominance elimination) and stat aggregation.
- **Two JPA entity sets**: `model/` entities are bot-application data (users, guilds, settings). `data/skyblock/` entities are game data (bonus stats, shop data). Both live in MariaDB and are accessed via `SimplifiedApi.getRepository(ModelClass.class)`.

### Optimizer Math (README Summary)

**Damage Per Hit**: Maximizes `(100 + critDamage) * (100 + strength)` by allocating reforges. The quadratic expression is decomposed into const + linear + quadratic terms. OptaPlanner assigns reforges to items using incremental solving with a `.join` function and a `HundredReforgeProblemFact` to represent `100 + base_stat`.

**Damage Per Second**: Extends DPH with attack speed (`fer`, `as`) and crit chance (`cc`) variables: `averageDamagePerHit * hitsPerSecond`. Too many variables for CPLEX, so it relies on OptaPlanner's heuristic search.

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **simplified-bot** (979 symbols, 2452 relationships, 79 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## When Debugging

1. `gitnexus_query({query: "<error or symptom>"})` — find execution flows related to the issue
2. `gitnexus_context({name: "<suspect function>"})` — see all callers, callees, and process participation
3. `READ gitnexus://repo/simplified-bot/process/{processName}` — trace the full execution flow step by step
4. For regressions: `gitnexus_detect_changes({scope: "compare", base_ref: "main"})` — see what your branch changed

## When Refactoring

- **Renaming**: MUST use `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` first. Review the preview — graph edits are safe, text_search edits need manual review. Then run with `dry_run: false`.
- **Extracting/Splitting**: MUST run `gitnexus_context({name: "target"})` to see all incoming/outgoing refs, then `gitnexus_impact({target: "target", direction: "upstream"})` to find all external callers before moving code.
- After any refactor: run `gitnexus_detect_changes({scope: "all"})` to verify only expected files changed.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Tools Quick Reference

| Tool | When to use | Command |
|------|-------------|---------|
| `query` | Find code by concept | `gitnexus_query({query: "auth validation"})` |
| `context` | 360-degree view of one symbol | `gitnexus_context({name: "validateUser"})` |
| `impact` | Blast radius before editing | `gitnexus_impact({target: "X", direction: "upstream"})` |
| `detect_changes` | Pre-commit scope check | `gitnexus_detect_changes({scope: "staged"})` |
| `rename` | Safe multi-file rename | `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` |
| `cypher` | Custom graph queries | `gitnexus_cypher({query: "MATCH ..."})` |

## Impact Risk Levels

| Depth | Meaning | Action |
|-------|---------|--------|
| d=1 | WILL BREAK — direct callers/importers | MUST update these |
| d=2 | LIKELY AFFECTED — indirect deps | Should test |
| d=3 | MAY NEED TESTING — transitive | Test if critical path |

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/simplified-bot/context` | Codebase overview, check index freshness |
| `gitnexus://repo/simplified-bot/clusters` | All functional areas |
| `gitnexus://repo/simplified-bot/processes` | All execution flows |
| `gitnexus://repo/simplified-bot/process/{name}` | Step-by-step execution trace |

## Self-Check Before Finishing

Before completing any code modification task, verify:
1. `gitnexus_impact` was run for all modified symbols
2. No HIGH/CRITICAL risk warnings were ignored
3. `gitnexus_detect_changes()` confirms changes match expected scope
4. All d=1 (WILL BREAK) dependents were updated

## Keeping the Index Fresh

After committing code changes, the GitNexus index becomes stale. Re-run analyze to update it:

```bash
npx gitnexus analyze
```

If the index previously included embeddings, preserve them by adding `--embeddings`:

```bash
npx gitnexus analyze --embeddings
```

To check whether embeddings exist, inspect `.gitnexus/meta.json` — the `stats.embeddings` field shows the count (0 means no embeddings). **Running analyze without `--embeddings` will delete any previously generated embeddings.**

> Claude Code users: A PostToolUse hook handles this automatically after `git commit` and `git merge`.

## CLI

| Task | Read this skill file                                |
|------|-----------------------------------------------------|
| Understand architecture / "How does X work?" | `~/.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `~/.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `~/.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `~/.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `~/.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `~/.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
