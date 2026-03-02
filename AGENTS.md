# Repository Guidelines

## Project Structure & Module Organization
- Main source code is under `src/main/java/io/github/vkkkv/rimine`.
- Core runtime logic lives in `core/` (`RimeInputHandler`, config and loader code).
- Native API/JNA bindings live in `jni/` (`RimeApi`, `RimeContext`, related structs).
- Forge integration and UI hooks live in `forge/` and `mixin/`.
- Mod metadata/resources are in `src/main/resources` (`META-INF/mods.toml`, mixin config, `pack.mcmeta`).
- The Gradle run directory is `run/`. Build artifacts are written to `build/`.

## Build, Test, and Development Commands
- `./gradlew build`: compile and package the mod JAR (`build/libs/rimine-<version>.jar`).
- `./gradlew runClient`: launch a Forge client with this mod from the local source set.
- `./gradlew runServer`: launch the Forge dedicated server profile (`--nogui`).
- `./gradlew clean build`: rebuild from scratch when debugging packaging or dependency issues.
- `./run_rime_test.sh`: run standalone RIME integration checks (requires host `librime`).

## Coding Style & Naming Conventions
- Language level is Java 17; use UTF-8 source encoding.
- Follow existing style: 2-space indentation, K&R braces, concise comments only when needed.
- Class names use `UpperCamelCase`; methods/fields use `lowerCamelCase`; constants use `UPPER_SNAKE_CASE`.
- Keep package boundaries clear (`core` for behavior, `jni` for native interop contracts).
- In `RimeInputHandler.translateKey`, keep GLFW-to-X11 keysym mappings aligned with RIME expectations.
- For candidate rendering, use cursor-relative coordinates from `RimeData.x()` / `RimeData.y()`.
- Prefer vanilla drawing APIs (`fill`, `drawString`) over external UI libraries.

## Testing Guidelines
- Current tests are integration-style checks in `src/main/java/io/github/vkkkv/rimine/test` (`RimeTest`, `RimeTestWithSchema`).
- Validate every JNI-related change by running `./gradlew build` and then `./run_rime_test.sh`.
- When changing key handling or schema logic, include before/after console output in the PR.

## Commit & Pull Request Guidelines
- Match the project’s commit style: short, imperative summaries (for example, `Refactor: split RimeApi structs`).
- Keep commits focused (one behavior change per commit where possible).
- PRs should include:
  - What changed and why.
  - Linked issue(s) when applicable.
  - Test evidence (`./gradlew build` output, RIME test result, and screenshots for UI changes).

## Security & Configuration Notes
- `librime` is a system dependency; do not commit machine-specific library paths.
- Keep RIME data in local config paths (`config/rimine/shared`, `config/rimine/user`), not in repository source.
