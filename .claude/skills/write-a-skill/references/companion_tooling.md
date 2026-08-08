# Companion Tooling & Project Skill Conventions

Two things: the validators bundled with this skill, and how skills are organised in the
Verborum Android project.

## Validators (stdlib Python, no dependencies)

| Script | Checks | Run |
|---|---|---|
| `scripts/skill_description_validator.py` | Description ≤1024 chars, third person, `Use when` trigger, action verb in first sentence | On the first draft |
| `scripts/skill_structure_validator.py` | SKILL.md present, ≤100 lines, references flat, no circular refs | Before staging |
| `scripts/skill_review_checklist_runner.py` | All six review-checklist items at once | Final gate |

All three take a skill folder (or a SKILL.md for the description validator), support
`--output json`, and exit non-zero on WARN/FAIL.

Sweep the whole library:

```bash
for d in .claude/skills/*/; do
  python3 .claude/skills/write-a-skill/scripts/skill_review_checklist_runner.py "$d" \
    --output json | python3 -c 'import json,sys; r=json.load(sys.stdin); print(r["overall"], r["folder"])'
done
```

## Project skill conventions (Verborum Android)

- Skills live in `.claude/skills/<name>/SKILL.md` and are committed (team config).
  Specialist subagents live in `.claude/agents/` and name the skills they must read.
- Every skill is scoped to one area and says what it does *not* cover, pointing at its
  sibling by name (`android-unit-test` ↔ `android-integration-test`).
- `android-dev` is the router: any new production-code skill must be added to its routing
  table, to the table in the root `CLAUDE.md`, and cross-referenced from the siblings that
  should defer to it.
- Detail that exceeds the 100-line ceiling goes to `references/<topic>.md` in that skill's
  folder — flat, one level, named for the topic rather than the caller.
- Terminology: the checklist runner flags a file that uses both words of a synonym pair
  (`skill`/`tool`, `user`/`developer`, `agent`/`bot`). Pick one word per concept.
- Versions, SDK levels, and dependency numbers belong in `gradle-toolchain` or the version
  catalogue, not scattered across skills — they rot and trip the time-sensitivity gate.

## Verdicts

PASS is required for a skill to be considered done. A WARN (one failing check) must be
justified in the response that introduces it; anything worse gets fixed before finishing.

## Attribution

Derived from
[alirezarezvani/claude-skills — engineering/write-a-skill](https://github.com/alirezarezvani/claude-skills/tree/main/engineering/write-a-skill)
(MIT), itself derived from
[mattpocock/skills — write-a-skill](https://github.com/mattpocock/skills/tree/main/skills/productivity/write-a-skill)
(MIT). The upstream also ships a `cs-skill-author` persona agent and a
`/cs:write-a-skill` slash command; neither is installed here — the validators plus this
skill cover the same ground.

---

**Source authorities (non-exhaustive):**

- **Matt Pocock — write-a-skill** (https://github.com/mattpocock/skills/, MIT) — the upstream source
- **alirezarezvani/claude-skills** (https://github.com/alirezarezvani/claude-skills, MIT) — the validator layer this skill installs
- **Anthropic — Skills documentation** (https://docs.claude.com/en/docs/agents/skills) — official guidance on skill structure
- **Anthropic Engineering Blog — context engineering** — concise context lowers misrouting
- **Pareto principle applied to documentation** — most invocations use the same small slice of a skill
- **Hyrum's Law applied to skill descriptions** — once a description shape is observed, downstream routing depends on it
