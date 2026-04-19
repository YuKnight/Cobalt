# Cobalt WAM Event Validation Orchestrator

You are the lead orchestrator for WAM event validation.
Your job is to verify that every event spec in `com.github.auties00.cobalt.wam.event` is emitted by Cobalt in the same code paths where WhatsApp Web emits it.

The user invokes this command as: `/wam`

## Preconditions

- Work from the Cobalt repository root.
- Preserve existing outputs under `wam-validation/` unless re-running intentionally.

### Verify MCP Server

Before doing anything else, verify the whatsapp MCP server is reachable:

1. Run `curl -s -o /dev/null -w "%{http_code}" http://localhost:8787/mcp` (or call any lightweight MCP tool like `mcp__whatsapp__get_active_snapshot`).
2. If the server is NOT running, start it:
   ```bash
   cd tooling/web-mcp-server-new && node dist/index.js &
   ```
   Wait a few seconds, then re-check. The server runs on port 8787 by default in HTTP mode.
3. If the server cannot be started (missing build, missing data), stop and tell the user.

## Output Layout

```
wam-validation/
  manifest.json          # Complete event list (ground truth checklist)
  plan.md                # Human-readable validation plan
  reports/               # Per-event reports from agents
    <EventClassName>.md
  report.md              # Final synthesis
```

---

## Phase 1: Discovery (You Do This Inline)

Discovery builds the event list. Do this yourself, do NOT delegate.

### Step 1.1: Enumerate Event Specs

1. Glob `modules/lib/src/main/java/com/github/auties00/cobalt/wam/event/*.java`.
2. For every file, read it and extract:
   - Fully qualified class name (e.g., `AddressingModeMismatchEvent`).
   - WAM event id from the `@WamEvent(id = N)` annotation.
   - The camelCase base name (class name minus the trailing `Event` suffix, first letter lowercased — e.g., `addressingModeMismatch`).
   - The list of `@WamProperty` fields with their indices, types, and getter names.
3. Count the files and cross-check the count against the glob result. Every file must appear in the enumeration. If counts differ, you silently dropped files — fix and re-count.

### Step 1.2: User Confirmation

Present to the user:

- The total number of event files found (with explicit count reconciliation).
- The first 20 and last 20 event class names as a sample (do not list all 300+).
- A note that the command will spawn one agent per event, sequentially, and that the run may be long.

Ask the user to confirm:

- Proceed with ALL events, or
- Scope to a subset (e.g., a glob pattern, a prefix filter, or a specific event name).

Do NOT proceed to Phase 2 until the user confirms. If the user scopes to a subset, filter the enumeration accordingly before continuing.

---

## Phase 2: Manifest Building (You Do This Inline)

### Step 2.1: Write the Manifest

Write `wam-validation/manifest.json`:

```json
{
  "timestamp": "<ISO timestamp>",
  "events": [
    {
      "className": "AddressingModeMismatchEvent",
      "eventId": 4750,
      "camelCaseName": "addressingModeMismatch",
      "filePath": "modules/lib/src/main/java/com/github/auties00/cobalt/wam/event/AddressingModeMismatchEvent.java",
      "properties": [
        { "index": 1, "type": "ENUM", "name": "iqResponse" },
        { "index": 2, "type": "ENUM", "name": "localAddressingMode" }
      ],
      "status": "pending"
    }
  ],
  "totalEvents": 0
}
```

### Step 2.2: Write the Plan

Write `wam-validation/plan.md`:

- Total events to validate (with filter reasoning if scoped).
- Table of class name, event id, property count.
- Note: one agent per event, sequential execution.

---

## Phase 3: Per-Event Validation (Sequential Agents, Strict 1:1)

For EACH event in the manifest, spawn exactly one `validate-wam-event` agent.

**Hard rules:**

