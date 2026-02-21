# Rimine Project Overview

Rimine is a multi-loader Minecraft mod for version 1.20.1 that integrates the **RIME (Rime Input Method Engine)** directly into the game. It uses **Architectury Loom** to manage a shared codebase between **Fabric** and **Forge**.

## Architecture

- **`common`**: (`io.github.vkkkv.rimine.core`, `io.github.vkkkv.rimine.jni`)
  - **`RimeLib`**: JNA interface mapping `librime` C API (Traits, Context, Menu, Candidates).
  - **`RimeInputHandler`**: 
    - Translates GLFW keycodes to X11 keysyms.
    - Manages bitwise modifier translation (Shift, Ctrl, Alt).
    - Handles robust native memory lifecycle (idempotent cleanup, C-side string freeing).
    - Provides `RimeData` record for rendering.
- **`forge`**: (`io.github.vkkkv.rimine.forge`)
  - Hooks into `ScreenEvent.KeyPressed.Pre` for input interception.
  - Renders UI via `RenderGuiOverlayEvent.Post` using `GuiGraphics`.
  - Implements JVM shutdown hooks for native finalization.
- **`fabric`**: (`io.github.vkkkv.rimine.fabric`)
  - Hooks into `ScreenKeyboardEvents.ALLOW_KEY_PRESS` for input interception.
  - Renders UI via `HudRenderCallback` using `DrawContext`.
  - Uses `ClientLifecycleEvents` for startup/shutdown.

## Tech Stack

- **Java 17**: Required for Minecraft 1.20.1.
- **Architectury Loom**: Gradle plugin for multi-loader development.
- **JNA (Java Native Access)**: Bridges Java and the native `librime` C library.
- **Fabric API**: Used for Fabric-specific hooks.
- **Official Mojang Mappings**: Standard deobfuscation mappings.

## Building and Running

### Build Commands
- Build all jars: `./gradlew build`
- Run Fabric client: `./gradlew :fabric:runClient`
- Run Forge client: `./gradlew :forge:runClient`

### Requirements
- `librime` must be installed on the host system.
- Shared and user data directories for RIME should be configured in `config/rimine/`.

## Development Conventions

1.  **Package Path**: `io.github.vkkkv.rimine`
2.  **Memory Safety**: Always call `RimeFreeContext` before fetching a new context or cleaning up to avoid native leaks.
3.  **UI Performance**: Keep the HUD render loop lightweight; avoid object allocation in `onRenderGui` / `HudRenderCallback`.
4.  **Key Mapping**: Expand `translateKey` in `RimeInputHandler` as new keys are needed, following X11 keysym standards.
