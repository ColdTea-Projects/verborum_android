---
name: git-workflow
description: Manage git state in the Verborum Android repo — verify the project hook's automatic staging of newly created files, stage Edit-modified files, write commit messages in the project's style, and keep build outputs, local.properties, keystores, and scratch files out of history. Use when creating files, staging, committing, or checking repo state.
---

# Git Workflow (Verborum)

## Automatic staging of new files (project hook)

This repo has a **PostToolUse hook** in `.claude/settings.json`: every file created (or overwritten) with the **Write tool** is immediately `git add`-ed. The harness runs it, and it applies to subagents too.

**Trust but verify — the hook can be inactive.** Hooks only load from settings files that existed when the session started; if `.claude/settings.json` was created or changed mid-session, the hook is dormant until the user runs `/hooks` or restarts. Therefore:

- **After creating files, verify staging** with `git status --short -- <paths>`. Anything still `??` means the hook didn't fire — stage it yourself: `git add -- <paths>`. New files must never be left untracked at the end of a task.
- Files **modified with Edit are NOT auto-staged** (the hook matches Write only). Stage them explicitly when preparing a commit: `git add -- <files>`.
- Files outside the repo (scratchpad, memory) are silently skipped by the hook — that's by design, not a failure.
- `git status` in this repo usually shows new files as `A` (staged) and edited files as ` M` (unstaged). Reconcile with `git add` at commit time.
- If a Write-created file was a mistake, remember it may be staged: remove with `git rm -f -- <file>` (or `git restore --staged` + delete).
- If verification shows the hook is dormant, tell the user to run `/hooks` once (or restart) — the model cannot reload settings itself.

## Staging & commit conventions

- **Never commit or push unless the user explicitly asks.** Auto-staging ≠ auto-committing.
- Before committing: `git status` + `git diff --staged` — review what's actually going in; stage Edit-modified files deliberately.
- Commit messages: imperative, concise, lowercase style matching history (`implement logo`, `fix test screen & implement debouncer`). End with:
  `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`
- One logical change per commit (feature, fix, or build change — not a mix).
- Branching: work lands on `main` (solo project); branch first if asked to prepare a PR.

## Never commit

- `build/` outputs, `local.properties`, `.gradle/`, keystores/credentials (already gitignored — don't force-add).
- `.claude/settings.local.json` (personal overrides) — keep out of the repo; `.claude/settings.json`, skills, and agents ARE committed (team config).
- Scratchpad/temp files. If one was created inside the repo by accident, unstage it (the hook will have staged it).

## Quick reference

```bash
git status --short                 # A = staged new file (hook), M = modified (stage manually)
git add -- <edited-files>          # stage Edit-modified files at commit time
git restore --staged -- <file>     # undo the hook's staging for an unwanted file
git log --oneline -5               # match commit message style
```
