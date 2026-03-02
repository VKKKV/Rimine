# Rimine

A native **RIME (Rime Input Method Engine)** integration for Minecraft 1.20.1 with **Forge**.

## Features

- **Native Performance**: Uses JNA (Java Native Access) to bridge directly with `librime`, ensuring low latency and high efficiency.
- **Cursor-Relative UI**: The candidate list renders dynamically at the position of your chat cursor for a natural typing experience.
- **Advanced Key Mapping**: Exhaustive translation of GLFW keycodes to RIME-compatible keysyms, including symbols, Numpad, and function keys.
- **Memory Safe**: Implements robust native memory management to prevent leaks and ensure stable sessions.
- **Minimalist Design**: Zero-bloat implementation using pure vanilla Minecraft rendering methods.

## Requirements

- **Minecraft**: 1.20.1
- **Java**: 17
- **System Library**: `librime` must be installed on your host system and available in the system library path.
- **Configuration**: RIME shared and user data must be placed in `config/rimine/shared` and `config/rimine/user`.

## Building

To build the mod, run:

```bash
./gradlew build
```

The resulting JAR will be located in:
- `build/libs/rimine-<version>.jar`

## Running

To run the mod in the Minecraft client:

```bash
./gradlew runClient
```

## Input Mode and Keymap

- **Default mode on startup**: English (ASCII).
- **Toggle Chinese/English in chat**: `Ctrl+Shift+Space`
- **Cycle input schema in chat**: `Ctrl+Shift+\``
- **Candidate navigation (Chinese mode)**:
  - Arrow keys: move highlight
  - Number keys `1..9`: select candidate
  - `Space`/`Enter`: commit current selection

### Chat Commands

- ` /rimine mode` - show current mode
- ` /rimine mode toggle` - toggle Chinese/English
- ` /rimine mode zh` - switch to Chinese mode
- ` /rimine mode en` - switch to English mode
- ` /rimine schema` - show current schema
- ` /rimine schema next` - cycle schema
- ` /rimine schema <id>` - switch schema directly (for example `luna_pinyin_simp`)

## Development

Rimine uses **Forge** for Minecraft mod development.

- **Package**: `io.github.vkkkv.rimine`
- **Native Interface**: `io.github.vkkkv.rimine.jni.RimeLib`
- **Input Logic**: `io.github.vkkkv.rimine.core.RimeInputHandler`

## License

This project is licensed under the MIT License.
