package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"size", "list"})
public class RimeSchemaList extends Structure {
  public long size;
  public Pointer list;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("size", "list");
  }
}
