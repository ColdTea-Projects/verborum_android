# Dictionary Tags API — Frontend Integration Guide

Endpoints for tagging a dictionary. Tags live on their own sub-resource (not inside the dictionary
payload) so tagging never requires re-sending the whole dictionary.

- **Service:** `ms_dictionary` (port **8085**)
- **Base path:** `/dictionaries/{dictionaryId}/tags`
- **Auth:** every call requires a valid Keycloak JWT — `Authorization: Bearer <access_token>`.
  A missing/invalid token → **401**. The owner is taken from the token (`sub` claim); never send a
  user id in the body or path.
- **Ownership:** tags follow the dictionary's ownership rules. Reads/writes on a dictionary you don't
  own behave as described per-endpoint below.

---

## Data types

### `DictionaryTagResponseDTO` (returned by GET)

| Field          | Type              | Notes                                              |
|----------------|-------------------|----------------------------------------------------|
| `tagId`        | string (UUID)     | Server-generated.                                  |
| `dictionaryId` | string            | The owning dictionary.                             |
| `tag`          | string            | Normalised: **trimmed + lower-cased** on write.    |
| `createdAt`    | string (ISO-8601) | e.g. `2026-07-24T09:15:30.123Z` (offset date-time).|

### `DictionaryTagRequestDTO` (body of POST)

| Field | Type   | Required | Notes                                          |
|-------|--------|----------|------------------------------------------------|
| `tag` | string | yes      | Must not be blank. No length cap (column is `TEXT`). |

> **Normalisation:** `"Food"`, `"food "`, and `"FOOD"` are all stored as `food`. Tags are grouping
> keys (marketplace browse + later AI aggregation), not display text. If you need a pretty label,
> render it client-side. Normalisation is applied on delete too, so deleting `"Food"` removes what
> `"food"` stored.

---

## Endpoints

### 1. List tags of a dictionary

```
GET /dictionaries/{dictionaryId}/tags
```

**Response `200 OK`** — a **bare JSON array** of `DictionaryTagResponseDTO` (no envelope):

```json
[
  {
    "tagId": "b3f1c2a4-5d6e-7f80-91a2-b3c4d5e6f7a8",
    "dictionaryId": "d-123",
    "tag": "food",
    "createdAt": "2026-07-24T09:15:30.123Z"
  }
]
```

- Dictionary not found **or not owned by the caller** → **404** (a not-owned dictionary is
  indistinguishable from a non-existent one, so ids can't be probed).

---

### 2. Add a tag

```
POST /dictionaries/{dictionaryId}/tags
Content-Type: application/json
```

```json
{ "tag": "Food" }
```

**Response `201 CREATED`** — the **envelope** shape (`Response`), not the tag DTO:

```json
{
  "status": 201,
  "message": "Saved successfully tag food",
  "path": "/dictionaries/d-123/tags",
  "timestamp": "2026-07-24T11:15:30.123+02:00"
}
```

Notes:
- `message` = a fixed prefix + the **normalised** tag. If you need the stored value, read it from
  `message` or re-fetch via GET.
- **Idempotent:** re-adding an existing tag also returns `201` (no duplicate created). Enforced by
  `UNIQUE (dictionaryId, tag)`.
- Blank/missing `tag` → **400** (validation).
- Dictionary owned by someone else → **403**. Dictionary does not exist → **404**.

---

### 3. Delete a tag

```
DELETE /dictionaries/{dictionaryId}/tags/{tag}
```

`{tag}` is the tag text in the path (normalised server-side, so casing/whitespace don't matter).
URL-encode it if it contains spaces/special characters.

**Response `200 OK`** — envelope shape:

```json
{
  "status": 200,
  "message": "Deleted successfully tag food",
  "path": "/dictionaries/d-123/tags/food",
  "timestamp": "2026-07-24T11:16:00.456+02:00"
}
```

Notes:
- **Silent no-op** if the tag isn't there — still `200`.
- Dictionary owned by someone else → **403**. Dictionary does not exist → **404**.

---

## Error envelope (`ErrorResponse`)

All 4xx/5xx errors share this shape:

```json
{
  "status": 403,
  "error": "ForbiddenOperationException",
  "errorDetail": "You are not the owner of this record",
  "path": "/dictionaries/d-123/tags",
  "timestamp": "2026-07-24T11:15:30.123+02:00"
}
```

| Status | When                                                              |
|--------|-------------------------------------------------------------------|
| 400    | Blank `tag`, malformed JSON body.                                 |
| 401    | Missing/invalid/expired token.                                    |
| 403    | Write on a dictionary you don't own.                              |
| 404    | Dictionary not found, or a read on a dictionary you don't own.    |
| 500    | Unexpected server error.                                          |

> **Two timestamps, two formats.** Success `Response` / `ErrorResponse` `timestamp` uses the
> server's **local offset** (`+02:00`). The tag's own `createdAt` is **ISO-8601 UTC** (`...Z`).
> Don't assume they match.
