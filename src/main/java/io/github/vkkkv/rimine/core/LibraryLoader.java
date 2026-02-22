package io.github.vkkkv.rimine.core;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LibraryLoader {
  public static <T extends Library> T load(String name, Class<T> interfaceClass) {
    // 1. Try system path
    try {
      return Native.load(name, interfaceClass);
    } catch (UnsatisfiedLinkError e) {
      System.err.println(
          "[Rimine] Native library " + name + " not found in system path, attempting fallback...");
    }

    // 2. Fallback to bundled resource
    String libName = System.mapLibraryName(name);
    String resourcePath = "/nativelibs/" + getPlatformFolder() + "/" + libName;

    try (InputStream is = LibraryLoader.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new RuntimeException("Bundled library not found in resources: " + resourcePath);
      }

      // TODO: Add custom temp dir
      Path tempDir = Files.createTempDirectory("rimine_natives");
      Path tempLib = tempDir.resolve(libName);
      Files.copy(is, tempLib, StandardCopyOption.REPLACE_EXISTING);

      // Register for cleanup on exit
      tempLib.toFile().deleteOnExit();
      tempDir.toFile().deleteOnExit();

      return Native.load(tempLib.toAbsolutePath().toString(), interfaceClass);
    } catch (IOException e) {
      throw new RuntimeException("Failed to extract bundled library: " + resourcePath, e);
    }
  }

  // TODO: Add more platforms
  private static String getPlatformFolder() {
    if (Platform.isWindows()) return "windows-x86-64";
    if (Platform.isLinux()) return "linux-x86-64";
    if (Platform.isMac()) return "darwin-x86-64";
    return "unknown";
  }
}
