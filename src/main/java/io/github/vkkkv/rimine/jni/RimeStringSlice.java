package io.github.vkkkv.rimine.jni;

import com.sun.jna.Structure;
import java.util.List;

@Structure.FieldOrder({"str", "length"})
public class RimeStringSlice extends Structure {
  public String str;
  public long length;

  @Override
  protected List<String> getFieldOrder() {
    return List.of("str", "length");
  }
}
