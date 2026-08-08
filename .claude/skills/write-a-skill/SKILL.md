---
name: write-a-skill
description: Create, restructure, and validate agent skills — frontmatter, progressive disclosure, bundled references, and the six quality gates. Use when writing a new skill, rewriting or expanding an existing one, splitting an oversized SKILL.md, fixing a skill description, or auditing everything in .claude/skills/.
license: MIT
---

# Writing Skills

Every skill in this project follows the rules below. They are enforced by the three
validators in `scripts/` — the checklist runner is the gate.

## Process

1. **Gather requirements** — what task/domain does the skill cover? Which specific use
   cases? Does it need executable scripts or just instructions? Any reference material?
2. **Draft** — SKILL.md with concise instructions; reference files for detail that does
   not fit; scripts for deterministic operations.
3. **Review** — run the checklist runner, then confirm with the person who asked: does it
   cover the use cases, is anything missing, is any section over/under-detailed?

## Skill structure

```
skill-name/
├── SKILL.md            # Main instructions (required, ≤100 lines)
├── references/         # Deep detail, flat — one level only
│   └── <topic>.md
└── scripts/            # Deterministic helpers (optional)
    └── helper.py
```

## SKILL.md shape

```md
---
name: skill-name
description: What it does. Use when [specific triggers].
---

# Skill Name

## Quick start
[minimal working example]

## Workflows
[step-by-step, checklists for complex tasks]

## Advanced
See [references/topic.md](references/topic.md).
```

## Description requirements

The description is **the only thing the agent sees** when deciding which skill to load —
a routing signal, not marketing copy. Max 1024 chars (target 100–300), third person, first
sentence = what it does (open with a concrete action verb), second sentence =
`Use when <specific triggers>`.

Good: `Extract text and tables from PDF files, fill forms, merge documents. Use when
working with PDF files or when the request mentions PDFs, forms, or document extraction.`
Bad: `Helps with documents.` — no verb, no trigger, indistinguishable from siblings.
See [references/description_design_patterns.md](references/description_design_patterns.md).

## When to add scripts

Add a script when the operation is deterministic (validation, formatting), when the same
code would otherwise be regenerated repeatedly, or when errors need explicit handling.

## When to split files

Split into `references/` when SKILL.md exceeds 100 lines, when content spans distinct
domains, or when advanced material is rarely needed. Replace the moved section with a
one-line pointer. Keep `references/` flat and keep a minimum example in SKILL.md.
See [references/progressive_disclosure_principles.md](references/progressive_disclosure_principles.md).

## Review checklist (the gate)

- [ ] Description includes a trigger (`Use when ...`)
- [ ] SKILL.md under 100 lines
- [ ] No time-sensitive info (dates, "as of <year>")
- [ ] Consistent terminology
- [ ] Concrete examples included (≥1 code block)
- [ ] References one level deep

Run all six programmatically before finishing:

```bash
python3 .claude/skills/write-a-skill/scripts/skill_review_checklist_runner.py .claude/skills/<name>/
```

Verdict must be PASS. See
[references/quality_gates_for_skills.md](references/quality_gates_for_skills.md) for why
each gate exists, and [references/companion_tooling.md](references/companion_tooling.md)
for the validator catalogue and this project's skill conventions.

---

Derived from alirezarezvani/claude-skills (MIT), itself derived from Matt Pocock's
`write-a-skill` (MIT). Principles preserved; validators and project conventions added.
