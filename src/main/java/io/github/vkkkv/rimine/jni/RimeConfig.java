package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"ptr"})
public class RimeConfig extends Structure {
  public Pointer ptr;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("ptr");
  }
}
