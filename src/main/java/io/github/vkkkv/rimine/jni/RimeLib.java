package io.github.vkkkv.rimine.jni;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface RimeLib extends Library {
  RimeLib INSTANCE = Native.load("rime", RimeLib.class);

  RimeApi.ByReference rime_get_api();
}