- One event per agent. Never batch multiple events into one agent.
- Sequential execution. Do NOT run agents in parallel, do NOT use `run_in_background`.
- Agents work directly on the main codebase. No worktrees.
- Do not cancel running agents. Wait for each to finish before starting the next.
- If an agent fails or produces no report, record the failure in the manifest and move to the next event.

### Spawning Loop

For each event in the manifest, in the order they appear in the manifest:

1. Update the event's `status` to `"in-progress"` in the manifest.
2. Spawn a single `validate-wam-event` agent with the prompt below.
3. Wait for it to complete.
4. Read its report from `wam-validation/reports/<EventClassName>.md`.
5. Update the event's `status` to the verdict from the report (`match`, `missing-emitters`, `missing-flow`, `skipped-telemetry-only`, `error`).
6. Move to the next event.

### Prompt Template

```
Validate Cobalt's emission of WAM event `{className}` (id {eventId}) against WhatsApp Web.

## Event Spec
- Class: `{className}`
- Event ID: `{eventId}`
- CamelCase base name: `{camelCaseName}`
- Spec file: `{filePath}`
- Properties: {list of {index, type, name}}

## Your Job
1. Locate every WA Web call site that logs this event (by event id, by camelCase name, by class name, by event-builder function).
2. For each call site, identify the surrounding flow (user action, network event, state transition, lifecycle hook).
3. Find the equivalent Cobalt code path.
4. Verify Cobalt emits the event via `wamService.commit(new {className}Builder()...build())` in that path.
5. If the emission is missing and the flow exists in Cobalt, add the commit call using the correct builder and properties.
6. If the flow itself is missing in Cobalt, report `MISSING_IN_COBALT_FLOW` (do NOT fabricate flows).
7. If WA Web never emits this event (it is telemetry-only with no sender, or only referenced by its definition), report `TELEMETRY_ONLY` and skip.

## Report Output Path
wam-validation/reports/{className}.md
```

---

## Phase 4: Synthesis (You Do This Inline)

### Step 4.1: Completeness Check

For every event in the manifest, verify the `status` is one of:

- `match` — Cobalt already emits in every WA Web call site
- `missing-emitters-fixed` — Cobalt was missing emissions; agent added them
- `missing-flow` — WA Web emits in flows Cobalt does not implement; agent reported it
- `telemetry-only` — WA Web has no emitter; only the spec exists
- `error` — agent failed; human follow-up required

Any event with `status: "pending"` means the validation is incomplete; re-run the missing agents.

### Step 4.2: Final Compilation

Do NOT run `mvn compile` during this command — the user uses their IDE for diagnostics.

### Step 4.3: Write Synthesis Report

Write `wam-validation/report.md`:

```markdown
# WAM Event Validation Report

## Summary
- Events validated: N
- match: N
- missing-emitters-fixed: N
- missing-flow: N
- telemetry-only: N
- error: N

## Events With Added Emissions
### {EventClassName} (id {eventId})
- Added emission(s) in: {file:line}, ...

## Events Missing Underlying Flow
### {EventClassName} (id {eventId})
- WA Web emits in: {WA Web module and function}
- Cobalt flow not found: {short explanation}

## Telemetry-Only (No WA Web Emitter)
- {EventClassName}, {EventClassName}, ...

## Errors
- {EventClassName}: {error summary}
```

---

## Rules

- **Exhaustiveness is mandatory.** Every event in the (possibly scoped) manifest must have a final verdict.
- **1:1 agent-to-event mapping is mandatory.** Never batch events.
- **Sequential execution.** No parallelism, no `run_in_background`.
- **Do not run `mvn compile`.** The user relies on IDE diagnostics.
- **Do not fabricate flows.** If WA Web emits in a flow Cobalt does not implement, report it as `missing-flow` — do not invent code to satisfy the event.
- **Respect the `WamService.commit(...)` pattern.** Emissions go through `wamService.commit(new {className}Builder()....build())`, not direct writes.
- **Preserve existing outputs** under `wam-validation/` unless explicitly re-running.