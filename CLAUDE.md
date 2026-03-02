# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rimine is a Minecraft 1.20.1 Forge mod that integrates **RIME (Rime Input Method Engine)** for native Chinese input. It uses JNA to bridge directly with the system's `librime` library, providing low-latency input handling with a cursor-relative candidate UI.

## Build & Development Commands

- **`./gradlew build`** - Compile and package the mod JAR to `build/libs/rimine-<version>.jar`
- **`./gradlew runClient`** - Launch Minecraft 1.20.1 with the mod loaded from local source
- **`./gradlew runServer`** - Launch Forge dedicated server with the mod
- **`./gradlew clean build`** - Full rebuild (use when debugging packaging or dependency issues)
- **`./run_rime_test.sh`** - Run standalone RIME integration tests (requires system `librime`)

## Project Structure & Architecture

### Directory Layout
- `src/main/java/io/github/vkkkv/rimine/` - Main source code
  - **`forge/`** - Forge mod integration and command registration
  - **`jni/`** - JNA bindings to native RIME library (RimeLib, RimeApi, RimeContext, etc.)
  - **`core/`** - Input handling logic, configuration, and library loading
  - **`mixin/`** - Spongepowered Mixin hooks into Minecraft's EditBox rendering
  - **`test/`** - Standalone RIME integration tests
- `src/main/resources/` - Mod metadata, mixin config, pack metadata
- `build/` - Build output artifacts
- `run/` - Gradle's run directory for test instances
- `gradle/` - Gradle wrapper

### Architecture Overview

1. **Entry Point & Forge Integration** (`RimineForge.java`)
   - `@Mod("rimine")` annotation registers the mod with Forge
   - Registers client commands (`/rimine mode`, `/rimine schema`, `/rimine reload`)
   - Subscribes to key Forge events: `RenderGuiOverlayEvent`, `ScreenEvent.KeyboardEvent`, `ScreenEvent.CharacterEvent`
   - Handles input redirection to RIME and candidate rendering

2. **JNI/Native Binding** (`jni/` package)
   - **`RimeLib.java`** - JNA interface that loads the native `librime` library
   - **`RimeApi.java`** - Structure containing all RIME function pointers (setup, initialize, process_key, etc.)
   - **`RimeContext.java`, `RimeStatus.java`, `RimeCommit.java`, `RimeCandidate.java`** - RIME data structures mapped to JNA types
   - **`RimeConfigIterator.java`, `RimeCandidateListIterator.java`** - Iteration helpers for RIME data
   - All structs inherit from `com.sun.jna.Structure` for C interop

3. **Core Logic** (`core/` package)
   - **`RimeInputHandler.java`** - Main state machine for input processing
     - Singleton-style static methods: `init()`, `processKey()`, `toggleAsciiMode()`, `selectSchema()`, etc.
     - Maintains RIME session and cursor position for candidate rendering
     - `RimeData` record holds composition, candidates, highlighting, and cursor position for rendering
   - **`RimineConfig.java`** - Configuration loading from `config/rimine.json`
   - **`LibraryLoader.java`** - JNA library loading with fallback and error handling

4. **Rendering Integration** (`mixin/` package)
   - **`EditBoxMixin.java`** - Uses Spongepowered Mixin to inject into Minecraft's `EditBox.renderWidget()`
   - Hooks capture cursor position and pass it to `RimeInputHandler.setCursorPosition()`
   - Candidate UI is rendered relative to the cursor in `RimineForge` via `RenderGuiOverlayEvent`

5. **Testing** (`test/` package)
   - **`RimeTest.java`** - Basic initialization and key processing test
   - **`RimeTestWithSchema.java`** - Tests schema switching and candidate selection
   - Run via `./run_rime_test.sh`; requires system RIME library installed

## Key Implementation Details

### JNA Library Loading
- `LibraryLoader` attempts to load `librime` from system library paths
- On failure, provides detailed error logging to guide users to install the library

### Input Mode & Schema Management
- Default mode: English (ASCII)
- Toggle Chinese/English: `Ctrl+Shift+Space`
- Cycle input schema: `Ctrl+Shift+\``
- Schema cycling order: `luna_pinyin_simp` → `luna_pinyin` → `terra_pinyin`
- Number keys `1-9` select candidates; `Space`/`Enter` commits

### Key Mapping
- GLFW keycodes are translated to X11 keysyms in `RimeInputHandler.translateKey()`
- Includes extensive mappings for symbols, Numpad, and function keys
- Arrow keys navigate candidates; arrow keys alone do not compose text

