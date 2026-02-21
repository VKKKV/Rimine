# Rimine

A native **RIME (Rime Input Method Engine)** integration for Minecraft 1.20.1, supporting both **Fabric** and **Forge** via Architectury.

## Features

- **Multi-Loader Support**: Seamless operation on both Fabric and Forge platforms.
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

To build the mod for both platforms, run:

```bash
./gradlew build
```

The resulting JARs will be located in:
- `fabric/build/libs/rimine-fabric-<version>.jar`
- `forge/build/libs/rimine-forge-<version>.jar`

## Development

Rimine uses **Architectury Loom**. Most of the core logic resides in the `common` module to ensure consistency across loaders.

- **Package**: `io.github.vkkkv.rimine`
- **Native Interface**: `io.github.vkkkv.rimine.jni.RimeLib`
- **Input Logic**: `io.github.vkkkv.rimine.core.RimeInputHandler`

## License

This project is licensed under the MIT License.
