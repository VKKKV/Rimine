package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
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
    // 模拟 C 里的 RIME_STRUCT_INIT 宏，非常重要！告诉 C 侧这个结构体的大小，用于做 ABI 兼容校验
    this.data_size = this.size();
  }

  @Override
  protected List<String> getFieldOrder() {
    // 这里的顺序必须和 rime_api.h 里的定义一模一样！
    return Arrays.asList(
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