### Cursor Position Tracking
- `EditBoxMixin` captures the cursor X offset (text before cursor width) for horizontal tracking
- The EditBox's own `getY()` is passed as the vertical anchor — this is the actual screen Y of the input bar, not the text cursor mid-line
- Candidate panel is positioned via `ScreenEvent.Render.Post` (fires after `ChatScreen` finishes drawing), so it always renders on top of chat history and the input bar

### Session Lifecycle
- `RimeInputHandler.init()` - Called once during mod load
- `RimeInputHandler.processKey()` - Called for every key event
- `RimeInputHandler.cleanup()` - Registered as shutdown hook to free RIME resources

## Dependencies & Requirements

### System Requirements
- **Minecraft**: 1.20.1
- **Java**: 17 (configured in `build.gradle` and `gradle.properties`)
- **librime**: Must be installed and available in system library path
- **RIME Data**: Place shared and user data in `config/rimine/shared` and `config/rimine/user`

### Build Dependencies
- Minecraft Forge: `47.1.0` (hardcoded in `gradle.properties`)
- JNA: `5.14.0` - Java Native Access for C interop
- Spongepowered Mixin: `0.8.5` - Bytecode manipulation for hooks
- Gradle plugins: `net.minecraftforge.gradle`, `com.github.johnrengelman.shadow`, `org.spongepowered.mixin`

## Testing & Validation

Before submitting changes, especially for JNI or key handling:
1. **Compile**: `./gradlew build` - Ensures no Java compilation errors
2. **Integration Test**: `./run_rime_test.sh` - Tests RIME library interaction
3. **Functional Test**: `./gradlew runClient` - Launch Minecraft and test input flow manually
4. **Review Changes**: Check console output and capture screenshots if UI changes

## Common Development Tasks

### Adding a New Chat Command
1. Open `RimineForge.java`
2. Add new `LiteralArgumentBuilder` chain in `registerCommands()` method
3. Use `RimeInputHandler` static methods to query or modify RIME state
4. Send feedback via `context.getSource().sendSystemMessage()`

### Modifying Key Handling
1. Update `RimeInputHandler.translateKey()` for GLFW-to-X11 mapping changes
2. Update `RimeInputHandler.processKey()` for new keybinding logic
3. Test via `./run_rime_test.sh` with various schemas
4. Manual testing in `/gradlew runClient`

### Changing Candidate Rendering
1. The render handler is `onRenderGui(ScreenEvent.Render.Post)` in `RimineForge.java` — it must use this event (not `RenderGuiOverlayEvent`) to draw above the chat screen
2. Panel position is read live from the focused `EditBox` in `mc.screen` — do not use the stored `RimeData.x()/y()` for positioning
3. Panel bottom is anchored at `editBox.getY() - editBox.getHeight() - 2` (one input bar height above the input bar top edge)
4. Use `GuiGraphics` for all drawing (no external UI libraries)

### Adding RIME Configuration Options
1. Add field to `RimineConfig.java`
2. Update `rimine.json` template in `config/rimine/` (user documentation)
3. Load/apply in `RimeInputHandler.init()` or relevant methods

## Coding Standards

- **Language**: Java 17 with UTF-8 encoding
- **Indentation**: 2 spaces, K&R braces
- **Naming**: `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields, `UPPER_SNAKE_CASE` for constants
- **Comments**: Only where logic isn't self-evident; avoid over-documentation
- **Package Boundaries**: `core` for behavior, `jni` for native contracts, `forge` for mod hooks, `mixin` for bytecode injection
- **Native Code**: Keep JNA struct definitions aligned with C types; validate struct layouts in tests

## Git & Commit Guidelines

- **Commit Style**: Short, imperative summaries (e.g., `Feat: add IME mode controls`, `Fix: stabilize RIME input flow`)
- **Scope**: Keep commits focused (one behavior change where possible)
- **PR Requirements**:
  - Describe what changed and why
  - Link related issues
  - Include test evidence (`./gradlew build` output, RIME test results, screenshots for UI changes)

## Security & Resource Management

- **System Library Dependency**: `librime` is a system dependency; do not commit machine-specific paths
- **RIME Data**: Stored in local config paths (`config/rimine/shared`, `config/rimine/user`), not in repository
- **Memory Management**: RIME session lifecycle is strictly managed in `RimeInputHandler` to prevent leaks
- **Session ID**: Single long-lived session maintained in static field; cleanup registered as shutdown hook

## Troubleshooting

- **Build Fails**: Check Java 17 installation and Gradle daemon status (`./gradlew --stop`)
- **`librime` Not Found**: Install system RIME library (e.g., `librime-dev` on Linux)
- **Test Segmentation Fault**: Verify RIME data directories exist and contain valid configuration
- **Input Not Working**: Check RIME mode (English vs Chinese) with `/rimine mode` command; verify composition state in logs
