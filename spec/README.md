# Android protocol notes and fixtures

This directory contains protocol notes and test data used by the Smart Netease Android app. It is not a runtime module or a separate service.

## Contents

| Path | Purpose |
|---|---|
| `api/` | NetEase endpoint, login, playlist, entitlement and request-encryption notes used while maintaining the Android client. |
| `fixtures/crypto/` | Deterministic test inputs and expected eapi / weapi / xeapi values. |
| `fixtures/entitlement/` | Account and playback entitlement cases used by Android tests. |
| `fixtures/lrc/` | LRC parsing and translation fixtures. |
| `fixtures/quality/` | Quality selection and fallback cases. |
| `fixtures/queue/` | Playback queue behavior fixtures. |
| `tools/` | Local protocol research and fixture-generation utilities. Review each script before running; some experiments may write to a real account. |

When changing an Android protocol implementation, update its notes and deterministic fixtures together, then run the related JVM tests. Record uncertainty explicitly; a captured request or mock test does not prove a real account write persisted.

Fixture JSON records use a stable `id`, a short `description`, the evidence `source`, and the case-specific `input` and `expect` fields. Tests should read fixtures from this directory rather than maintaining duplicate copies.
