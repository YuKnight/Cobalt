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
2. Include ALL files in every discovered package and its subpackages. Do not exclude files because they seem like a "separate concern" — if they live in a feature package, they are part of the feature.
3. Read candidate files and extract:
   - `@implNote` tags naming WA Web modules and functions.
   - String constants, `ACTION_NAME`, `QUERY_ID`, enum names, stanza tags.
   - Superclass/interface/import relationships.
4. Follow transitive references: if a file imports or registers another class, that class is a candidate too.
5. Search `modules/lib/src/main/java/com/github/auties00/cobalt/node/binary/NodeTokens.java` for protocol identifiers related to the feature.

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

### Step 1.4: User Confirmation

Present EVERY Cobalt file discovered in Step 1.1 — do not summarize, abbreviate, or omit files that seem less critical. Verify the count of files you present matches the count from your glob results. If they differ, you silently dropped files.

- Total Cobalt files found with explicit count reconciliation (glob returned N files, presenting N files)
- List every file, grouped by subpackage for readability but with no omissions
- Total WA Web modules found (list them)
- Any packages or subpackages that were discovered

Ask the user to confirm the scope is correct, or to add/remove items. Do NOT proceed to manifest building until the user confirms.

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
      "cobaltFiles": [".../src/main/java/.../File.java"],
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

### Step 2.4: Coverage Check

Before writing the plan, verify full coverage from both sides:

1. Every discovered Cobalt file must appear as an owned file in at least one agent assignment. If any file is unowned, find its WA Web module(s) and create an agent for it.
2. Every discovered WA Web module must appear in at least one agent assignment. If any module is unassigned, find its Cobalt counterpart(s) and create an agent for it.
3. The plan is not complete until both checks pass.

### Step 2.5: Build the Dependency Graph and Topological Order

This step is critical. Validation order determines whether dependencies are in place when consumers run, which determines whether agents can fix issues in their own files or have to flag them as context-file issues.

For every Cobalt file in the discovered set, parse its `import` statements and build a directed graph where `A -> B` means "file A imports from file B AND B is also in the discovered set." Imports outside the discovered set are ignored — they're external dependencies, not part of this validation pass.

Use `grep -h "^import com.github.auties00" <file>` or read each file directly to extract imports. Resolve each import to a discovered file path via the package-to-path mapping.

Then:

1. Compute strongly-connected components (SCCs). Files in the same SCC have circular dependencies and must be validated together as one unit.
2. Topologically sort the SCCs. Leaves (no outgoing edges to discovered files) come first; top-level consumers (no incoming edges) come last.
3. Within an SCC, the agent owns all files in that component.
4. Record the order in the manifest as a `validationOrder` array.

The result is a strict order where every agent runs only after all its Cobalt-internal dependencies have been validated. This means by the time a consumer agent runs:

- The store accessors it needs already exist (the store agent ran earlier)
- The action model fields it needs already exist (the model agent ran earlier)
- The utility methods it should delegate to already exist (the utility agent ran earlier)
- The exception subtypes it needs already exist (the exception agent ran earlier)

Each agent must validate its file against its WA Web counterpart EXHAUSTIVELY — adding every export WA Web has, regardless of whether any current consumer uses it. This is what makes the dependency ordering work: leaves are made complete before consumers ask anything of them.

### Step 2.6: Write the Plan

Write `validation/<feature>/plan.md` with:

- Discovery coverage summary (keywords searched, modules found, Cobalt files found).
- Full module-to-file mapping table.
- Export counts per module.
- Unmapped exports and methods.
- Validation agent assignments in topological order (which modules will be validated together, in dependency order).
- Coverage: N/N Cobalt files and N/N WA Web modules covered by agent assignments.
- Dependency layers: explicit listing of which files are leaves, which are middle, which are top-level consumers.

---

## Phase 3: Validation (Sequential Agents in Topological Order)

Spawn `validate-module` sub-agents one at a time, in the topological order computed in Step 2.5. Each agent works directly on the main codebase — no worktrees, no merging. This means:
- Each agent sees the full codebase including all fixes from previous agents.
- No merge conflicts. No silent overwrites.
- Dependencies are validated before consumers, so by the time a consumer runs, every utility, store accessor, model field, and exception type it needs already exists.
- Shared files (e.g., `WhatsAppStore.java`) are edited in place and immediately available to subsequent agents.

