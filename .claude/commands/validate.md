# Cobalt Validation Orchestrator

You are the lead validation orchestrator for the Cobalt project.
Your job is to validate that a given feature area is implemented correctly and exhaustively by comparing Cobalt's Java code against WhatsApp Web's JavaScript source via MCP tools.

The user invokes this command as: `/validate <feature-area>`

## Preconditions

- Work from the Cobalt repository root.
- Preserve existing validation outputs under `validation/<feature>/` unless re-running intentionally.

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
validation/<feature>/
  manifest.json          # Complete export-to-method mapping (ground truth checklist)
  plan.md                # Human-readable validation plan
  reports/               # Per-module validation reports from agents
    <ModuleName>.md
  report.md              # Final synthesis
```

Replace `<feature>` with a short kebab-case name (e.g., `app-state-sync`, `newsletter`, `group-management`).

---

## Phase 1: Discovery (You Do This Inline)

Discovery builds the module list. Do this yourself, do NOT delegate to agents.

### Step 1.1: Cobalt Source Scan

1. Glob for Java files in packages likely related to the feature area.
2. Read candidate files and extract:
   - `@implNote` tags naming WA Web modules and functions.
   - String constants, `ACTION_NAME`, `QUERY_ID`, enum names, stanza tags.
   - Superclass/interface/import relationships.
3. Follow transitive references: if a file imports or registers another class, that class is a candidate too.
4. Search `src/main/java/com/github/auties00/cobalt/node/binary/NodeTokens.java` for protocol identifiers related to the feature.

### Step 1.2: WA Web Module Search

Using MCP tools, find all WA Web modules for this feature:

1. `mcp__whatsapp__search_modules` with the feature name, synonyms, abbreviations, and WA prefixes (`WAWeb`, `WA`, `WAMsg`, etc.). Try at least 10-15 keyword variations.
2. `mcp__whatsapp__search_code` with `searchIn: "literals"` for every constant/identifier found in Cobalt source.
3. `mcp__whatsapp__search_code` with `searchIn: "source"` for Cobalt class names that map to WA Web modules.
4. For every discovered module: `mcp__whatsapp__trace_dependencies` with `direction: "forward"` depth 2 and `direction: "reverse"` depth 2.
5. For every newly discovered module from traces: `mcp__whatsapp__get_module_metadata` to assess relevance.
6. Filter aggressively: keep only modules with behavioral relevance to the feature, not generic utilities.

### Step 1.3: Cross-Check

1. List all Java files in the Cobalt packages discovered.
2. For each file, verify it appears in the discovered set.
3. For each WA Web module found, verify it's accounted for.
4. If any discovered file or module is unaccounted, investigate and add it.

---

## Phase 2: Manifest Building (You Do This Inline)

This is the most critical phase. The manifest is the ground truth checklist that guarantees exhaustiveness.

### Step 2.1: Enumerate All Exports

For each WA Web module in the discovery results:

1. `mcp__whatsapp__get_exports` to get the complete export list.
2. For each export: `mcp__whatsapp__resolve_export` to get the implementing symbol.
3. Record: module name, export name, symbol name, symbol kind (function/class/variable).

### Step 2.2: Build the Mapping

For each WA Web export:

1. Check if any Cobalt method has an `@implNote` referencing this module and function.
2. If yes: record the mapping (WA export -> Cobalt method).
3. If no: search Cobalt source for methods with similar names, similar logic, or similar constants. If found, record as "probable mapping." If not found, record as "unmapped" (MISSING_IN_COBALT candidate).

For each Cobalt method in the relevant files:

1. Check if its `@implNote` maps to a discovered WA Web export.
2. If yes: already mapped above.
3. If no `@implNote` or the referenced module wasn't found: record as "unmapped Cobalt method" (MISSING_IN_WA_WEB candidate).

### Step 2.3: Write the Manifest

Write `validation/<feature>/manifest.json`:

```json
{
  "feature": "<feature>",
  "timestamp": "<ISO timestamp>",
  "modules": [
    {
      "waModule": "WAWebModuleName",
      "cobaltFiles": ["src/main/java/.../File.java"],
      "exports": [
        {
          "exportName": "functionA",
          "symbolName": "functionA",
          "symbolKind": "function",
          "cobaltMethod": "File.java#methodA",
          "mappingSource": "implNote | nameMatch | unmapped",
          "status": "pending"
        }
      ],
      "unmappedCobaltMethods": [
        {
          "file": "File.java",
          "method": "orphanMethod",
          "status": "pending"
        }
      ]
    }
  ],
  "totalExports": 0,
  "totalMapped": 0,
  "totalUnmappedExports": 0,
  "totalUnmappedMethods": 0
}
```

### Step 2.4: Write the Plan

Write `validation/<feature>/plan.md` with:

- Discovery coverage summary (keywords searched, modules found, Cobalt files found).
- Full module-to-file mapping table.
- Export counts per module.
- Unmapped exports and methods.
- Validation agent assignments (which modules will be validated together).

---

## Phase 3: Validation (Delegate to Agents)

Spawn `validate-module` sub-agents to do the actual comparison and fix work.

### Agent Assignment

Each agent gets exactly ONE WA Web module and its Cobalt counterpart(s). Optimize for granularity:

- Correct: one agent for `WAWebSyncdResponseParser` against `MutationResponseParser.java`
- Wrong: one agent for a broad "sync layer" bundling unrelated modules

Very small utility modules with one or two exports may be grouped with their parent ONLY when they are inseparable implementation details of the same behavior.

### Spawning

1. Spawn agents in batches of up to 5, using `isolation: "worktree"` for each.
2. Each agent prompt must include:
   - The WA Web module name
   - The full export list for that module (from manifest)
   - The Cobalt file path(s)
   - The export-to-method mapping (from manifest)
   - The unmapped exports and methods for that module
   - The output path: `validation/<feature>/reports/<ModuleName>.md`
3. Use `run_in_background: true` so agents run concurrently.
4. Wait for each batch to complete before spawning the next.

### Prompt Template

```
Validate WA Web module `{waModule}` against Cobalt.

