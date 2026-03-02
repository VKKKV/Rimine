package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.List;

@Structure.FieldOrder({"size", "list"})
public class RimeSchemaList extends Structure {
  public long size;
  public Pointer list;

  @Override
  protected List<String> getFieldOrder() {
    return List.of("size", "list");
  }
}
