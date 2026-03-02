package io.github.vkkkv.rimine.core;

import com.sun.jna.Pointer;
import io.github.vkkkv.rimine.jni.RimeApi;
import io.github.vkkkv.rimine.jni.RimeCandidate;
import io.github.vkkkv.rimine.jni.RimeCommit;
import io.github.vkkkv.rimine.jni.RimeContext;
import io.github.vkkkv.rimine.jni.RimeLib;
import io.github.vkkkv.rimine.jni.RimeStatus;
import io.github.vkkkv.rimine.jni.RimeTraits;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class RimeInputHandler {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final int GLFW_KEY_A = 65;
  private static final int GLFW_KEY_Z = 90;
  private static final int ASCII_LOWERCASE_OFFSET = 32;
  private static final int CANDIDATE_STRUCT_SIZE = new RimeCandidate().size();
  private static final int INPUT_TRACE_LIMIT = 40;
  private static final String[] SCHEMA_CYCLE = {"luna_pinyin_simp", "luna_pinyin", "terra_pinyin"};
  private static final int GLFW_KEY_DOWN = 264;
  private static final int GLFW_KEY_UP = 265;
  private static final int GLFW_KEY_LEFT = 263;
  private static final int GLFW_KEY_RIGHT = 262;
  private static int inputTraceCount = 0;
  private static long sessionId = 0;
  private static boolean initialized = false;
  private static boolean asciiMode = true;

  // Get the RIME API structure with all function pointers
  private static final RimeApi api = RimeLib.INSTANCE.rime_get_api();

  private static int cursorX = 0;
  private static int cursorY = 0;
  private static Snapshot snapshot = null;
  private static String pendingCommitText = "";

  public record RimeData(
      String composition,
      List<String> candidates,
      int highlightedIndex,
      int x,
      int y,
      int pageNo,
      boolean isLastPage,
      boolean isSwitcher) {}

  private record Snapshot(
      String composition,
      List<String> candidates,
      int highlightedIndex,
      int pageNo,
      boolean isLastPage,
      boolean isSwitcher) {}

  public static synchronized void init(Path sharedDataDir, Path userDataDir) {
    if (initialized) return;

    // Load config from game-relative directory
    Path configPath = userDataDir.getParent().resolve("rimine.json");
    RimineConfig.load(configPath);

    Path resolvedSharedDataDir = resolveSharedDataDir(sharedDataDir);
    Path resolvedUserDataDir = resolveUserDataDir(userDataDir);

    try {
      Files.createDirectories(resolvedSharedDataDir);
      Files.createDirectories(resolvedUserDataDir);
    } catch (IOException e) {
      LOGGER.error("[Rimine] Failed to create RIME data directories.", e);
    }

    RimeTraits traits = new RimeTraits();
    traits.shared_data_dir = resolvedSharedDataDir.toAbsolutePath().toString();
    traits.user_data_dir = resolvedUserDataDir.toAbsolutePath().toString();
    traits.app_name = "rimine";

    RimeTraits.ByReference traitsRef = new RimeTraits.ByReference();
    traitsRef.shared_data_dir = traits.shared_data_dir;
    traitsRef.user_data_dir = traits.user_data_dir;
    traitsRef.app_name = traits.app_name;
    traitsRef.data_size = traits.data_size;

    api.setup.invoke(traitsRef);
    api.initialize.invoke(traitsRef);
    sessionId = api.create_session.invoke();
    if (sessionId == 0) {
      throw new IllegalStateException("Failed to create RIME session");
    }
    LOGGER.info(
        "[Rimine] Initialized with shared_data_dir={} user_data_dir={} sessionId={}",
        traits.shared_data_dir,
        traits.user_data_dir,
        sessionId);
    ensureSchemaReady(traitsRef);
    logSchemaInfo();
    initialized = true;
  }

  /**
   * Handles a key press and ensures context is updated.
   *
   * @param glfwKeyCode The GLFW keycode.
   * @param modifiers The GLFW modifier flags.
   * @return true if the key was absorbed by Rime.
   */
  public static boolean handleKeyPress(int glfwKeyCode, int modifiers) {
    if (!initialized || sessionId == 0) return false;
    if (asciiMode) return false;

    if (handleCandidateNavigation(glfwKeyCode)) {
      traceInput("key", glfwKeyCode, modifiers, true);
      return true;
    }

    int rimeKey = translateKey(glfwKeyCode);
    int rimeMods = translateModifiers(modifiers);

    boolean handled;
    try {
      api.set_option.invoke(sessionId, "ascii_mode", asciiMode);
      handled = api.process_key.invoke(sessionId, rimeKey, rimeMods);
      refreshSnapshot();
      collectCommitText();
      traceInput("key", glfwKeyCode, modifiers, handled);
    } catch (RuntimeException e) {
      LOGGER.error("[Rimine] Failed while processing key input.", e);
      return false;
    }

    return handled;
  }

  private static boolean handleCandidateNavigation(int glfwKeyCode) {
    if (snapshot == null || snapshot.candidates().isEmpty()) return false;
    if (glfwKeyCode == GLFW_KEY_LEFT || glfwKeyCode == GLFW_KEY_RIGHT) {
      boolean backward = glfwKeyCode == GLFW_KEY_LEFT;
      boolean ok = api.change_page.invoke(sessionId, backward);
      if (!ok) return false;
      refreshSnapshot();
      return true;
    }
    if (glfwKeyCode != GLFW_KEY_DOWN && glfwKeyCode != GLFW_KEY_UP) return false;

    int count = snapshot.candidates().size();
    int current = Math.max(0, Math.min(snapshot.highlightedIndex(), count - 1));
    int next =
        glfwKeyCode == GLFW_KEY_DOWN
            ? (current + 1) % count
            : (current - 1 + count) % count;

    boolean ok = api.highlight_candidate_on_current_page.invoke(sessionId, next);
    if (!ok) return false;
    refreshSnapshot();
    return true;
  }

  public static boolean handleCharTyped(int codePoint, int modifiers) {
    if (!initialized || sessionId == 0 || codePoint <= 0) return false;

    boolean handled;
    try {
      api.set_option.invoke(sessionId, "ascii_mode", asciiMode);
      handled = api.process_key.invoke(sessionId, codePoint, translateModifiers(modifiers));
      refreshSnapshot();
      collectCommitText();
      traceInput("char", codePoint, modifiers, handled);
    } catch (RuntimeException e) {
      LOGGER.error("[Rimine] Failed while processing typed character.", e);
      return false;
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
      case 32 -> 0x0020; // SPACE

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
      case 39 -> 0x0027; // APOSTROPHE
      case 44 -> 0x002c; // COMMA
      case 45 -> 0x002d; // MINUS
      case 46 -> 0x002e; // PERIOD
      case 47 -> 0x002f; // SLASH
      case 59 -> 0x003b; // SEMICOLON
      case 61 -> 0x003d; // EQUAL
      case 91 -> 0x005b; // LEFT_BRACKET
      case 92 -> 0x005c; // BACKSLASH
      case 93 -> 0x005d; // RIGHT_BRACKET
      case 96 -> 0x0060; // GRAVE_ACCENT

      default -> {
        // Map A-Z (65-90) to lowercase a-z (97-122)
        if (glfwKey >= GLFW_KEY_A && glfwKey <= GLFW_KEY_Z) yield glfwKey + ASCII_LOWERCASE_OFFSET;
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
    if (!initialized || sessionId == 0 || snapshot == null) return null;
    return new RimeData(
        snapshot.composition(),
        snapshot.candidates(),
        snapshot.highlightedIndex(),
        cursorX,
        cursorY,
        snapshot.pageNo(),
        snapshot.isLastPage(),
        snapshot.isSwitcher());
  }

  public static void reload(Path configPath) {
    RimineConfig.load(configPath);
  }

  public static synchronized void cleanup() {
    if (initialized) {
      if (sessionId != 0) {
        api.destroy_session.invoke(sessionId);
        sessionId = 0;
      }
      api.finalize.invoke();
      initialized = false;
      snapshot = null;
      pendingCommitText = "";
    }
  }

  public static String consumeCommitText() {
    String commit = pendingCommitText;
    pendingCommitText = "";
    return commit;
  }

  public static boolean shouldBlockCharTyped(int codePoint) {
    if (!initialized || sessionId == 0 || asciiMode) return false;
    return codePoint >= 32 && codePoint <= 126;
  }

  public static synchronized boolean setAsciiMode(boolean enabled) {
    if (!initialized || sessionId == 0) return false;
    try {
      api.set_option.invoke(sessionId, "ascii_mode", enabled);
      asciiMode = enabled;
      return true;
    } catch (RuntimeException e) {
      LOGGER.warn("[Rimine] Failed to set ascii_mode={}", enabled, e);
      return false;
    }
  }

  public static synchronized boolean toggleAsciiMode() {
    return setAsciiMode(!asciiMode);
  }

  public static synchronized boolean isAsciiMode() {
    return asciiMode;
  }

  public static synchronized boolean setSchema(String schemaId) {
    if (!initialized || sessionId == 0 || schemaId == null || schemaId.isBlank()) return false;
    boolean selected = api.select_schema.invoke(sessionId, schemaId);
    if (selected) {
      LOGGER.info("[Rimine] Activated schema: {}", schemaId);
      return true;
    }
    return false;
  }

  public static synchronized String cycleSchema() {
    if (!initialized || sessionId == 0) return null;
    String current = getActiveSchemaId();
    int startIdx = 0;
    for (int i = 0; i < SCHEMA_CYCLE.length; i++) {
      if (SCHEMA_CYCLE[i].equals(current)) {
        startIdx = i + 1;
        break;
      }
    }
    for (int i = 0; i < SCHEMA_CYCLE.length; i++) {
      String candidate = SCHEMA_CYCLE[(startIdx + i) % SCHEMA_CYCLE.length];
      if (setSchema(candidate)) return candidate;
    }
    return null;
  }

  public static synchronized String getCurrentSchemaId() {
    if (!initialized || sessionId == 0) return null;
    return getActiveSchemaId();
  }

  private static void logSchemaInfo() {
    RimeStatus status = new RimeStatus();
    if (api.get_status.invoke(sessionId, status)) {
      String schemaId = status.schema_id != null ? status.schema_id : "N/A";
      String schemaName = status.schema_name != null ? status.schema_name : "N/A";
      System.out.println("[Rimine] Active schema: " + schemaId + " (" + schemaName + ")");
      LOGGER.info(
          "[Rimine] Active schema: {} ({}) ascii_mode={} composing={}",
          schemaId,
          schemaName,
          status.is_ascii_mode,
          status.is_composing);
      if (".default".equals(schemaId)) {
        boolean switched = api.select_schema.invoke(sessionId, "luna_pinyin");
        if (switched) {
          LOGGER.info("[Rimine] Auto-selected schema: luna_pinyin");
        } else {
          LOGGER.warn(
              "[Rimine] Active schema is .default and auto-select of luna_pinyin failed. "
                  + "Install/deploy RIME schemas in shared/user data.");
        }
      }
      api.set_option.invoke(sessionId, "ascii_mode", asciiMode);
      LOGGER.info("[Rimine] Set option ascii_mode={}", asciiMode);
      // Prevent JNA from writing Java String fields back into native-owned memory on free.
      status.setAutoWrite(false);
      api.free_status.invoke(status);
    } else {
      LOGGER.warn("[Rimine] Could not query active schema after initialization.");
    }
  }

  private static void ensureSchemaReady(RimeTraits.ByReference traitsRef) {
    boolean initiallySelected = trySelectPreferredSchema();
    if (!initiallySelected) {
      LOGGER.warn("[Rimine] Could not activate preferred schema before deploy.");
    }

    // Always run one deploy/prebuild pass for the chosen user_data_dir to ensure
    // dictionaries and schema artifacts are usable at runtime.
    try {
      api.deployer_initialize.invoke(traitsRef);
      boolean prebuildOk = api.prebuild.invoke();
      boolean deployOk = api.deploy.invoke();
      LOGGER.info("[Rimine] prebuild={} deploy={}", prebuildOk, deployOk);
    } catch (RuntimeException e) {
      LOGGER.warn("[Rimine] Deploy/prebuild failed.", e);
    }

    if (!trySelectPreferredSchema()) {
      LOGGER.warn("[Rimine] Preferred schemas still unavailable after deploy attempt.");
    }
  }

  private static boolean trySelectPreferredSchema() {
    String[] preferredSchemas = {"luna_pinyin_simp", "luna_pinyin", "terra_pinyin"};
    for (String schemaId : preferredSchemas) {
      boolean selected = api.select_schema.invoke(sessionId, schemaId);
      if (!selected) continue;
      String active = getActiveSchemaId();
      if (schemaId.equals(active)) {
        LOGGER.info("[Rimine] Activated schema: {}", schemaId);
        return true;
      }
    }
    return false;
  }

  private static String getActiveSchemaId() {
    RimeStatus status = new RimeStatus();
    try {
      if (!api.get_status.invoke(sessionId, status)) return null;
      return status.schema_id;
    } finally {
      status.setAutoWrite(false);
      api.free_status.invoke(status);
    }
  }

  private static Path resolveSharedDataDir(Path configuredSharedDataDir) {
    if (hasCompiledSchemaData(configuredSharedDataDir)) {
      return configuredSharedDataDir;
    }

    Path systemSharedDataDir = Path.of("/usr/share/rime-data");
    if (hasCompiledSchemaData(systemSharedDataDir)) {
      LOGGER.info(
          "[Rimine] Using system shared data directory: {}",
          systemSharedDataDir.toAbsolutePath());
      return systemSharedDataDir;
    }

    LOGGER.warn(
        "[Rimine] Shared data directory has no compiled schema data (missing build/*.bin): "
            + configuredSharedDataDir.toAbsolutePath());
    return configuredSharedDataDir;
  }

  private static boolean isDirectoryUsable(Path dir) {
    if (!Files.isDirectory(dir)) return false;
    try (var stream = Files.list(dir)) {
      return stream.findAny().isPresent();
    } catch (IOException e) {
      return false;
    }
  }

  private static boolean hasCompiledSchemaData(Path dir) {
    Path buildDir = dir.resolve("build");
    if (!Files.isDirectory(buildDir)) return false;
    try (var stream = Files.list(buildDir)) {
      return stream.anyMatch(path -> path.getFileName().toString().endsWith(".bin"));
    } catch (IOException e) {
      return false;
    }
  }

  private static Path resolveUserDataDir(Path configuredUserDataDir) {
    if (isDirectoryUsable(configuredUserDataDir)) {
      return configuredUserDataDir;
    }

    Path systemUserDataDir =
        Path.of(System.getProperty("user.home"), ".local", "share", "rime");
    if (isDirectoryUsable(systemUserDataDir)) {
      LOGGER.info("[Rimine] Using system user data directory: {}", systemUserDataDir.toAbsolutePath());
      return systemUserDataDir;
    }

    return configuredUserDataDir;
  }

  private static void traceInput(String type, int code, int modifiers, boolean handled) {
    if (inputTraceCount >= INPUT_TRACE_LIMIT) return;
    inputTraceCount++;

    String preedit = snapshot != null && snapshot.composition() != null ? snapshot.composition() : "";
    int candidateCount = snapshot != null ? snapshot.candidates().size() : 0;
    LOGGER.info(
        "[Rimine] input {} code={} mods={} handled={} preedit='{}' candidates={}",
        type,
        code,
        modifiers,
        handled,
        preedit,
        candidateCount);
  }

  private static void refreshSnapshot() {
    RimeContext context = new RimeContext();
    if (!api.get_context.invoke(sessionId, context)) {
      snapshot = null;
      return;
    }

    try {
      // Prevent JNA from writing Java String fields back into native-owned memory on free.
      context.setAutoWrite(false);
      boolean hasComposition =
          context.composition != null
              && context.composition.preedit != null
              && !context.composition.preedit.isEmpty();
      boolean hasCandidates = context.menu != null && context.menu.num_candidates > 0;
      if (!hasComposition && !hasCandidates) {
        snapshot = null;
        return;
      }

      List<String> candidates = new ArrayList<>();
      if (hasCandidates && context.menu.candidates != null) {
        for (int i = 0; i < context.menu.num_candidates; i++) {
          Pointer p = context.menu.candidates.share((long) i * CANDIDATE_STRUCT_SIZE);
          candidates.add(new RimeCandidate(p).text);
        }
      }

      snapshot =
          new Snapshot(
              context.composition.preedit,
              candidates,
              context.menu.highlighted_candidate_index,
              context.menu.page_no,
              context.menu.is_last_page != 0,
              hasCandidates && !hasComposition);
    } finally {
      api.free_context.invoke(context);
    }
  }

  private static void collectCommitText() {
    RimeCommit commit = new RimeCommit();
    if (!api.get_commit.invoke(sessionId, commit)) {
      return;
    }
    try {
      if (commit.text != null && !commit.text.isEmpty()) {
        pendingCommitText = pendingCommitText + commit.text;
      }
    } finally {
      // Prevent JNA from writing Java String fields back into native-owned memory on free.
      commit.setAutoWrite(false);
      api.free_commit.invoke(commit);
    }
  }
}
