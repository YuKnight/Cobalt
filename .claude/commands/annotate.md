# Cobalt Annotation Orchestrator

You are the lead annotation orchestrator for the Cobalt project.
Your job is to add source provenance annotations, high-quality javadocs, `@implNote` technical references, and inline method body comments to every Java file in the `lib` and `model` modules by comparing Cobalt's Java code against WhatsApp Web's JavaScript source via MCP tools.

The user invokes this command as: `/annotate`

## Preconditions

- Work from the Cobalt repository root.

### Verify MCP Server

Before doing anything else, verify the whatsapp MCP server is reachable:

1. Run `curl -s -o /dev/null -w "%{http_code}" http://localhost:8787/mcp` (or call any lightweight MCP tool like `mcp__whatsapp__get_active_snapshot`).
2. If the server is NOT running, start it:
   ```bash
   cd tooling/web-mcp-server-new && node dist/index.js &
   ```
   Wait a few seconds, then re-check. The server runs on port 8787 by default in HTTP mode.
3. If the server cannot be started (missing build, missing data), stop and tell the user.

---

## Phase 1: Package Discovery (You Do This Inline)

Discover every package that contains Java files in the two source trees:

1. Glob `modules/lib/src/main/java/com/github/auties00/cobalt/**/*.java`
2. Glob `modules/model/src/main/java/com/github/auties00/cobalt/**/*.java`
3. Extract the set of unique package directories (parent directory of each Java file).
4. For each package directory, collect the list of Java files it **directly** contains (not recursively — each sub-package is its own unit).
5. Exclude packages that contain zero non-`module-info.java` files.
6. Sort the packages alphabetically.

### Present to User

Present the full list of packages, grouped by module (`model` first, then `lib`), with file counts per package. Example:

```
## model (N packages, M files)
- com.github.auties00.cobalt.model.chat (5 files)
- com.github.auties00.cobalt.model.chat.group (3 files)
...

## lib (N packages, M files)
- com.github.auties00.cobalt.device (2 files)
- com.github.auties00.cobalt.device.adv (3 files)
...
```

Ask the user to confirm or adjust the scope before proceeding.

---

## Phase 2: Agent Spawning (Sequential, One Package at a Time)

For each package in the confirmed list, spawn a single `annotate-package` agent. Process packages sequentially — do NOT run agents in parallel or in background, because later packages may depend on annotations added by earlier agents.

### Ordering

Process `model` packages first (they are dependencies of `lib`), then `lib` packages. Within each module, process in alphabetical order.

### Agent Prompt

Each agent prompt MUST include:

1. **Package path**: the full package name (e.g., `com.github.auties00.cobalt.device.icdc`)
2. **File list**: every Java file in that package (absolute paths)
3. **Module**: whether this is `model` or `lib`

Use this template:

```
Annotate package `{packageName}` in the `{module}` module.

## Files to Annotate
{for each file: absolute path}

## Module
{model or lib}
```

### After Each Agent

1. Verify compilation:
   ```
   mvn compile -pl modules/source-meta,modules/model,modules/lib -q
   ```
2. If compilation fails, inspect the agent's changes and fix the issue before continuing.
3. Move to the next package.

---

## Phase 3: Final Verification (You Do This Inline)

After all agents have completed:

1. Run a full compile:
   ```
   mvn compile -pl modules/source-meta,modules/model,modules/lib -q
   ```
2. Spot-check 5-10 files across different packages to verify:
   - Source provenance annotations are present on types and members
   - Javadocs follow the required style
   - `@implNote` tags contain technical source references
   - Inline comments in method bodies follow the required format
3. Report the total number of packages processed and any issues found.

---

## Rules

- **One agent per package.** Do not batch multiple packages into one agent.
- **Sequential execution.** Do not run agents in parallel or in background.
- **Model before lib.** Process all `model` packages before any `lib` package.
- **Every file in every package must be annotated.** No skipping, no summarizing.
- **Verify compilation after every agent.** Do not proceed if compilation fails.
- **Do not cancel running agents.** Wait for them to finish.