## WA Web Module
`{waModule}`

## Exports to Validate
{for each export: exportName -> cobaltMethod or "unmapped"}

## Cobalt Files
{list of Java file paths}

## Unmapped Cobalt Methods
{methods with no WA Web counterpart}

## Report Output Path
validation/{feature}/reports/{waModule}.md

Validate every export exhaustively. Fix all issues. Write the report.
```

---

## Phase 4: Integration and Synthesis (You Do This Inline)

After all agents complete:

### Step 4.1: Collect Results

1. Read every module report from `validation/<feature>/reports/`.
2. For each agent that ran in a worktree: check if it made changes (the agent result will include the worktree path and branch if changes were made).

### Step 4.2: Integrate Changes

For each agent that produced changes in a worktree:

1. Review the changes with `git diff` in the worktree.
2. Cherry-pick the commit(s) into the main branch.
3. After each cherry-pick batch (5-10 commits), verify compilation:
   ```
   mvn compile -pl . -q "-Dcobalt.build.dir=target-validate-batch-N"
   ```
4. Delete the batch build directory after successful compilation.
5. If a cherry-pick conflicts, resolve trivially or re-run the specific module agent.

### Step 4.3: Completeness Check

Update the manifest: for every export, verify it has a verdict from a module report.

- If any export has `status: "pending"` still, the validation is INCOMPLETE. Investigate and re-run.
- Every export must be one of: MATCH, MISMATCH (fixed), MISSING_IN_COBALT (implemented), ADAPTED, or SKIPPED (WAM/telemetry with reason).

### Step 4.4: Final Compilation

```
mvn compile -pl . -q "-Dcobalt.build.dir=target-validate-final"
```

Delete `target-validate-final` after success.

### Step 4.5: Write Synthesis Report

Write `validation/<feature>/report.md`:

```markdown
# Validation Report: <feature>

## Summary
- Modules validated: N
- Total exports validated: N / N
- MATCH: N
- MISMATCH: N (all fixed)
- MISSING_IN_COBALT: N (all implemented)
- MISSING_IN_WA_WEB: N (all resolved)
- ADAPTED: N
- SKIPPED: N (WAM/telemetry)

## Issues Fixed

### [Module] Issue 1: description
- Category: MISMATCH | MISSING_IN_COBALT | MISSING_IN_WA_WEB
- Fix: [description]

## Remaining ADAPTED Items

### [Module] Item 1: description
- Why: [reason]

## Completeness
- All exports accounted for: Yes/No
- Compilation verified: Yes/No

## Per-Module Summary

| Module | Exports | MATCH | MISMATCH | MISSING_COBALT | MISSING_WA | ADAPTED | SKIP |
|--------|---------|-------|----------|----------------|------------|---------|------|
| ...    | N       | N     | N        | N              | N          | N       | N    |
```

---

## Rules

- **Exhaustiveness is mandatory.** Every WA Web export must have a verdict. The manifest is the checklist.
- Validate feature and behavior parity, not file structure parity.
- Cast a wide net during discovery. Better to surface false positives than miss modules.
- Prefer the smallest defensible validation unit. One module per agent.
- Do not cancel running agents. Wait for them to finish.
- Every issue must be fixed, not only reported.
- Do not skip compilation verification.
- Cherry-pick agent commits into the main branch. Do not merge worktree branches.
- When searching WA Web, try multiple keyword forms: synonyms, abbreviations, subfeatures.