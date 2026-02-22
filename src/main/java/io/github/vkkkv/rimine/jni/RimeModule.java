package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"data_size", "module_name", "initialize", "finalize", "get_api"})
public class RimeModule extends Structure {
  public int data_size;
  public String module_name;
  public Pointer initialize;
  public Pointer finalize;
  public Pointer get_api;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("data_size", "module_name", "initialize", "finalize", "get_api");
  }
}
