package io.github.vkkkv.rimine.jni;

import com.sun.jna.Structure;
import java.util.List;

@Structure.FieldOrder({"data_size", "text"})
public class RimeCommit extends Structure {
  public int data_size;
  public String text;

  public RimeCommit() {
    super();
    // Mimic RIME_STRUCT_INIT: data_size = sizeof(Type) - sizeof(data_size)
    int sz = this.size();
    this.data_size = sz - 4; // sizeof(int) = 4
  }

  @Override
  protected List<String> getFieldOrder() {
    return List.of("data_size", "text");
  }
}