### Agent Assignment

Each agent gets exactly ONE WA Web module and its Cobalt counterpart(s). This is a hard rule, not a guideline:

- Correct: one agent for `WAWebSyncdResponseParser` against `MutationResponseParser.java`
- Wrong: one agent for a broad "sync layer" bundling unrelated modules
- Wrong: batching multiple independent modules into one agent to reduce agent count

**Do not batch independent modules together for efficiency, practicality, or any other reason.** If discovery produces N modules, the plan must have N agents. The number of agents is a consequence of the module count, not something to be minimized. If you find yourself wanting to group modules to "keep the agent count manageable," that impulse is the exact thing this rule exists to prevent — each module deserves isolated, focused validation.

Very small utility modules with one or two exports may be grouped with their parent ONLY when they are inseparable implementation details of the same behavior (e.g., a `*Helper` or `*Const` module used exclusively by its parent). Two modules that happen to share the same Cobalt file or the same interface are NOT inseparable — they must still get separate agents.

### File Ownership Assignment

For each agent, determine precisely which files it owns (may edit) vs which files it needs for context (read-only):

- **Owned files**: Only the Java file(s) whose behavior directly corresponds to the WA Web module being validated. For example, if validating `WAWebSyncdResponseParser`, the owned file is `MutationResponseParser.java` — NOT the entire handler directory.
- **Context files**: Other files the agent may need to read to understand call interfaces, types, or dependencies (e.g., shared store classes, model classes, other handlers that are called). These are read-only.

Getting this right is critical. An agent that owns too many files will rewrite things outside its scope.

### Spawning

Spawn agents one at a time, in the topological order from Step 2.5:

1. Take the next module/SCC in the validation order.
2. Spawn a single `validate-module` agent (no `isolation: "worktree"`, no `run_in_background`).
3. Wait for it to complete.
4. Review its report from `validation/<feature>/reports/<ModuleName>.md`.
5. Verify compilation:
   ```
   mvn compile -pl . -q "-Dcobalt.build.dir=target-validate-<module-slug>"
   ```
6. Delete the build directory after successful compilation.
7. If compilation fails, inspect the agent's changes and fix the issue before continuing.
8. Move to the next module in the topological order. Repeat until all modules are validated.

Each agent must validate its file EXHAUSTIVELY against WA Web — adding every export the WA Web counterpart has, even if no current Cobalt consumer uses it. This is what makes dependency ordering work: by the time a consumer runs, every method/field/type it might need from a leaf is already in place.

Each agent prompt must include:
- The WA Web module name
- The full export list for that module (from manifest)
- The owned file(s) — precisely scoped
- The context file(s) — files needed for understanding but not editing
- The export-to-method mapping (from manifest)
- The unmapped exports and methods for that module
- The output path: `validation/<feature>/reports/<ModuleName>.md`

### Prompt Template

```
Validate WA Web module `{waModule}` against Cobalt.

## WA Web Module
`{waModule}`

## Exports to Validate
{for each export: exportName -> cobaltMethod or "unmapped"}

## Owned Files (you may edit these)
{list of Java file paths this agent owns}

## Context Files (read-only, do not edit)
{list of Java file paths for reference only}

## Unmapped Cobalt Methods
{methods with no WA Web counterpart}

## Report Output Path
validation/{feature}/reports/{waModule}.md

Validate every export exhaustively against WA Web. Add every method/field/type WA Web has, even if no current Cobalt consumer uses it — leaves must be complete so consumers can rely on them. Fix all issues in owned files. Report issues found in context files without fixing them. Write the report.
```

---

## Phase 3.5: Cross-Cutting Flow Validation

After every module has been validated in topological order, spawn a single `validate-flow` agent to handle cross-file architectural patterns that no single-file validator can see. Examples of patterns this catches:

- "Module A inlines logic that should delegate to Module B's helper" (when both are correct in isolation but the call relationship is wrong)
- "Stream handler X processes raw response when WA Web's equivalent processes a decoded object" (type mismatch that spans the dispatcher and the service)
- "Service Y is called with the wrong JID variant from caller Z" (correct internally, wrong at the boundary)
- "Caller does N work that should be done in M batches" (per-key vs batched calls across files)

