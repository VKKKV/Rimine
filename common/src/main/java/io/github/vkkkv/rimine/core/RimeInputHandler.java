package io.github.vkkkv.rimine.core;

import io.github.vkkkv.rimine.jni.RimeLib;
import com.sun.jna.Pointer;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

public class RimeInputHandler {
    private static long sessionId = 0;
    private static boolean initialized = false;
    
    // Strong reference to prevent GC of the JNA Structure while native side is active.
    private static final RimeLib.RimeContext context = new RimeLib.RimeContext();

    private static int cursorX = 0;
    private static int cursorY = 0;

    public record RimeData(String composition, List<String> candidates, int highlightedIndex, int x, int y, int pageNo, boolean isLastPage, boolean isSwitcher) {}

    public static synchronized void init(Path sharedDataDir, Path userDataDir) {
        if (initialized) return;

        // Load config from game-relative directory
        Path configPath = userDataDir.getParent().resolve("rimine.json");
        RimineConfig.load(configPath);
        
        RimeLib.RimeTraits traits = new RimeLib.RimeTraits();
        traits.shared_data_dir = sharedDataDir.toAbsolutePath().toString();
        traits.user_data_dir = userDataDir.toAbsolutePath().toString();
        traits.app_name = "rimine";

        RimeLib.INSTANCE.RimeInitialize(traits);
        sessionId = RimeLib.INSTANCE.RimeCreateSession();
        initialized = true;
    }

    /**
     * Handles a key press and ensures context is updated.
     * @param glfwKeyCode The GLFW keycode.
     * @param modifiers The GLFW modifier flags.
     * @return true if the key was absorbed by Rime.
     */
    public static boolean handleKeyPress(int glfwKeyCode, int modifiers) {
        if (!initialized || sessionId == 0) return false;

        int rimeKey = translateKey(glfwKeyCode);
        int rimeMods = translateModifiers(modifiers);

        boolean handled = RimeLib.INSTANCE.RimeProcessKey(sessionId, rimeKey, rimeMods);
        
        if (handled) {
            RimeLib.INSTANCE.RimeFreeContext(context);
            RimeLib.INSTANCE.RimeGetContext(sessionId, context);
        }
        
        return handled;
    }

