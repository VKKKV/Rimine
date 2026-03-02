package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.List;

public class RimeTraits extends Structure {
  public static class ByReference extends RimeTraits implements Structure.ByReference {}

  public int data_size;
  public String shared_data_dir;
  public String user_data_dir;
  public String distribution_name;
  public String distribution_code_name;
  public String distribution_version;
  public String app_name;
  public Pointer modules; // const char** modules;

  public RimeTraits() {
    super();
    // Mimic RIME_STRUCT_INIT: data_size = sizeof(Type) - sizeof(data_size)
    int sz = this.size();
    this.data_size = sz - 4; // sizeof(int) = 4
  }

  @Override
  protected List<String> getFieldOrder() {
    // Field order must match rime_api.h exactly.
    return List.of(
        "data_size",
        "shared_data_dir",
        "user_data_dir",
        "distribution_name",
        "distribution_code_name",
        "distribution_version",
        "app_name",
        "modules");
  }
}
