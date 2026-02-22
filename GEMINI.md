# Rimine Project Overview

Rimine is a Minecraft mod for version 1.20.1 that integrates the **RIME (Rime Input Method Engine)** directly into the game for Forge.

## Architecture

- **`io.github.vkkkv.rimine.jni`**: JNA interface mapping `librime` C API including Menu and Candidate structures.
- **`io.github.vkkkv.rimine.core`**:
  - **`RimeInputHandler`**: 
    - **Advanced Key Mapping**: Exhaustive translation of GLFW keycodes to X11 keysyms (Symbols, F-keys, Numpad).
    - **Cursor Tracking**: Maintains screen coordinates for the active `EditBox` cursor.
    - **Memory Safety**: Strict lifecycle management using `RimeFreeContext` and JVM shutdown hooks.
  - **`RimineConfig`**: Configuration management for RIME settings
  - **`LibraryLoader`**: Handles loading native librime library
- **`io.github.vkkkv.rimine.mixin`**: `EditBoxMixin` intercepts `renderWidget` to calculate and update cursor screen coordinates.
- **`io.github.vkkkv.rimine.forge`**:
  - **UI**: Renders a vertical candidate box with a dark background and highlighting using Forge's `GuiGraphics`.

## Tech Stack

- **Java 17**: Required for Minecraft 1.20.1.
- **Forge**: Native Minecraft mod development framework.
- **JNA (Java Native Access)**: Bridges Java and the native `librime` C library.
- **Mixins**: Used to extract internal state (cursor position) from vanilla Minecraft classes.

## Building and Running

### Build Commands
- Build jar: `./gradlew build`
- Run Forge client: `./gradlew runClient`

### Requirements
- `librime` must be installed on the host system.
- Shared and user data directories for RIME should be configured in `config/rimine/`.

## Development Conventions

1.  **Package Path**: `io.github.vkkkv.rimine`
2.  **Cursor-Relative UI**: Always use `RimeData.x()` and `RimeData.y()` for rendering to ensure the candidate list follows the typing focus.
3.  **Vanilla-Only Drawing**: Use native `fill()` and `drawText()`/`drawString()` methods. Avoid external UI libraries to keep the mod "minimal and zero-bloat."
4.  **Key Mapping**: Ensure any new keys added to `translateKey` follow the X11 keysym standard as expected by RIME.