    private static int translateKey(int glfwKey) {
        // Mapping GLFW keycodes to X11 keysyms used by RIME
        return switch (glfwKey) {
            // Navigation & Control
            case 256 -> 0xFF1B; // ESCAPE
            case 257 -> 0xFF0D; // ENTER
            case 258 -> 0xFF09; // TAB
            case 259 -> 0xFF08; // BACKSPACE
            case 260 -> 0xFF63; // INSERT
            case 261 -> 0xFFFF; // DELETE
            case 262 -> 0xFF53; // RIGHT
            case 263 -> 0xFF51; // LEFT
            case 264 -> 0xFF54; // DOWN
            case 265 -> 0xFF52; // UP
            case 266 -> 0xFF55; // PAGE_UP
            case 267 -> 0xFF56; // PAGE_DOWN
            case 268 -> 0xFF50; // HOME
            case 269 -> 0xFF57; // END
            case 32  -> 0x0020; // SPACE

            // Function Keys
            case 290 -> 0xFFBE; // F1
            case 291 -> 0xFFBF; // F2
            case 292 -> 0xFFC0; // F3
            case 293 -> 0xFFC1; // F4
            case 294 -> 0xFFC2; // F5
            case 295 -> 0xFFC3; // F6
            case 296 -> 0xFFC4; // F7
            case 297 -> 0xFFC5; // F8
            case 298 -> 0xFFC6; // F9
            case 299 -> 0xFFC7; // F10
            case 300 -> 0xFFC8; // F11
            case 301 -> 0xFFC9; // F12

            // Numpad
            case 320 -> 0xFFB0; // KP_0
            case 321 -> 0xFFB1; // KP_1
            case 322 -> 0xFFB2; // KP_2
            case 323 -> 0xFFB3; // KP_3
            case 324 -> 0xFFB4; // KP_4
            case 325 -> 0xFFB5; // KP_5
            case 326 -> 0xFFB6; // KP_6
            case 327 -> 0xFFB7; // KP_7
            case 328 -> 0xFFB8; // KP_8
            case 329 -> 0xFFB9; // KP_9
            case 330 -> 0xFFAE; // KP_DECIMAL
            case 331 -> 0xFFAF; // KP_DIVIDE
            case 332 -> 0xFFAA; // KP_MULTIPLY
            case 333 -> 0xFFAD; // KP_SUBTRACT
            case 334 -> 0xFFAB; // KP_ADD
            case 335 -> 0xFF8D; // KP_ENTER
            case 336 -> 0xFFBD; // KP_EQUAL

            // Symbols
            case 39  -> 0x0027; // APOSTROPHE
            case 44  -> 0x002c; // COMMA
            case 45  -> 0x002d; // MINUS
            case 46  -> 0x002e; // PERIOD
            case 47  -> 0x002f; // SLASH
            case 59  -> 0x003b; // SEMICOLON
            case 61  -> 0x003d; // EQUAL
            case 91  -> 0x005b; // LEFT_BRACKET
            case 92  -> 0x005c; // BACKSLASH
            case 93  -> 0x005d; // RIGHT_BRACKET
            case 96  -> 0x0060; // GRAVE_ACCENT

            default -> {
                // Map A-Z (65-90) to lowercase a-z (97-122)
                if (glfwKey >= 65 && glfwKey <= 90) yield glfwKey + 32;
                yield glfwKey;
            }
        };
    }

    private static int translateModifiers(int glfwMods) {
        int rimeMods = 0;
        if ((glfwMods & 0x0001) != 0) rimeMods |= (1 << 0); // Shift (kShiftMask)
        if ((glfwMods & 0x0002) != 0) rimeMods |= (1 << 2); // Control (kControlMask)
        if ((glfwMods & 0x0004) != 0) rimeMods |= (1 << 3); // Alt (kMod1Mask)
        return rimeMods;
    }

    public static void setCursorPosition(int x, int y) {
        cursorX = x;
        cursorY = y;
    }

    public static RimeData getCurrentData() {
        if (!initialized || sessionId == 0) return null;

        // RIME's context is already updated in handleKeyPress
        boolean hasComposition = context.composition.preedit != null && !context.composition.preedit.isEmpty();
        boolean hasCandidates = context.menu.num_candidates > 0;

        if (!hasComposition && !hasCandidates) return null;

        List<String> candidates = new ArrayList<>();
        if (hasCandidates && context.menu.candidates != null) {
            int structSize = new RimeLib.RimeCandidate(context.menu.candidates).size();
            for (int i = 0; i < context.menu.num_candidates; i++) {
                Pointer p = context.menu.candidates.share((long) i * structSize);
                candidates.add(new RimeLib.RimeCandidate(p).text);
            }
        }

        // Switcher state: candidates exist but no composition/preedit text
        boolean isSwitcher = hasCandidates && !hasComposition;

        return new RimeData(
            context.composition.preedit, 
            candidates, 
            context.menu.highlighted_candidate_index, 
            cursorX, 
            cursorY,
            context.menu.page_no,
            context.menu.is_last_page,
            isSwitcher
        );
    }

    public static void reload(Path configPath) {
        RimineConfig.load(configPath);
    }

    public static synchronized void cleanup() {
        if (initialized) {
            if (sessionId != 0) {
                RimeLib.INSTANCE.RimeFreeContext(context);
                RimeLib.INSTANCE.RimeDestroySession(sessionId);
                sessionId = 0;
            }
            RimeLib.INSTANCE.RimeFinalize();
            initialized = false;
        }
    }
}
