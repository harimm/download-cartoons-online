# agents.md

## Purpose
This file is a contributor/automation guide for agents working in `download-cartoons-online`.

## Project Snapshot
- **Language:** Java 21
- **Build:** Gradle (Groovy DSL)
- **Runtime framework:** Spring Context/Core (non-Boot)
- **HTTP + parsing:** Apache HttpClient 5, Jsoup
- **Logging:** SLF4J with Log4j backend
- **Tests:** JUnit 5 + Mockito

## Architectural Overview
- Entry point: `CartoonDownloader`
- Configuration root: `BaseConfig`
- Core orchestration: `worker/DownloadManager`
- Extraction pipeline:
  1. `PageWalker` discovers episode pages
  2. `VideoFinder` resolves stream/video URL
  3. `DownloadWorker` downloads the media file
  4. `EpisodeTracker` tracks downloaded/ignored episodes

## Non-Negotiable Standards
1. Keep package structure under `dev.harimohan.app.toondownload`.
2. Do not introduce new frameworks unless explicitly requested.
3. Keep runtime configuration externalized in `application.properties`.
4. Avoid hardcoded machine-specific paths in source code.
5. Preserve existing logging style and tone.
6. Keep changes backward-compatible with existing config keys.
7. Always declare explicit access modifiers on classes, methods, and fields.
8. Unless broader visibility is required by framework/runtime/contracts, default members to `private`.
9. JUnit test methods should be declared `public`.

## Configuration Rules
- Existing key formats must remain valid.
- For any new key, update `README.md` with:
  - default value,
  - expected format,
  - whether it is required.

## Testing Expectations
- Add/adjust tests in `src/test/java` for any behavior changes.
- Prefer deterministic tests with mocks/stub HTTP responses.
- Ensure local test command succeeds:
  - Windows: `gradlew.bat test`
  - Linux/macOS: `./gradlew test`

## Change Safety Checklist
Before finalizing, verify:
- No secrets/tokens/private URLs are committed.
- Existing behavior is not removed unless requested.
- Dependency changes are minimal and justified.
- README examples and commands still match actual code.

## Recommended Workflow for Agents
1. Read relevant files first (`src/main` + related tests).
2. Propose minimal, focused edits.
3. Implement changes with clear reasoning.
4. Update tests/docs when behavior/config changes.
5. Validate with Gradle tests.
