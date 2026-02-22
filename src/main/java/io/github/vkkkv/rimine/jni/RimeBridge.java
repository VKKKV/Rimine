package io.github.vkkkv.rimine.jni;

public class RimeBridge {
  static {
    System.loadLibrary("rimine_jni");
  }

  public static native void initEngine(String sharedDataDir, String userDataDir);
}
