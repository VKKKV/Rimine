package io.github.vkkkv.rimine.jni;

import com.sun.jna.Library;
import io.github.vkkkv.rimine.core.LibraryLoader;

public interface RimeLib extends Library {
  RimeLib INSTANCE = LibraryLoader.load("rime", RimeLib.class);

  // Only exported function - returns the API structure with all function pointers
  RimeApi.ByReference rime_get_api();
}
