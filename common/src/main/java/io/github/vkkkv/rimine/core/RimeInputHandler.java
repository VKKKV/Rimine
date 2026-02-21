package io.github.vkkkv.rimine.core;

import io.github.vkkkv.rimine.jni.RimeLib;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

public class RimeInputHandler {
    private static long sessionId = 0;
    private static boolean initialized = false;
    
    // Strong reference to prevent GC of the JNA Structure while native side is active.
    private static final RimeLib.RimeContext context = new RimeLib.RimeContext();

    public record RimeData(String composition, List<String> candidates, int highlightedIndex) {}

    public static synchronized void init(Path sharedDataDir, Path userDataDir) {
        if (initialized) return;

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
            case 259 -> 0xFF08; // BACKSPACE
            case 257 -> 0xFF0D; // ENTER
            case 258 -> 0xFF09; // TAB
            case 256 -> 0xFF1B; // ESCAPE
            case 262 -> 0xFF53; // RIGHT
            case 263 -> 0xFF51; // LEFT
            case 264 -> 0xFF54; // DOWN
            case 265 -> 0xFF52; // UP
            case 266 -> 0xFF55; // PAGE_UP
            case 267 -> 0xFF56; // PAGE_DOWN
            case 268 -> 0xFF50; // HOME
            case 269 -> 0xFF57; // END
            case 261 -> 0xFFFF; // DELETE
            case 32  -> 0x0020; // SPACE
            default -> {
                // Map A-Z (65-90) to lowercase a-z (97-122) for standard RIME input
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

    public static RimeData getCurrentData() {
        if (!initialized || sessionId == 0) return null;

        // RIME's context is already updated in handleKeyPress
        if (context.composition == null && context.menu.num_candidates == 0) return null;

        List<String> candidates = new ArrayList<>();
        if (context.menu.num_candidates > 0 && context.menu.candidates != null) {
            // Pointer arithmetic for candidate array
            int structSize = new RimeLib.RimeCandidate(context.menu.candidates).size();
            for (int i = 0; i < context.menu.num_candidates; i++) {
                Pointer p = context.menu.candidates.share((long) i * structSize);
                candidates.add(new RimeLib.RimeCandidate(p).text);
            }
        }

        return new RimeData(context.composition, candidates, context.menu.highlighted_candidate_index);
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