The orchestrator constructs a list of cross-cutting issues to investigate by collecting:

1. Every `## Issues in Context Files` section from Phase 3 module reports.
2. Any "should delegate to" / "should route through" / "double-decodes" findings noted by individual agents.
3. Any cross-file inconsistencies the orchestrator noticed while reviewing reports.

Spawn the `validate-flow` agent with:

- The list of cross-cutting issues from Phase 3 reports
- All discovered Cobalt files (read access)
- Edit access to any file in the discovered set
- The output path: `validation/<feature>/flow-report.md`

Verify compilation after the agent completes.

---

## Phase 3.6: Phantom Code Sweep

After cross-cutting flow validation, spawn a single `validate-phantom` agent to remove dead code introduced by previous validation passes or already present in the codebase.

The phantom sweep agent:

1. For every public/protected method, field, class, and enum constant in the discovered set, runs `Grep` to find references in the discovered files.
2. For any member with zero references in the discovered set, queries the WA Web MCP (`mcp__whatsapp__find_references`, `mcp__whatsapp__search_code`) to verify there is also no WA Web counterpart that justifies its presence.
3. If the member has neither Cobalt references nor WA Web justification, removes it.
4. Defensive null checks, `AutoCloseable` patterns, builder helpers, and other Java-specific adaptations are kept (these are `ADAPTED`, not phantom).

Spawn the `validate-phantom` agent with:

- All discovered Cobalt files (read and edit access)
- The output path: `validation/<feature>/phantom-report.md`

Verify compilation after the agent completes.

---

## Phase 4: Synthesis (You Do This Inline)

After all agents have completed (Phases 3, 3.5, and 3.6):

### Step 4.1: Completeness Check

Update the manifest: for every export, verify it has a verdict from a module report.

- If any export has `status: "pending"` still, the validation is INCOMPLETE. Investigate and re-run.
- Every export must be one of: MATCH, MISMATCH (fixed), MISSING_IN_COBALT (implemented), ADAPTED, or SKIPPED (WAM/telemetry with reason).

### Step 4.2: Final Compilation

```
mvn compile -pl . -q "-Dcobalt.build.dir=target-validate-final"
```

Delete `target-validate-final` after success.

### Step 4.3: Re-validation Pass (Safety Net)

If any agent in Phase 3 reported MISMATCH, MISSING_IN_COBALT, or MISSING_IN_WA_WEB issues (i.e., it made fixes), or if Phase 3.5 / 3.6 made changes, re-run the entire validation from Phase 3:

1. Clear the reports directory.
2. Re-run all module agents in topological order against the now-fixed codebase.
3. Verify compilation after each agent.
4. Re-run Phase 3.5 and Phase 3.6.
5. If the re-validation pass produces zero MISMATCH, zero MISSING_IN_COBALT, and zero MISSING_IN_WA_WEB across all agents, the validation is complete.
6. If it still finds issues, fix them and re-run again. Repeat until a clean pass is achieved.

With dependency ordering most issues should be fixed in the first pass, so re-validation usually converges quickly. It exists as a safety net to catch fixes introduced by one agent that broke another module's parity.

### Step 4.4: Write Synthesis Report

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
- **Topological order is mandatory.** Leaves first, consumers last. By the time a consumer runs, every utility, accessor, model field, and exception type it needs must already exist.
- **Leaves must be made complete against WA Web**, not just against current consumer needs. This is what makes dependency ordering work.
- Validate feature and behavior parity, not file structure parity.
- Cast a wide net during discovery. Better to surface false positives than miss modules.
- Prefer the smallest defensible validation unit. One module per agent.
- Do not cancel running agents. Wait for them to finish.
- Every issue must be fixed, not only reported.
- Do not skip compilation verification.
- Agents run sequentially on the main codebase. No worktrees, no merging. Verify compilation after each agent.
- When searching WA Web, try multiple keyword forms: synonyms, abbreviations, subfeatures.
- After per-module validation, run the cross-cutting flow agent (Phase 3.5) and the phantom sweep agent (Phase 3.6) before final synthesis.